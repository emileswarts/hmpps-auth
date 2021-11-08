package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.azuread
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.delius
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.security.NomisUserService
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService

@Service
class UserContextService(
  private val deliusUserService: DeliusUserService,
  private val authUserService: AuthUserService,
  private val nomisUserService: NomisUserService,
  private val userService: UserService,
  @Value("\${application.link.accounts}") private val linkAccounts: Boolean,
) {

  fun discoverUsers(
    loginUser: UserPersonDetails,
    scopes: Set<String> = emptySet(),
    roles: List<String> = emptyList(),
  ): List<UserPersonDetails> {
    val authSource = AuthSource.fromNullableString(loginUser.authSource)
    if (authSource != azuread && !linkAccounts) return emptyList()

    // if specific accounts are requested via scopes, attempt to find just those.
    // otherwise, attempt to find all accounts.
    val requestedSources = AuthSource.values()
      .filter { it.name in scopes }
      .let { it.ifEmpty { setOf(auth, nomis, delius) } }

    val email = userService.getEmail(loginUser) ?: return emptyList()
    return requestedSources.map { findEnabledUsersWithRoles(email, it, roles) }
      .filter { it.isNotEmpty() }
      .flatten()
  }

  private fun findEnabledUsersWithRoles(email: String, source: AuthSource, roles: List<String>) = findUsers(email, source)
    .filter { u -> u.isEnabled }
    .filter { roles.isEmpty() || it.authorities.map { r -> r.authority }.intersect(roles.toSet()).isNotEmpty() }

  private fun findUsers(email: String, to: AuthSource): List<UserPersonDetails> =
    when (to) {
      delius -> deliusUserService.getDeliusUsersByEmail(email)
      auth -> authUserService.findAuthUsersByEmail(email).filter { it.verified }
      nomis -> nomisUserService.getNomisUsersByEmail(email)
      else -> emptyList()
    }
}
