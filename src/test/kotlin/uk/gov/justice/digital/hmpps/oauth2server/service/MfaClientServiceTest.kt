@file:Suppress("ClassName", "DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess
import uk.gov.justice.digital.hmpps.oauth2server.utils.MfaRememberMeContext
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService

internal class MfaClientServiceTest {
  private val clientDetailsService: ClientDetailsService = mock()
  private val clientDetails: ClientDetails = mock()
  private val mfaClientNetworkService: MfaClientNetworkService = mock()
  private val tokenService: TokenService = mock()
  private val service = MfaClientService(clientDetailsService, mfaClientNetworkService, tokenService)
  private val request: AuthorizationRequest = AuthorizationRequest()
  private val userDetails: UserDetails = mock()

  @BeforeEach
  fun setup() {
    request.clientId = "bob"
    whenever(clientDetails.clientId).thenReturn("fred")
    MfaRememberMeContext.token = null
  }

  @Nested
  inner class clientNeedsMfa {
    @Test
    fun `client doesn't needs mfa on trusted network`() {
      whenever(mfaClientNetworkService.outsideApprovedNetwork()).thenReturn(false)
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.untrusted.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isFalse
    }

    @Test
    fun `client needs mfa on untrusted network`() {
      whenever(mfaClientNetworkService.outsideApprovedNetwork()).thenReturn(true)
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.untrusted.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isTrue
    }

    @Test
    fun `client needs mfa everywhere`() {
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isTrue
    }

    @Test
    fun `client doesn't need mfa`() {
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isFalse
    }

    @Test
    fun `client has remember me set to false`() {
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name, "mfaRememberMe" to false))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isTrue
    }

    @Test
    fun `client has remember me set to true and no token`() {
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name, "mfaRememberMe" to true))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request, null)).isTrue
    }

    @Test
    fun `client has remember me set to true and a valid token`() {
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name, "mfaRememberMe" to true))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)
      whenever(tokenService.isValid(any(), any(), any())).thenReturn(true)
      whenever(userDetails.username).thenReturn("user")
      MfaRememberMeContext.token = "some token"

      assertThat(service.clientNeedsMfa(request, userDetails)).isFalse

      verify(tokenService).isValid(UserToken.TokenType.MFA_RMBR, "some token", "user")
    }
  }
}
