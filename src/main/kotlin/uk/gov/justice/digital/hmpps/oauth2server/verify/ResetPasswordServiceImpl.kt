package uk.gov.justice.digital.hmpps.oauth2server.verify

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.LockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.service.DelegatingUserService
import uk.gov.justice.digital.hmpps.oauth2server.service.NotificationClientRuntimeException
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailHelper.format
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.util.Optional

@Service
@Transactional
class ResetPasswordServiceImpl(
  private val userRepository: UserRepository,
  private val userTokenRepository: UserTokenRepository,
  private val userService: UserService,
  private val delegatingUserService: DelegatingUserService,
  private val notificationClient: NotificationClientApi,
  @Value("\${application.notify.reset.template}") private val resetTemplateId: String,
  @Value("\${application.notify.reset-unavailable.template}") private val resetUnavailableTemplateId: String,
  @Value("\${application.notify.reset-unavailable-email-not-found.template}") private val resetUnavailableEmailNotFoundTemplateId: String,
  @Value("\${application.notify.reset-password.template}") private val resetPasswordConfirmedTemplateId: String,
) : ResetPasswordService, PasswordService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  @Throws(NotificationClientRuntimeException::class, ResetPasswordException::class)
  override fun requestResetPassword(usernameOrEmailAddress: String, url: String): Optional<String> {
    val optionalUser: Optional<User>
    val multipleMatchesAndCanBeReset: Boolean
    if (StringUtils.contains(usernameOrEmailAddress, "@")) {
      val email = format(usernameOrEmailAddress)
      var matches = userRepository.findByEmail(email)
      if (matches.isEmpty()) {
        // no match in auth, check in nomis and delius
        val userDetailsList = setOf(AuthSource.nomis, AuthSource.delius)
          .map { it -> userService.findUserPersonDetailsByEmail(email!!, it).filter { it.isEnabled } }
          .filter { it.isNotEmpty() }
          .flatten()

        if (userDetailsList.size == 1) {
          val userPersonDetails = userDetailsList.first()
          val authSource = AuthSource.fromNullableString(userPersonDetails.authSource)
          if (authSource == AuthSource.nomis) saveNomisUser(userPersonDetails)
          else if (authSource == AuthSource.delius) saveDeliusUser(userPersonDetails)
          // update the match list as we have now saved the user
          matches = userRepository.findByEmail(email)
        } else {
          // no match or multiple matches, but got an email address so let them know
          sendEmail(email, resetUnavailableEmailNotFoundTemplateId, emptyMap<String, String>(), email)
          return Optional.empty()
        }
      }

      val passwordCanBeReset = matches.firstOrNull { passwordAllowedToBeReset(it) }
      multipleMatchesAndCanBeReset = passwordCanBeReset?.let { matches.size > 1 } ?: false
      optionalUser = Optional.of(matches[0])
    } else {
      multipleMatchesAndCanBeReset = false
      optionalUser = userRepository.findByUsername(usernameOrEmailAddress.uppercase())
        .or {
          // can't find the user in auth, so look in nomis or delius
          userService.findMasterUserPersonDetails(usernameOrEmailAddress.uppercase())
            .flatMap { userPersonDetails: UserPersonDetails ->
              when (AuthSource.fromNullableString(userPersonDetails.authSource)) {
                AuthSource.nomis -> saveNomisUser(userPersonDetails)
                AuthSource.delius -> saveDeliusUser(userPersonDetails)
                else -> Optional.empty()
              }
            }
        }
    }
    if (optionalUser.isEmpty || StringUtils.isEmpty(optionalUser.get().email)) {
      // no user found or email address found, so nothing more we can do. Bail
      return Optional.empty()
    }
    val user = optionalUser.get()
    if (user.source == AuthSource.azuread) throw ResetPasswordException("User password not stored in this system.")
    val templateAndParameters = getTemplateAndParameters(url, multipleMatchesAndCanBeReset, user)
    sendEmail(user.username, templateAndParameters, user.masterEmail())
    return Optional.ofNullable(templateAndParameters.resetLink)
  }

  private fun User.masterEmail(): String =
    if (source == AuthSource.delius)
      userService.getMasterUserPersonDetails(username, AuthSource.delius).map { it.toUser().email!! }.orElse(email)
    else email!!

  private fun saveDeliusUser(userPersonDetails: UserPersonDetails): Optional<User> {
    val user = userPersonDetails.toUser()
    if (!passwordAllowedToBeReset(user, userPersonDetails)) {
      return Optional.empty()
    }
    userRepository.save(user)
    return Optional.of(user)
  }

  private fun saveNomisUser(userPersonDetails: UserPersonDetails): Optional<User> {
    val user = userPersonDetails.toUser()
    return if (!passwordAllowedToBeReset(user, userPersonDetails)) {
      Optional.empty()
    } else {
      val email = if (userPersonDetails is NomisUserPersonDetails) Optional.ofNullable(userPersonDetails.email)
      else userService.getEmailAddressFromNomis(user.username)
      email.map {
        user.email = it
        user.verified = true
        userRepository.save(user)
        user
      }
    }
  }

  private fun getTemplateAndParameters(
    url: String,
    multipleMatchesAndCanBeReset: Boolean,
    user: User,
  ): TemplateAndParameters {

    val userDetails = userService.findEnabledOrNomisLockedUserPersonDetails(user.username)
      // can't find an enabled user in any system, so give up
      ?: return TemplateAndParameters(resetUnavailableTemplateId, user.firstName, user.name, user.authSource)

    // only allow reset for active accounts that aren't locked
    // or are locked by getting password incorrect (in either c-nomis or auth)
    val firstName = userDetails.firstName
    val fullName = userDetails.name
    if (multipleMatchesAndCanBeReset || passwordAllowedToBeReset(user, userDetails)) {
      val userToken = user.createToken(UserToken.TokenType.RESET)
      val selectOrConfirm = if (multipleMatchesAndCanBeReset) "select" else "confirm"
      val resetLink = "$url-$selectOrConfirm?token=${userToken.token}"
      return TemplateAndParameters(
        resetTemplateId,
        mapOf("firstName" to firstName, "fullName" to fullName, "resetLink" to resetLink)
      )
    }
    return TemplateAndParameters(resetUnavailableTemplateId, firstName, fullName, userDetails.authSource)
  }

  @Throws(NotificationClientRuntimeException::class)
  private fun sendEmail(username: String, templateAndParameters: TemplateAndParameters, email: String) {
    sendEmail(username, templateAndParameters.template, templateAndParameters.parameters, email)
  }

  @Throws(NotificationClientRuntimeException::class)
  private fun sendEmail(username: String?, template: String, parameters: Map<String, Any?>, email: String?) {
    try {
      log.info("Sending reset password to notify for user {}", username)
      notificationClient.sendEmail(template, email, parameters, null)
    } catch (e: NotificationClientException) {
      log.warn("Failed to send reset password to notify for user {}", username, e)
      if (e.httpResult >= 500) {
        // second time lucky
        try {
          notificationClient.sendEmail(template, email, parameters, null)
        } catch (e1: NotificationClientException) {
          throw NotificationClientRuntimeException(e1)
        }
      }
      throw NotificationClientRuntimeException(e)
    }
  }

  private fun passwordAllowedToBeReset(user: User, userPersonDetails: UserPersonDetails): Boolean =
    when (user.source) {
      AuthSource.azuread -> false
      AuthSource.nomis -> {
        val nomisApiUser = userPersonDetails as NomisUserPersonDetails
        val status = nomisApiUser.accountStatus
        (nomisApiUser.staffStatus == "ACTIVE") && (!status.isLocked || status.isUserLocked || user.locked)
      }
      // for non nomis users they must be enabled (so can be locked)
      else -> userPersonDetails.isEnabled
    }

  @Transactional
  override fun setPassword(token: String, password: String?) {
    val userToken = userTokenRepository.findById(token).orElseThrow()
    val user = userToken.user

    val userPersonDetails = userService.findEnabledOrNomisLockedUserPersonDetails(user.username)
      ?: throw ResetPasswordException("Can't find an enabled account for the user")

    if (!passwordAllowedToBeReset(user, userPersonDetails)) {
      // failed, so let user know
      throw LockedException("locked")
    }
    delegatingUserService.changePasswordWithUnlock(userPersonDetails, password!!)
    user.removeToken(userToken)
    userRepository.save(user)
    sendPasswordResetEmail(user)
  }

  private fun sendPasswordResetEmail(user: User) {
    // then the reset token
    val username = if (user.username.contains("@")) user.username.lowercase() else user.username
    val email = user.email
    val parameters = mapOf("firstName" to user.firstName, "fullName" to user.name, "username" to username)

    // send the email
    try {
      log.info("Sending password reset to notify for user {}", username)
      notificationClient.sendEmail(resetPasswordConfirmedTemplateId, email, parameters, null)
    } catch (e: NotificationClientException) {
      val reason = (if (e.cause != null) e.cause else e)!!.javaClass.simpleName
      log.warn("Failed to send password reset notify for user {} due to {}", username, reason, e)
      if (e.httpResult >= 500) {
        // second time lucky
        try {
          notificationClient.sendEmail(resetPasswordConfirmedTemplateId, email, parameters, null, null)
        } catch (ex: NotificationClientException) {
          log.error("Failed to send password reset notify for user {}", username, ex)
        }
      }
    }
  }

  private fun passwordAllowedToBeReset(ue: User): Boolean {
    val userPersonDetailsOptional =
      if (ue.isMaster) Optional.of(ue) else userService.findMasterUserPersonDetails(ue.username)
    return userPersonDetailsOptional.map { upd: UserPersonDetails -> passwordAllowedToBeReset(ue, upd) }.orElse(false)
  }

  @Transactional
  @Throws(
    ResetPasswordException::class
  )
  override fun moveTokenToAccount(token: String, username: String?): String {
    if (StringUtils.isBlank(username)) {
      throw ResetPasswordException("missing")
    }
    val lookupUsername = StringUtils.upperCase(StringUtils.trim(username))
    val userOptional = userRepository.findByUsername(lookupUsername)
    return userOptional.map { ue: User ->
      val userToken = userTokenRepository.findById(token).orElseThrow()
      // need to have same email address
      if (userToken.user.email != ue.email) {
        throw ResetPasswordException("email")
      }
      if (!passwordAllowedToBeReset(ue)) {
        throw ResetPasswordException("locked")
      }
      if (userToken.user.username == lookupUsername) {
        // no work since they are the same
        return@map token
      }
      // otherwise need to delete and add
      userTokenRepository.delete(userToken)
      val newUserToken = ue.createToken(UserToken.TokenType.RESET)
      newUserToken.token
    }.orElseThrow { ResetPasswordException("notfound") }
  }

  internal data class TemplateAndParameters(
    internal val template: String,
    internal val parameters: Map<String, Any>,
  ) {
    constructor(template: String, firstName: String, fullName: String, authSource: String) :
      this(
        template,
        mapOf(
          "firstName" to firstName,
          "fullName" to fullName,
          "deliusUser" to false,
          "authUser" to false,
          "nomisUser" to false,
          "${authSource}User" to true
        )
      )

    internal val resetLink: String?
      get() = parameters["resetLink"] as String?
  }

  class ResetPasswordException(val reason: String) : RuntimeException("Reset Password failed with reason: $reason")
}
