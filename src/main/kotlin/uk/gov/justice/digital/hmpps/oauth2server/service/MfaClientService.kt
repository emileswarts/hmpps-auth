@file:Suppress("DEPRECATION", "SpringJavaInjectionPointsAutowiringInspection")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetailsService
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess.all
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess.untrusted
import uk.gov.justice.digital.hmpps.oauth2server.utils.MfaRememberMeContext
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService

open class MfaClientService(
  private val clientDetailsService: ClientDetailsService,
  private val mfaClientNetworkService: MfaClientNetworkService,
  private val tokenService: TokenService,
) {

  open fun clientNeedsMfa(request: AuthorizationRequest?, user: UserDetails?): Boolean {
    val client = clientDetailsService.loadClientByClientId(request?.clientId)
    val baseClientId = ClientService.baseClientId(client.clientId)

    // Special case for migration of check my diary users - ignore 2fa if they don't have the migrated role
    if (baseClientId == "my-diary" &&
      user?.authorities?.none { it.authority == "ROLE_CMD_MIGRATED_MFA" } == true
    ) {
      return false
    }

    val mfa = client.additionalInformation["mfa"] as? String?
    val requireMfa = (mfa == untrusted.name && mfaClientNetworkService.outsideApprovedNetwork()) || mfa == all.name
    if (!requireMfa) return false

    // now check if they are allowed to use a remember me token (from the mfa_remember_me cookie) for that client
    // and if it is still valid
    val rememberMe = client.additionalInformation["mfaRememberMe"] as? Boolean? == true
    return !(rememberMe && tokenService.isValid(TokenType.MFA_RMBR, MfaRememberMeContext.token, user?.username))
  }
}
