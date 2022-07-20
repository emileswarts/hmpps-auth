package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Contact
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ContactType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailHelper
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.util.Optional
import javax.persistence.EntityNotFoundException

@Service
@Transactional(readOnly = true)
class VerifyEmailService(
  private val userRepository: UserRepository,
  private val userTokenRepository: UserTokenRepository,
  private val telemetryClient: TelemetryClient,
  private val notificationClient: NotificationClientApi,
  private val verifyEmailDomainService: VerifyEmailDomainService,
  private val nomisUserApiService: NomisUserApiService,
  @Value("\${application.notify.verify.template}") private val notifyTemplateId: String,
) {

  fun getEmail(username: String): Optional<User> =
    userRepository.findByUsername(username).filter { ue: User -> StringUtils.isNotBlank(ue.email) }

  fun isNotVerified(name: String): Boolean =
    !getEmail(name).map { obj: User -> obj.verified }.orElse(false)

  @Transactional
  @Throws(NotificationClientException::class, ValidEmailException::class)
  fun changeEmailAndRequestVerification(
    username: String,
    emailInput: String?,
    firstName: String?,
    fullname: String?,
    url: String,
    emailType: EmailType,
  ): LinkEmailAndUsername {
    val user = userRepository.findByUsername(username).orElseThrow()
    val verifyLink =
      url + user.createToken(if (emailType == EmailType.PRIMARY) UserToken.TokenType.VERIFIED else UserToken.TokenType.SECONDARY).token
    val parameters = mapOf("firstName" to firstName, "fullName" to fullname, "verifyLink" to verifyLink)
    val email = EmailHelper.format(emailInput)
    validateEmailAddress(email, emailType)
    when (emailType) {
      EmailType.PRIMARY -> {
        // if the user is configured so that the email address is their username, need to check it is unique
        if (username.contains("@") && email!!.uppercase() != user.username) {
          val userWithEmailInDatabase = userRepository.findByUsername(email.uppercase())
          if (userWithEmailInDatabase.isPresent) {
            // there's already a user in the database with that username
            throw ValidEmailException("duplicate")
          } else {
            user.username = email
            telemetryClient.trackEvent(
              "AuthUserChangeUsername",
              mapOf("username" to user.username, "previous" to username),
              null
            )
          }
        }
        user.email = email
        user.verified = false
      }
      EmailType.SECONDARY -> user.addContact(ContactType.SECONDARY_EMAIL, email)
    }
    try {
      log.info("Sending email verification to notify for user {}", username)
      notificationClient.sendEmail(notifyTemplateId, email, parameters, null)
      telemetryClient.trackEvent("VerifyEmailRequestSuccess", mapOf("username" to username), null)
    } catch (e: NotificationClientException) {
      val reason = (if (e.cause != null) e.cause else e)?.javaClass?.simpleName
      log.warn("Failed to send email verification to notify for user {}", username, e)
      telemetryClient.trackEvent(
        "VerifyEmailRequestFailure",
        mapOf("username" to username, "reason" to reason),
        null
      )
      if (e.httpResult >= 500) {
        // second time lucky
        notificationClient.sendEmail(notifyTemplateId, email, parameters, null, null)
      }
      throw e
    }
    userRepository.save(user)

    return LinkEmailAndUsername(verifyLink, email!!, user.username)
  }

  @Transactional
  @Throws(NotificationClientException::class, ValidEmailException::class)
  fun resendVerificationCodeEmail(username: String, url: String): Optional<String> {
    val user = userRepository.findByUsername(username).orElseThrow()
    if (user.email == null) {
      throw ValidEmailException("noemail")
    }
    if (user.verified) {
      log.info("Verify email succeeded due to already verified")
      telemetryClient.trackEvent(
        "VerifyEmailConfirmFailure",
        mapOf("reason" to "alreadyverified", "username" to username),
        null
      )
      return Optional.empty()
    }
    val verifyLink = url + user.createToken(UserToken.TokenType.VERIFIED).token
    val parameters = mapOf("firstName" to user.firstName, "fullName" to user.name, "verifyLink" to verifyLink)
    notificationClient.sendEmail(notifyTemplateId, user.email, parameters, null)
    return Optional.of(verifyLink)
  }

  @Transactional
  @Throws(NotificationClientException::class, ValidEmailException::class)
  fun resendVerificationCodeSecondaryEmail(username: String, url: String): Optional<String> {
    val user = userRepository.findByUsername(username).orElseThrow()
    if (user.secondaryEmail == null) {
      throw ValidEmailException("nosecondaryemail")
    }
    if (user.isSecondaryEmailVerified) {
      log.info("Verify secondary email succeeded due to already verified")
      telemetryClient.trackEvent(
        "VerifySecondaryEmailConfirmFailure",
        mapOf("reason" to "alreadyverified", "username" to username),
        null
      )
      return Optional.empty()
    }
    val verifyLink = url + user.createToken(UserToken.TokenType.SECONDARY).token
    val parameters = mapOf("firstName" to user.firstName, "fullName" to user.name, "verifyLink" to verifyLink)
    notificationClient.sendEmail(notifyTemplateId, user.secondaryEmail, parameters, null)
    return Optional.of(verifyLink)
  }

  fun secondaryEmailVerified(username: String): Boolean = userRepository.findByUsername(username)
    .orElseThrow { EntityNotFoundException(String.format("User not found with username %s", username)) }
    .isSecondaryEmailVerified

  @Throws(ValidEmailException::class)
  fun validateEmailAddress(email: String?, emailType: EmailType): Boolean {
    if (email.isNullOrBlank()) {
      throw ValidEmailException("blank")
    }
    if (email.length > MAX_LENGTH_EMAIL) throw ValidEmailException("maxlength")
    validateEmailAddressExcludingGsi(email, emailType)
    if (email.matches(Regex(".*@.*\\.gsi\\.gov\\.uk"))) throw ValidEmailException("gsi")
    return true
  }

  @Throws(ValidEmailException::class)
  fun validateEmailAddressExcludingGsi(email: String, emailType: EmailType) {
    val atIndex = email.indexOf('@')
    if (atIndex == -1 || !email.matches(Regex(".*@.*\\..*"))) {
      throw ValidEmailException("format")
    }
    val firstCharacter = email[0]
    val lastCharacter = email[email.length - 1]
    if (firstCharacter == '.' || firstCharacter == '@' || lastCharacter == '.' || lastCharacter == '@') {
      throw ValidEmailException("firstlast")
    }
    if (email.matches(Regex(".*\\.@.*")) || email.matches(Regex(".*@\\..*"))) {
      throw ValidEmailException("together")
    }
    if (StringUtils.countMatches(email, '@') > 1) {
      throw ValidEmailException("at")
    }
    if (StringUtils.containsWhitespace(email)) {
      throw ValidEmailException("white")
    }
    if (!email.matches(Regex("[0-9A-Za-z@.'_\\-+]*"))) {
      throw ValidEmailException("characters")
    }
    if (emailType == EmailType.PRIMARY && !verifyEmailDomainService.isValidEmailDomain(email.substring(atIndex + 1))) {
      throw ValidEmailException("domain")
    }
  }

  fun validateEmailDomainExcludingGsi(emailDomain: String): Boolean =
    verifyEmailDomainService.isValidEmailDomain(emailDomain)

  @Transactional
  fun confirmEmail(token: String): Optional<String> {
    val userTokenOptional = userTokenRepository.findById(token)
    if (userTokenOptional.isEmpty) {
      return trackAndReturnFailureForInvalidToken()
    }
    val userToken = userTokenOptional.get()
    val user = userToken.user
    val username = user.username
    if (user.verified) {
      log.info("Verify email succeeded due to already verified")
      telemetryClient.trackEvent(
        "VerifyEmailConfirmFailure",
        mapOf("reason" to "alreadyverified", "username" to username),
        null
      )
      return Optional.empty()
    }
    if (userToken.hasTokenExpired()) {
      return trackAndReturnFailureForExpiredToken(username)
    }
    markEmailAsVerified(user)
    return Optional.empty()
  }

  @Transactional
  fun confirmSecondaryEmail(token: String): Optional<String> {
    val userTokenOptional = userTokenRepository.findById(token)
    if (userTokenOptional.isEmpty) {
      return trackAndReturnFailureForInvalidToken()
    }
    val userToken = userTokenOptional.get()
    val user = userToken.user
    val username = user.username
    if (user.isSecondaryEmailVerified) {
      log.info("Verify secondary email succeeded due to already verified")
      telemetryClient.trackEvent(
        "VerifySecondaryEmailConfirmFailure",
        mapOf("reason" to "alreadyverified", "username" to username),
        null
      )
      return Optional.empty()
    }
    if (userToken.hasTokenExpired()) {
      return trackAndReturnFailureForExpiredToken(username)
    }
    markSecondaryEmailAsVerified(user)
    return Optional.empty()
  }

  @Transactional
  fun syncEmailWithNOMIS(username: String) =
    nomisUserApiService.findUserByUsername(username)
      ?.let { syncEmailWithNOMIS(username, it.email) }
      ?: throw UsernameNotFoundException("Account for username $username not found")

  @Transactional
  fun syncEmailWithNOMIS(username: String, nomisEmail: String?) {
    nomisEmail?.also {
      userRepository.findByUsername(username).filter {
        emailsDiffer(nomisEmail, it.email) && emailChangeNotInProgress(it)
      }.ifPresent { user ->
        user.email = nomisEmail
        user.verified = true

        log.info("Email re-synchronised with NOMIS for {}", user.username)
        telemetryClient.trackEvent("SynchroniseEmailSuccess", mapOf("username" to user.username), null)
      }
    }
  }

  private fun emailsDiffer(nomisEmail: String, authEmail: String?) = nomisEmail != authEmail

  private fun emailChangeNotInProgress(user: User): Boolean {
    return user.verified || user.email.isNullOrBlank()
  }

  private fun markEmailAsVerified(user: User) {
    // verification token match
    user.verified = true
    userRepository.save(user)

    if (AuthSource.fromNullableString(user.authSource) == AuthSource.nomis) {
      nomisUserApiService.changeEmail(user.username, user.email!!)
    }

    log.info("Verify email succeeded for {}", user.username)
    telemetryClient.trackEvent("VerifyEmailConfirmSuccess", mapOf("username" to user.username), null)
  }

  private fun markSecondaryEmailAsVerified(user: User) {
    // verification token match
    user.findContact(ContactType.SECONDARY_EMAIL).ifPresent { c: Contact -> c.verified = true }
    userRepository.save(user)
    log.info("Verify secondary email succeeded for {}", user.username)
    telemetryClient.trackEvent("VerifySecondaryEmailConfirmSuccess", mapOf("username" to user.username), null)
  }

  private fun trackAndReturnFailureForInvalidToken(): Optional<String> {
    log.info("Verify email failed due to invalid token")
    telemetryClient.trackEvent("VerifyEmailConfirmFailure", mapOf("reason" to "invalid"), null)
    return Optional.of("invalid")
  }

  private fun trackAndReturnFailureForExpiredToken(username: String): Optional<String> {
    log.info("Verify email failed due to expired token")
    telemetryClient.trackEvent(
      "VerifyEmailConfirmFailure",
      mapOf("reason" to "expired", "username" to username),
      null
    )
    return Optional.of("expired")
  }

  class ValidEmailException(val reason: String) : Exception("Validate email failed with reason: $reason")

  data class LinkEmailAndUsername(val link: String, val email: String, val username: String)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    private const val MAX_LENGTH_EMAIL = 240
  }
}
