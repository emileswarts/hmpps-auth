package uk.gov.justice.digital.hmpps.oauth2server.security

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientNetworkService
import javax.persistence.EntityManager

@Service("authUserDetailsService")
@Transactional(
  readOnly = true,
  noRollbackFor = [UsernameNotFoundException::class]
)
class AuthUserDetailsService(
  private val authUserService: AuthUserService,
  private val authEntityManager: EntityManager
) :
  UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

  override fun loadUserByUsername(username: String): UserDetails {
    val userPersonDetails =
      authUserService.getAuthUserByUsername(username).orElseThrow { UsernameNotFoundException(username) }
    // ensure that any changes to user details past this point are not persisted - e.g. by calling CredentialsContainer.eraseCredentials
    authEntityManager.detach(userPersonDetails)
    return userPersonDetails
  }

  override fun loadUserDetails(token: PreAuthenticatedAuthenticationToken): UserDetails = loadUserByUsername(token.name)
}

@Component
@Transactional(
  readOnly = true,
  noRollbackFor = [BadCredentialsException::class]
)
class AuthAuthenticationProvider(
  authUserDetailsService: AuthUserDetailsService,
  userRetriesService: UserRetriesService,
  mfaClientNetworkService: MfaClientNetworkService,
  userService: UserService,
  telemetryClient: TelemetryClient,
) :
  LockingAuthenticationProvider(authUserDetailsService, userRetriesService, mfaClientNetworkService, userService, telemetryClient)
