@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.security

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.azuread
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.UserContextService

class UserContextApprovalHandler(
  private val userContextService: UserContextService,
  clientDetailsService: ClientDetailsService,
  private val mfaClientService: MfaClientService,
  private val linkAccounts: Boolean,
  private val authServicesService: AuthServicesService,
) : TokenStoreUserApprovalHandler() {

  init {
    super.setClientDetailsService(clientDetailsService)
  }

  /**
   * Users need approval if:
   * <ol>
   *   <li>The service requires MFA and they haven't already been through it</li>
   *   <li>The user is an Azure user</li>
   * </ol>
   */
  override fun checkForPreApproval(
    authorizationRequest: AuthorizationRequest,
    userAuthentication: Authentication,
  ): AuthorizationRequest {

    // we have hijacked the UserContextApprovalHandler for our account selection process.
    // we are purposefully not calling the super method, because if we deny the request
    // based on unapproved scopes we do not currently have a way to explicitly approve it.

    val userDetails = userAuthentication.principal as UserDetailsImpl
    if (!userDetails.passedMfa && mfaClientService.clientNeedsMfa(authorizationRequest, userDetails)) {
      authorizationRequest.isApproved = false
      return authorizationRequest
    }

    // All Azure AD users are sent down this route, the controller will work out what accounts are found etc.
    val authSource = AuthSource.fromNullableString(userDetails.authSource)
    authorizationRequest.isApproved =
      !(authSource == azuread || linkAccounts || noPrivileges(userDetails, authorizationRequest))

    return authorizationRequest
  }

  private fun noPrivileges(userDetails: UserDetailsImpl, authorizationRequest: AuthorizationRequest): Boolean {
    val service = authServicesService.findService(ClientService.baseClientId(authorizationRequest.clientId))
    val serviceRoles = service?.roles ?: emptyList()
    return !userContextService.checkUser(
      loginUser = userDetails,
      scopes = authorizationRequest.scope,
      roles = serviceRoles
    )
  }

  /**
   * Overridden to
   * <ol>
   *   <li>Store whether the user needs MFA in the approval request</li>
   *   <li>Retrieve and store azure users in the approval request for use by the authorization endpoint</li>
   * </ol>
   */
  override fun getUserApprovalRequest(
    authorizationRequest: AuthorizationRequest,
    userAuthentication: Authentication,
  ): MutableMap<String, Any> {

    val userApprovalRequest = super.getUserApprovalRequest(authorizationRequest, userAuthentication)

    // if we are in as an azure user find out any users that can be mapped to the current user
    val userDetails = userAuthentication.principal as UserDetailsImpl
    val service = authServicesService.findService(ClientService.baseClientId(authorizationRequest.clientId))
    val serviceRoles = service?.roles ?: emptyList()
    if (userDetails.authSource == azuread.source || linkAccounts) {
      val users = userContextService.discoverUsers(
        loginUser = userDetails,
        scopes = authorizationRequest.scope,
        roles = serviceRoles
      )
      userApprovalRequest["users"] = users
      userApprovalRequest["service"] = service?.name ?: "this service"
    } else if (!userContextService.checkUser(
        loginUser = userDetails, scopes = authorizationRequest.scope, roles = serviceRoles
      )
    ) {
      userApprovalRequest["users"] = emptyList<UserPersonDetails>()
      userApprovalRequest["service"] = service?.name ?: "this service"
    }

    if (!userDetails.passedMfa && mfaClientService.clientNeedsMfa(authorizationRequest, userDetails)) {
      // found a client that requires mfa
      userApprovalRequest["requireMfa"] = true
    }

    return userApprovalRequest
  }
}
