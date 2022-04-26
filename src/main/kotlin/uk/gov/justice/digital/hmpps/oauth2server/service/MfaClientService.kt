@file:Suppress("DEPRECATION", "SpringJavaInjectionPointsAutowiringInspection")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetailsService
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess.all
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess.untrusted

open class MfaClientService(
  private val clientDetailsService: ClientDetailsService,
  private val mfaClientNetworkService: MfaClientNetworkService,
) {

  open fun clientNeedsMfa(request: AuthorizationRequest?): Boolean {
    val client = clientDetailsService.loadClientByClientId(request?.clientId)
    val baseClientId = ClientService.baseClientId(client.clientId)

    // Special case for migration of check my diary users - ignore 2fa if they don't have the migrated role
    if (baseClientId == "my-diary" &&
      request?.authorities?.none { it.authority == "ROLE_CMD_MIGRATED_MFA" } == true
    ) {
      return false
    }

    val mfa = client.additionalInformation["mfa"] as? String?
    return (mfa == untrusted.name && mfaClientNetworkService.outsideApprovedNetwork()) || mfa == all.name
  }
}
