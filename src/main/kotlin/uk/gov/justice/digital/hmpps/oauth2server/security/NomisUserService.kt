package uk.gov.justice.digital.hmpps.oauth2server.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType.PRIMARY
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.LinkEmailAndUsername
import java.util.Optional

@Service
@Transactional(readOnly = true)
abstract class NomisUserService(
  private val staffUserAccountRepository: StaffUserAccountRepository,
  private val userRepository: UserRepository,
  private val verifyEmailService: VerifyEmailService,
  private val nomisUserApiService: NomisUserApiService,
  @Value("\${nomis.enabled:false}") private val nomisEnabled: Boolean,
) {
  fun getNomisUserByUsername(username: String): Optional<NomisUserPersonDetails> =
    staffUserAccountRepository.findById(username.uppercase())

  fun getNomisUsersByEmail(email: String): List<NomisApiUserPersonDetails> {
    val emailLowered = email.lowercase()

    // Find all users in auth with the specified email address
    val allNomisInAuthUsernames = userRepository.findByEmailAndSourceOrderByUsername(emailLowered, nomis)
      .filter { it.verified }
      .associate { it.username to it.email }

    // Find all nomis users for the usernames or email address
    val allNomis =
      nomisUserApiService.findUsersByEmailAddressAndUsernames(emailLowered, allNomisInAuthUsernames.keys)

    // then use all the results from nomis with emails from auth
    return allNomis.map { it.copy(email = emailLowered) }
  }

  fun findPrisonUsersByFirstAndLastNames(firstName: String, lastName: String): List<NomisUserSummaryDto> {
    return nomisUserApiService.findUsers(firstName, lastName)
  }

  fun changeEmailAndRequestVerification(username: String, emailInput: String?, url: String, emailType: EmailType): LinkEmailAndUsername {
    val user = getNomisUserByUsername(username)
      .orElseThrow { UsernameNotFoundException("Account for username $username not found") }

    return verifyEmailService.changeEmailAndRequestVerification(
      username = user.username,
      emailInput = emailInput,
      firstName = user.firstName,
      fullname = user.name,
      url = url,
      emailType = PRIMARY
    )
  }

  @Transactional
  fun changePasswordWithUnlock(username: String, password: String) {
    changePassword(username, password)
    if (nomisEnabled) nomisUserApiService.unlockAccount(username)
    else staffUserAccountRepository.unlockUser(username)
  }

  @Transactional
  fun lockAccount(username: String) {
    if (nomisEnabled) nomisUserApiService.lockAccount(username)
    else staffUserAccountRepository.lockUser(username)
  }

  fun changePassword(username: String, password: String) {
    if (nomisEnabled) nomisUserApiService.changePassword(username, password)
    // bit naff here - but until we have migrated everything need to also call changePasswordInternal too
    changePasswordInternal(username, password)
  }

  abstract fun changePasswordInternal(username: String, password: String)
}
