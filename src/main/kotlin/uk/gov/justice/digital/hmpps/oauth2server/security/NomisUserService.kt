package uk.gov.justice.digital.hmpps.oauth2server.security

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType.PRIMARY
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.LinkEmailAndUsername

@Service
class NomisUserService(
  private val userRepository: UserRepository,
  private val verifyEmailService: VerifyEmailService,
  private val nomisUserApiService: NomisUserApiService,
) {
  fun getNomisUserByUsername(username: String): NomisApiUserPersonDetails? =
    nomisUserApiService.findUserByUsername(username.uppercase())

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

  fun changeEmailAndRequestVerification(
    username: String,
    emailInput: String?,
    url: String,
    emailType: EmailType
  ): LinkEmailAndUsername =
    getNomisUserByUsername(username)?.let {
      return verifyEmailService.changeEmailAndRequestVerification(
        username = it.username,
        emailInput = emailInput,
        firstName = it.firstName,
        fullname = it.name,
        url = url,
        emailType = PRIMARY
      )
    } ?: throw UsernameNotFoundException("Account for username $username not found")

  fun changePasswordWithUnlock(username: String, password: String) {
    changePassword(username, password)
    nomisUserApiService.unlockAccount(username)
  }

  fun lockAccount(username: String) {
    nomisUserApiService.lockAccount(username)
  }

  fun changePassword(username: String, password: String) {
    nomisUserApiService.changePassword(username, password)
  }
}
