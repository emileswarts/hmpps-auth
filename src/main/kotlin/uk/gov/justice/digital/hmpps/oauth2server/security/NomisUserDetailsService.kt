package uk.gov.justice.digital.hmpps.oauth2server.security

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientNetworkService

@Service("nomisUserDetailsService")
class NomisUserDetailsService(private val nomisUserService: NomisUserService) :
  UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

  override fun loadUserByUsername(username: String): UserDetails =
    nomisUserService.getNomisUserByUsernameFromNomisUserApiService(username)
      ?: throw UsernameNotFoundException(username)

  override fun loadUserDetails(token: PreAuthenticatedAuthenticationToken): UserDetails = loadUserByUsername(token.name)
}

@Component
class NomisAuthenticationProvider(
  private val nomisUserApiService: NomisUserApiService,
  nomisUserDetailsService: NomisUserDetailsService,
  userRetriesService: UserRetriesService,
  mfaClientNetworkService: MfaClientNetworkService,
  userService: UserService,
  telemetryClient: TelemetryClient,
) :
  LockingAuthenticationProvider(nomisUserDetailsService, userRetriesService, mfaClientNetworkService, userService, telemetryClient) {

  override fun checkPassword(userDetails: UserDetails, password: String): Boolean =
    nomisUserApiService.authenticateUser(userDetails.username, password)
}
