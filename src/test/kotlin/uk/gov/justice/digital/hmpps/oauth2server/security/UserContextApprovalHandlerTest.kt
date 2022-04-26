@file:Suppress("DEPRECATION", "ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.OAuth2RequestFactory
import org.springframework.security.oauth2.provider.token.TokenStore
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.UserContextService

internal class UserContextApprovalHandlerTest {
  private val userContextService: UserContextService = mock()
  private val authServicesService: AuthServicesService = mock()
  private val mfaClientService: MfaClientService = mock()
  private val handler = UserContextApprovalHandler(userContextService, mock(), mfaClientService, false, authServicesService)
  private val linkAccountsEnabledHandler = UserContextApprovalHandler(userContextService, mock(), mfaClientService, true, authServicesService)
  private val authentication: Authentication = mock()
  private val authorizationRequest = AuthorizationRequest()
  private val requestFactory: OAuth2RequestFactory = mock()
  private val tokenStore: TokenStore = mock()
  private val oAuth2AccessToken: OAuth2AccessToken = mock()
  private val clientDetails: ClientDetails = mock()

  private val service = Service("CODE", "NAME", "Description", "ROLE_BOB,ROLE_FRED", "http://some.url", true, "a@b.com")

  @BeforeEach
  internal fun setUp() {
    handler.setRequestFactory(requestFactory)
    handler.setTokenStore(tokenStore)
    authorizationRequest.clientId = "someClient"
  }

  @Nested
  inner class checkForPreApproval {
    @Test
    fun `not approved as client needs mfa`() {
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId")
      )
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name))
      whenever(mfaClientService.clientNeedsMfa(any(), any())).thenReturn(true)
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      val approval = handler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isFalse
    }

    @Test
    fun `approved as mfa already passed`() {
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(true)
      val approval = handler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isTrue
    }

    @Test
    fun `not approved as azure ad user`() {
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      val approval = handler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isFalse
    }

    @Test
    fun `not approved as link accounts enabled`() {
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      val approval = linkAccountsEnabledHandler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isFalse
    }

    @Test
    fun `not approved as no privileges`() {
      authorizationRequest.clientId = "base-client-239"
      val userDetails =
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId", passedMfa = true)
      whenever(authentication.principal).thenReturn(
        userDetails
      )
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(false)
      val approval = handler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isFalse
      verify(authServicesService).findService("base-client")
      verify(userContextService).checkUser(userDetails, emptySet(), listOf("ROLE_BOB", "ROLE_FRED"))
    }

    @Test
    fun `approved as not azure ad user`() {
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(tokenStore.getAccessToken(any())).thenReturn(oAuth2AccessToken)
      whenever(oAuth2AccessToken.isExpired).thenReturn(false)
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(true)
      val approval = handler.checkForPreApproval(authorizationRequest, authentication)
      assertThat(approval.isApproved).isTrue
    }
  }

  @Nested
  inner class getUserApprovalRequest {
    @Test
    fun `already passed mfa`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(true)
      val map = handler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactly(entry("bob", "joe"))
    }

    @Test
    fun `needs mfa`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId")
      )
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name))
      whenever(mfaClientService.clientNeedsMfa(any(), any())).thenReturn(true)
      val users = listOf(createSampleUser(username = "harry"))
      whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(users)
      val map = handler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "users" to users, "requireMfa" to true, "service" to "this service"))
    }

    @Test
    fun `not an azuread user`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(true)
      val map = handler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactly(entry("bob", "joe"))
    }

    @Test
    fun `user has no privileges`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.checkUser(any(), any(), any())).thenReturn(false)
      val map = handler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "service" to "NAME", "users" to emptyList<UserPersonDetails>()))
    }

    @Test
    fun `an azuread user`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      val users = listOf(createSampleUser(username = "harry"))
      whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(users)
      val map = handler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "users" to users, "service" to "this service"))
    }

    @Test
    fun `link accounts enabled`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId", passedMfa = true)
      )
      val users = listOf(createSampleUser(username = "harry"))
      whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(users)
      val map = linkAccountsEnabledHandler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "users" to users, "service" to "this service"))
    }

    @Test
    fun `roles passed through to discover users`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      val userDetails =
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId", passedMfa = true)
      whenever(authentication.principal).thenReturn(userDetails)
      val users = listOf(createSampleUser(username = "harry"))
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(users)
      val map = linkAccountsEnabledHandler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "users" to users, "service" to "NAME"))
      verify(userContextService).discoverUsers(userDetails, emptySet(), listOf("ROLE_BOB", "ROLE_FRED"))
    }

    @Test
    fun `calls auth service with base client id`() {
      authorizationRequest.requestParameters = mutableMapOf("bob" to "joe")
      authorizationRequest.clientId = "base-client-239"
      val userDetails =
        UserDetailsImpl("user", "name", setOf(), AuthSource.nomis.name, "userid", "jwtId", passedMfa = true)
      whenever(authentication.principal).thenReturn(userDetails)
      val users = listOf(createSampleUser(username = "harry"))
      whenever(authServicesService.findService(any())).thenReturn(service)
      whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(users)
      val map = linkAccountsEnabledHandler.getUserApprovalRequest(authorizationRequest, authentication)
      assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf("bob" to "joe", "users" to users, "service" to "NAME"))
      verify(authServicesService).findService("base-client")
    }
  }
}
