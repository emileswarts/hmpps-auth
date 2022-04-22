@file:Suppress("ClassName", "DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaAccess

internal class MfaClientServiceTest {
  private val clientDetailsService: ClientDetailsService = mock()
  private val clientDetails: ClientDetails = mock()
  private val mfaClientNetworkService: MfaClientNetworkService = mock()
  private val service = MfaClientService(clientDetailsService, mfaClientNetworkService)
  private val request: AuthorizationRequest = AuthorizationRequest()

  @BeforeEach
  fun setup() {
    request.clientId = "bob"
    whenever(clientDetails.clientId).thenReturn("fred")
  }

  @Nested
  inner class clientNeedsMfa {
    @Test
    fun `client doesn't needs mfa on trusted network`() {
      whenever(mfaClientNetworkService.outsideApprovedNetwork()).thenReturn(false)
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.untrusted.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isFalse
    }

    @Test
    fun `client needs mfa on untrusted network`() {
      whenever(mfaClientNetworkService.outsideApprovedNetwork()).thenReturn(true)
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.untrusted.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isTrue
    }

    @Test
    fun `client needs mfa everywhere`() {
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.all.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isTrue
    }

    @Test
    fun `client doesn't need mfa`() {
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isFalse
    }

    @Test
    fun `check my diary client no role`() {
      whenever(clientDetails.clientId).thenReturn("my-diary")
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isFalse
    }

    @Test
    fun `check my diary client incorrect role`() {
      whenever(clientDetails.clientId).thenReturn("my-diary")
      request.authorities = mutableListOf(SimpleGrantedAuthority("ROLE_JOE"))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isFalse
    }

    @Test
    fun `check my diary client with role but mfa disabled for my-diary`() {
      request.authorities = mutableListOf(SimpleGrantedAuthority("ROLE_CMD_MIGRATED_MFA"), SimpleGrantedAuthority("ROLE_JOE"))
      whenever(clientDetails.clientId).thenReturn("my-diary-5")
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isFalse
    }

    @Test
    fun `check my diary client with role and mfa enabled for my-diary`() {
      request.authorities = mutableListOf(SimpleGrantedAuthority("ROLE_CMD_MIGRATED_MFA"), SimpleGrantedAuthority("ROLE_JOE"))
      whenever(clientDetails.clientId).thenReturn("my-diary-5")
      whenever(mfaClientNetworkService.outsideApprovedNetwork()).thenReturn(true)
      whenever(clientDetails.additionalInformation).thenReturn(mapOf("mfa" to MfaAccess.untrusted.name))
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(clientDetails)

      assertThat(service.clientNeedsMfa(request)).isTrue
    }
  }
}
