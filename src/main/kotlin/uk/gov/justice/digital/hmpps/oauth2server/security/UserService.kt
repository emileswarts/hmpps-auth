package uk.gov.justice.digital.hmpps.oauth2server.security

import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.azure.service.AzureUserService
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.azuread
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.delius
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.none
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import java.util.Optional

@Service
class UserService(
  private val nomisUserService: NomisUserService,
  private val authUserService: AuthUserService,
  private val deliusUserService: DeliusUserService,
  private val azureUserService: AzureUserService,
  private val userRepository: UserRepository,
  private val verifyEmailService: VerifyEmailService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    fun isHmpsGsiEmail(email: String) = email.endsWith("hmps.gsi.gov.uk")
  }

  fun findMasterUserPersonDetails(username: String): Optional<UserPersonDetails> =
    authUserService.getAuthUserByUsername(username).map { UserPersonDetails::class.java.cast(it) }
      .or {
        Optional.ofNullable(nomisUserService.getNomisUserByUsername(username))
          .map { UserPersonDetails::class.java.cast(it) }
      }
      .or { azureUserService.getAzureUserByUsername(username).map { UserPersonDetails::class.java.cast(it) } }
      .or { deliusUserService.getDeliusUserByUsername(username).map { UserPersonDetails::class.java.cast(it) } }

  fun findEnabledOrNomisLockedUserPersonDetails(username: String): UserPersonDetails? =
    authUserService.getAuthUserByUsername(username).filter { it.isEnabled }.map { UserPersonDetails::class.java.cast(it) }
      .or {
        Optional.ofNullable(nomisUserService.getNomisUserByUsername(username))
          .filter { it.isEnabled || it.accountStatus == AccountStatus.LOCKED }
          .map { UserPersonDetails::class.java.cast(it) }
      }
      .or { azureUserService.getAzureUserByUsername(username).filter { it.isEnabled }.map { UserPersonDetails::class.java.cast(it) } }
      .or { deliusUserService.getDeliusUserByUsername(username).filter { it.isEnabled }.map { UserPersonDetails::class.java.cast(it) } }
      .orElse(null)

  fun getMasterUserPersonDetailsWithEmailCheck(
    username: String,
    authSource: AuthSource,
    loginUser: UserPersonDetails,
  ): Optional<UserPersonDetails> =
    getMasterUserPersonDetails(username, authSource).filter { emailMatchesUser(getEmail(loginUser), it) }

  fun findUserPersonDetailsByEmail(email: String, to: AuthSource): List<UserPersonDetails> = when (to) {
    auth -> authUserService.findAuthUsersByEmail(email).filter { it.verified }
    nomis -> nomisUserService.getNomisUsersByEmail(email)
    delius -> deliusUserService.getDeliusUsersByEmail(email)
    else -> emptyList()
  }

  fun getMasterUserPersonDetails(username: String, authSource: AuthSource): Optional<UserPersonDetails> =
    when (authSource) {
      auth -> authUserService.getAuthUserByUsername(username)
      nomis -> Optional.ofNullable(nomisUserService.getNomisUserByUsername(username))
      azuread -> azureUserService.getAzureUserByUsername(username)
      delius -> deliusUserService.getDeliusUserByUsername(username)
      none -> Optional.empty()
    }.map { UserPersonDetails::class.java.cast(it) }

  @Transactional(readOnly = true)
  fun findUsersBySource(source: AuthSource): List<User> = userRepository.findBySourceOrderByUsername(source)

  fun getEmail(userPersonDetails: UserPersonDetails): String? {
    // special case where the authentication is passed through - we won't have the email address in that case
    val email = if (userPersonDetails is UserDetailsImpl || userPersonDetails.authSource == nomis.source) {
      verifyEmailService.getEmail(userPersonDetails.username).filter { it.verified }.map { it.email }.orElse(null)
    } else with(userPersonDetails.toUser()) { if (verified) email else null }

    return if (userPersonDetails.authSource == nomis.source) {
      email ?: getEmailAddressFromNomis(userPersonDetails.username).orElse(null)
    } else email
  }

  private fun emailMatchesUser(email: String?, userPersonDetails: UserPersonDetails): Boolean =
    email == getEmail(userPersonDetails)
  @Transactional(readOnly = true)
  fun findUser(username: String): Optional<User> = userRepository.findByUsername(StringUtils.upperCase(username))

  fun getUser(username: String): User =
    findUser(username).orElseThrow { UsernameNotFoundException("User with username $username not found") }

  @Transactional(readOnly = true)
  fun getUserWithContacts(username: String): User = findUser(username)
    .map {
      // initialise contacts by calling size
      it.contacts.size
      it
    }
    .orElseThrow { UsernameNotFoundException("User with username $username not found") }

  @Transactional
  fun getOrCreateUser(username: String): Optional<User> =
    findUser(username).or {
      findMasterUserPersonDetails(username).map {
        val user = it.toUser()
        if (AuthSource.fromNullableString(user.authSource) == nomis) {
          getEmailAddressFromNomis(username).ifPresent { email ->
            user.email = email
            user.verified = true
          }
        }
        userRepository.save(user)
      }
    }

  fun getEmailAddressFromNomis(username: String): Optional<String> =
    nomisUserService.getNomisUserByUsername(username)?.email?.let {
      if (isHmpsGsiEmail(it)) Optional.empty() else Optional.of(it)
    } ?: Optional.empty()

  @Transactional(readOnly = true)
  fun hasVerifiedMfaMethod(userDetails: UserPersonDetails): Boolean {
    val user = findUser(userDetails.username).orElseGet { userDetails.toUser() }
    return user.hasVerifiedMfaMethod()
  }

  @Transactional(readOnly = true)
  fun isSameAsCurrentVerifiedMobile(username: String, mobile: String?): Boolean {
    val user = getUser(username)
    val canonicalMobile = mobile?.replace("\\s+".toRegex(), "")
    return user.isMobileVerified && canonicalMobile == user.mobile
  }

  @Transactional(readOnly = true)
  fun isSameAsCurrentVerifiedEmail(username: String, email: String, emailType: EmailType): Boolean {
    val user = getUser(username)
    if (emailType == EmailType.SECONDARY) {
      return user.isSecondaryEmailVerified && email == user.secondaryEmail
    }
    return user.verified && email == user.email
  }

  fun findPrisonUsersByFirstAndLastNames(firstName: String, lastName: String): List<PrisonUserDto> {
    val nomisUsers: List<NomisUserSummaryDto> =
      nomisUserService.findPrisonUsersByFirstAndLastNames(firstName, lastName)

    val authUsersByUsername = authUserService
      .findAuthUsersByUsernames(nomisUsers.map { it.username })
      .filter { !it.email.isNullOrBlank() && it.source == nomis }
      .associateBy { it.username }

    return nomisUsers.map {
      PrisonUserDto(
        username = it.username,
        userId = it.staffId,
        email = authUsersByUsername[it.username]?.email ?: it.email,
        verified = authUsersByUsername[it.username]?.verified ?: (it.email != null),
        firstName = it.firstName,
        lastName = it.lastName,
        activeCaseLoadId = it.activeCaseload?.id
      )
    }
  }

  fun searchUsersInMultipleSourceSystems(
    name: String?,
    pageable: Pageable,
    searcher: String,
    authorities: Collection<GrantedAuthority>,
    status: UserFilter.Status,
    authSources: List<AuthSource>?,
  ): Page<User> {
    val sources = if (authSources.isNullOrEmpty()) listOf(auth) else authSources
    return authUserService.findAuthUsers(
      name = name,
      roleCodes = emptyList(),
      groupCodes = emptyList(),
      pageable = pageable,
      searcher = searcher,
      authorities = authorities,
      status = status,
      authSources = sources
    )
  }

  @Transactional
  fun getOrCreateUsers(usernames: List<String>): List<User> = usernames.mapNotNull {
    getOrCreateUser(it).orElse(null)
  }
}

data class PrisonUserDto(
  val username: String,
  val userId: String,
  val email: String?,
  val verified: Boolean,
  val firstName: String,
  val lastName: String,
  val activeCaseLoadId: String?
)
