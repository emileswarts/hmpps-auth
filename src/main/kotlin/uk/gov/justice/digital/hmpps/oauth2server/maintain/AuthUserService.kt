package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.microsoft.applicationinsights.TelemetryClient
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Person
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthServiceRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck.AuthUserGroupRelationshipException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailHelper
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.ValidEmailException
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import javax.persistence.EntityNotFoundException

@Service
@Transactional(readOnly = true)
class AuthUserService(
  private val userRepository: UserRepository,
  private val notificationClient: NotificationClientApi,
  private val telemetryClient: TelemetryClient,
  private val verifyEmailService: VerifyEmailService,
  private val authUserGroupService: AuthUserGroupService,
  private val maintainUserCheck: MaintainUserCheck,
  private val passwordEncoder: PasswordEncoder,
  private val oauthServiceRepository: OauthServiceRepository,
  @Value("\${application.notify.create-initial-password.template}") private val initialPasswordTemplateId: String,
  @Value("\${application.authentication.disable.login-days}") private val loginDaysTrigger: Int,
  @Value("\${application.authentication.password-age}") private val passwordAge: Long,
  @Value("\${application.notify.enable-user.template}") private val enableUserTemplateId: String,
) {
  @Transactional
  @Throws(CreateUserException::class, NotificationClientException::class, ValidEmailException::class)
  fun createUserByEmail(
    emailInput: String?,
    firstName: String?,
    lastName: String?,
    groupCodes: Set<String>?,
    url: String,
    creator: String,
    authorities: Collection<GrantedAuthority>,
  ): UUID? {
    val email = EmailHelper.format(emailInput)
    validatePrimary(email, firstName, lastName)
    // get the initial groups to assign to - only allowed to be empty if super user
    val groups = getInitialGroups(groupCodes, creator, authorities)
    val person = Person(firstName!!.trim(), lastName!!.trim())
    // obtain list of authorities that should be assigned for group
    val roles = groups.flatMap { it.assignableRoles }.filter { it.automatic }.mapNotNull { it.role }.toSet()

    // username should now be set to a user's email address & ensure username always uppercase
    val username = StringUtils.upperCase(email)

    val user = User(
      username = username,
      email = email,
      enabled = true,
      source = AuthSource.auth,
      person = person,
      authorities = roles,
      groups = groups,
    )
    val (_, userId) = saveAndSendInitialEmail(url, user, creator, "AuthUserCreate", groups)
    return userId
  }

  private fun getInitialEmailSupportLink(groups: Collection<Group>): String {
    val serviceCode = groups.firstOrNull { it.groupCode.startsWith("PECS") }?.let { "book-a-secure-move-ui" } ?: "prison-staff-hub"
    return oauthServiceRepository.findById(serviceCode).map { it.email!! }.orElseThrow()
  }

  fun findAuthUsers(
    name: String?,
    roleCodes: List<String>?,
    groupCodes: List<String>?,
    pageable: Pageable,
    searcher: String,
    authorities: Collection<GrantedAuthority>,
    status: UserFilter.Status,
    authSources: List<AuthSource> = listOf(AuthSource.auth),
  ): Page<User> {
    val groupSearchCodes = if (authorities.any { it.authority == "ROLE_MAINTAIN_OAUTH_USERS" }) {
      groupCodes
    } else if (authorities.any { it.authority == "ROLE_AUTH_GROUP_MANAGER" }) {
      val assignableGroupCodes = authUserGroupService.getAssignableGroups(searcher, authorities).map { it.groupCode }
      if (groupCodes.isNullOrEmpty()) assignableGroupCodes else groupCodes.filter { g -> assignableGroupCodes.any { it == g } }
    } else {
      emptyList()
    }
    val userFilter = UserFilter(
      name = name,
      roleCodes = roleCodes,
      groupCodes = groupSearchCodes,
      status = status,
      authSources = authSources
    )
    return userRepository.findAll(userFilter, pageable)
  }

  fun findAuthUsersByUsernames(usernames: List<String>): List<User> = userRepository.findByUsernameIn(usernames)

  @Throws(CreateUserException::class)
  private fun getInitialGroups(
    groupCodes: Set<String>?,
    creator: String,
    authorities: Collection<GrantedAuthority>
  ): Set<Group> {
    if (groupCodes.isNullOrEmpty()) {
      return if (authorities.any { it.authority == "ROLE_MAINTAIN_OAUTH_USERS" }) {
        emptySet()
      } else throw CreateUserException("groupCode", "missing")
    }
    val authUserGroups = authUserGroupService.getAssignableGroups(creator, authorities)
    val groups = authUserGroups.filter { it.groupCode in groupCodes }.toSet()

    if (groups.isEmpty()) {
      throw CreateUserException("groupCode", "notfound")
    }
    return groups
  }

  @Throws(NotificationClientException::class)
  private fun saveAndSendInitialEmail(
    url: String,
    user: User,
    creator: String,
    eventPrefix: String,
    groups: Collection<Group>,
  ): Pair<String, UUID?> { // then the reset token
    val userToken = user.createToken(UserToken.TokenType.RESET)
    // give users more time to do the reset
    userToken.tokenExpiry = LocalDateTime.now().plusDays(7)
    val savedUser = userRepository.save(user)
    // support link
    val supportLink = getInitialEmailSupportLink(groups)
    val setPasswordLink = url + userToken.token
    val username = user.username
    val email = user.email
    val parameters = mapOf(
      "firstName" to user.name,
      "fullName" to user.name,
      "resetLink" to setPasswordLink,
      "supportLink" to supportLink
    )
    // send the email
    try {
      log.info("Sending initial set password to notify for user {}", username)
      notificationClient.sendEmail(initialPasswordTemplateId, email, parameters, null)
      telemetryClient.trackEvent("${eventPrefix}Success", mapOf("username" to username, "admin" to creator), null)
    } catch (e: NotificationClientException) {
      val reason = (e.cause?.let { e.cause } ?: e).javaClass.simpleName
      log.warn("Failed to send create user notify for user {}", username, e)
      telemetryClient.trackEvent(
        "${eventPrefix}Failure",
        mapOf("username" to username, "reason" to reason, "admin" to creator),
        null
      )
      if (e.httpResult >= 500) { // second time lucky
        notificationClient.sendEmail(initialPasswordTemplateId, email, parameters, null, null)
        telemetryClient.trackEvent("${eventPrefix}Success", mapOf("username" to username, "admin" to creator), null)
      }
      throw e
    }
    // return the reset link and userId to the controller
    return Pair(setPasswordLink, savedUser.id)
  }

  @Transactional
  @Throws(ValidEmailException::class, NotificationClientException::class, AuthUserGroupRelationshipException::class, UsernameNotFoundException::class)
  fun amendUserEmailByUserId(
    userId: String,
    emailAddressInput: String?,
    url: String,
    admin: String,
    authorities: Collection<GrantedAuthority>,
    emailType: EmailType,
  ): String {
    val user = userRepository.findByIdOrNull(UUID.fromString(userId)) ?: throw UsernameNotFoundException("User $userId not found")
    val username = user.username
    maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, user)
    if (user.password != null) {
      return verifyEmailService.changeEmailAndRequestVerification(
        username,
        emailAddressInput,
        user.firstName,
        user.name,
        url.replace("initial-password", "verify-email-confirm"),
        emailType
      ).link
    }
    val email = EmailHelper.format(emailAddressInput)
    verifyEmailService.validateEmailAddress(email, emailType)
    if (user.email == username.lowercase()) {
      userRepository.findByUsername(email!!.uppercase()).ifPresent {
        throw ValidEmailException("duplicate")
      }
      user.username = email
      telemetryClient.trackEvent(
        "AuthUserChangeUsername",
        mapOf("username" to user.username, "previous" to username),
        null
      )
    }
    user.email = email
    user.verified = false
    val (resetLink, _) = saveAndSendInitialEmail(url, user, admin, "AuthUserAmend", user.groups)
    return resetLink
  }

  @Transactional
  @Throws(ValidEmailException::class, NotificationClientException::class, AuthUserGroupRelationshipException::class)
  fun amendUserEmail(
    usernameInput: String,
    emailAddressInput: String?,
    url: String,
    admin: String,
    authorities: Collection<GrantedAuthority>,
    emailType: EmailType,
  ): String {
    val username = StringUtils.upperCase(usernameInput)
    val user = userRepository.findByUsernameAndMasterIsTrue(username)
      .orElseThrow { EntityNotFoundException("User not found with username $username") }
    maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, user)
    if (user.password != null) {
      return verifyEmailService.changeEmailAndRequestVerification(
        username,
        emailAddressInput,
        user.firstName,
        user.name,
        url.replace("initial-password", "verify-email-confirm"),
        emailType
      ).link
    }
    val email = EmailHelper.format(emailAddressInput)
    verifyEmailService.validateEmailAddress(email, emailType)
    if (user.email == username.lowercase()) {
      userRepository.findByUsername(email!!.uppercase()).ifPresent {
        throw ValidEmailException("duplicate")
      }
      user.username = email
      telemetryClient.trackEvent(
        "AuthUserChangeUsername",
        mapOf("username" to user.username, "previous" to username),
        null
      )
    }
    user.email = email
    user.verified = false
    val (resetLink, _) = saveAndSendInitialEmail(url, user, admin, "AuthUserAmend", user.groups)
    return resetLink
  }

  fun findAuthUsersByEmail(email: String?): List<User> =
    userRepository.findByEmailAndMasterIsTrueOrderByUsername(EmailHelper.format(email))

  fun getAuthUserByUsername(username: String?): Optional<User> =
    userRepository.findByUsernameAndMasterIsTrue(StringUtils.upperCase(StringUtils.trim(username)))

  fun getAuthUserByUserId(id: String, admin: String, authorities: Collection<GrantedAuthority>): User? =
    userRepository.findByIdOrNull(UUID.fromString(id))?.also {
      it.authorities.size // initialises user roles
      maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, it)
    }

  @Transactional
  @Throws(AuthUserGroupRelationshipException::class)
  fun enableUserByUserId(userId: String, admin: String, requestUrl: String, authorities: Collection<GrantedAuthority>) {
    userRepository.findByIdOrNull(UUID.fromString(userId))?.let { user ->
      maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, user)
      user.isEnabled = true
      user.inactiveReason = null
      // give user 7 days grace if last logged in more than x days ago
      if (user.lastLoggedIn.isBefore(LocalDateTime.now().minusDays(loginDaysTrigger.toLong()))) {
        user.lastLoggedIn = LocalDateTime.now().minusDays(loginDaysTrigger - 7L)
      }
      userRepository.save(user)
      user.email?.let { sendEnableEmail(user = user, creator = admin, requestUrl = requestUrl) }
      telemetryClient.trackEvent("AuthUserEnabled", mapOf("username" to user.username, "admin" to admin), null)
    } ?: throw UsernameNotFoundException("User $userId not found")
  }

  private fun sendEnableEmail(user: User, creator: String, requestUrl: String) {
    with(user) {
      val parameters = mapOf(
        "firstName" to firstName,
        "username" to username,
        "signinUrl" to requestUrl.replaceAfter("/auth/", ""),
      )
      // send the email
      try {
        log.info("Sending enable user email to notify for user {}", username)
        notificationClient.sendEmail(enableUserTemplateId, email, parameters, null)
      } catch (e: NotificationClientException) {
        val reason = (e.cause?.let { e.cause } ?: e).javaClass.simpleName
        log.warn("Failed to send enable user email for user {}", username, e)
        telemetryClient.trackEvent(
          "AuthUserEnabledEmailFailure",
          mapOf("username" to username, "reason" to reason, "admin" to creator),
          null
        )
        throw e
      }
    }
  }

  @Transactional
  @Throws(AuthUserGroupRelationshipException::class)
  fun disableUser(
    username: String,
    admin: String,
    inactiveReason: String,
    authorities: Collection<GrantedAuthority>
  ) {
    val user = userRepository.findByUsernameAndMasterIsTrue(username)
      .orElseThrow { EntityNotFoundException("User not found with username $username") }
    maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, user)
    user.isEnabled = false
    user.inactiveReason = inactiveReason
    userRepository.save(user)
    telemetryClient.trackEvent("AuthUserDisabled", mapOf("username" to user.username, "admin" to admin), null)
  }

  @Transactional
  @Throws(AuthUserGroupRelationshipException::class)
  fun disableUserByUserId(userId: String, admin: String, inactiveReason: String, authorities: Collection<GrantedAuthority>) {
    userRepository.findByIdOrNull(UUID.fromString(userId))?.let { user ->
      maintainUserCheck.ensureUserLoggedInUserRelationship(admin, authorities, user)
      user.isEnabled = false
      user.inactiveReason = inactiveReason
      userRepository.save(user)
      telemetryClient.trackEvent("AuthUserDisabled", mapOf("username" to user.username, "admin" to admin), null)
    } ?: throw UsernameNotFoundException("User $userId not found")
  }

  @Throws(CreateUserException::class, ValidEmailException::class)
  private fun validatePrimary(email: String?, firstName: String?, lastName: String?) {
    validate(firstName, lastName)

    verifyEmailService.validateEmailAddress(email, EmailType.PRIMARY)
  }

  @Throws(CreateUserException::class)
  private fun validate(firstName: String?, lastName: String?) {
    if (firstName.isNullOrBlank()) throw CreateUserException("firstName", "required")
    else {
      if (firstName.any(XSS_CHAR_BLOCK_LIST::contains)) throw CreateUserException("firstName", "invalid")
      if (firstName.length < MIN_LENGTH_FIRST_NAME) throw CreateUserException("firstName", "length")
      else if (firstName.length > MAX_LENGTH_FIRST_NAME) throw CreateUserException("firstName", "maxlength")
    }

    if (lastName.isNullOrBlank()) throw CreateUserException("lastName", "required")
    else {
      if (lastName.any(XSS_CHAR_BLOCK_LIST::contains)) throw CreateUserException("lastName", "invalid")
      if (lastName.length < MIN_LENGTH_LAST_NAME) throw CreateUserException("lastName", "length")
      else if (lastName.length > MAX_LENGTH_LAST_NAME) throw CreateUserException("lastName", "maxlength")
    }
  }

  @Transactional
  fun lockUser(userPersonDetails: UserPersonDetails) {
    val username = userPersonDetails.username
    val userOptional = userRepository.findByUsername(username)
    val user = userOptional.orElseGet { userPersonDetails.toUser() }
    user.locked = true
    userRepository.save(user)
  }

  @Transactional
  fun unlockUser(userPersonDetails: UserPersonDetails) {
    val username = userPersonDetails.username
    val userOptional = userRepository.findByUsername(username)
    val user = userOptional.orElseGet { userPersonDetails.toUser() }
    user.locked = false
    // TODO: This isn't quite right - shouldn't always verify a user when unlocking...
    user.verified = true
    userRepository.save(user)
  }

  @Transactional
  fun changePassword(user: User, password: String) {
    // check user not setting password to existing password
    if (passwordEncoder.matches(password, user.password)) {
      throw ReusedPasswordException()
    }
    user.setPassword(passwordEncoder.encode(password))
    user.passwordExpiry = LocalDateTime.now().plusDays(passwordAge)
  }

  @Transactional
  @Throws(CreateUserException::class)
  fun amendUser(username: String, firstName: String?, lastName: String?) {
    validate(firstName, lastName)
    // will always be a user at this stage since we're retrieved it from the authentication
    val user = userRepository.findByUsernameAndMasterIsTrue(username).orElseThrow()
    user.person!!.firstName = firstName!!.trim()
    user.person!!.lastName = lastName!!.trim()
    userRepository.save(user)
  }

  @Transactional
  fun useEmailAsUsername(username: String?): String? {
    val user = userRepository.findByUsernameAndMasterIsTrue(username).orElseThrow()

    // double check can switch
    val emailUpper = user.email?.uppercase()
    if (emailUpper != null && !user.username.contains('@') &&
      userRepository.findByUsernameAndMasterIsTrue(emailUpper).isEmpty
    ) {
      user.username = emailUpper
      userRepository.save(user)

      telemetryClient.trackEvent(
        "AuthUserChangeUsername",
        mapOf("username" to user.username, "previous" to username),
        null
      )

      return user.email
    }
    return null
  }

  class CreateUserException(val field: String, val errorCode: String) :
    Exception("Create user failed for field $field with reason: $errorCode")

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    // Data item field size validation checks
    private const val MAX_LENGTH_FIRST_NAME = 50
    private const val MAX_LENGTH_LAST_NAME = 50
    private const val MIN_LENGTH_FIRST_NAME = 2
    private const val MIN_LENGTH_LAST_NAME = 2

    // XSS char block list:
    // These variants of greater-than and less-than are persisted as the standard versions
    // (U+003C and U+003E) in the database
    private val XSS_CHAR_BLOCK_LIST = setOf('<', '＜', '〈', '〈', '>', '＞', '〉', '〉')
  }
}
