@file:Suppress("DEPRECATION", "ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.config

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Client
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientAllowedIps
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientAllowedIpsRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthIpSecurity
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.utils.JwtAuthHelper
import uk.gov.justice.digital.hmpps.oauth2server.utils.JwtAuthHelper.JwtParameters
import java.time.LocalDateTime
import java.util.Optional

internal class TrackingTokenServicesTest {

  private val authIpSecurity: AuthIpSecurity = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val tokenStore: TokenStore = mock()
  private val clientDetailsService: ClientDetailsService = mock()
  private val userService: UserService = mock()
  private val clientRepository: ClientRepository = mock()
  private val clientAllowedIpsRepository: ClientAllowedIpsRepository = mock()
  private val restTemplate: RestTemplate = mock()
  private val tokenVerificationClientCredentials = TokenVerificationClientCredentials()
  private val tokenServices =
    TrackingTokenServices(authIpSecurity, telemetryClient, restTemplate, clientRepository, clientAllowedIpsRepository, tokenVerificationClientCredentials, true)
  private val tokenServicesVerificationDisabled =
    TrackingTokenServices(authIpSecurity, telemetryClient, restTemplate, clientRepository, clientAllowedIpsRepository, tokenVerificationClientCredentials, false)
  private val request = MockHttpServletRequest()

  @BeforeEach
  fun setUp() {
    tokenVerificationClientCredentials.clientId = "token-verification-client-id"
    tokenServices.setSupportRefreshToken(true)
    tokenServices.setTokenStore(tokenStore)
    tokenServicesVerificationDisabled.setSupportRefreshToken(true)
    tokenServicesVerificationDisabled.setTokenStore(tokenStore)
    val tokenEnhancer = JWTTokenEnhancer()
    ReflectionTestUtils.setField(tokenEnhancer, "clientsDetailsService", clientDetailsService)
    ReflectionTestUtils.setField(tokenEnhancer, "userService", userService)
    tokenServices.setTokenEnhancer(tokenEnhancer)
    tokenServicesVerificationDisabled.setTokenEnhancer(tokenEnhancer)
    whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(BaseClientDetails())
    request.remoteAddr = "12.21.23.24"
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, null))
  }

  @Nested
  inner class `create access token` {
    @Test
    fun createAccessToken() {
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(Client("id")))
      val userAuthentication = UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
      tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication))
      verify(telemetryClient).trackEvent(
        "CreateAccessToken",
        mapOf("username" to "authenticateduser", "clientId" to "client", "clientIpAddress" to "12.21.23.24"),
        null
      )
    }

    @Test
    fun `create access token calls token verification service`() {
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(Client("id")))
      val userAuthentication = UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
      tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication))
      verify(restTemplate).postForLocation(
        eq("/token?authJwtId={authJwtId}"),
        check {
          assertThat(it).isInstanceOf(String::class.java).asString().hasSize(27)
        },
        eq("jwtId")
      )
    }

    @Test
    fun `create access token ignores token verification service if disabled`() {
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(Client("id")))
      val userAuthentication = UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
      tokenServicesVerificationDisabled.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication))
      verifyNoInteractions(restTemplate)
    }

    @Test
    fun createAccessToken_ClientOnly() {
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(Client("id")))
      tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, null))
      verify(telemetryClient).trackEvent(
        "CreateSystemAccessToken",
        mapOf("username" to "client", "clientId" to "client", "clientIpAddress" to "12.21.23.24"),
        null
      )
    }

    @Test
    fun `updates last accessed for client`() {
      val client = Client("id")
      // reset to past date
      client.lastAccessed = LocalDateTime.now().minusDays(1)
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(client))
      tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_SCOPE_REQUEST, null))
      // then assert has been updated
      assertThat(client.lastAccessed).isAfter(LocalDateTime.now().minusMinutes(5))
    }

    @Test
    fun `createAccessToken request from allowed ip`() {
      whenever(clientAllowedIpsRepository.findById(anyString())).thenReturn(Optional.of(ClientAllowedIps("client", listOf("12.21.23.24"))))
      whenever(clientRepository.findById(anyString())).thenReturn(Optional.of(Client("id")))
      val userAuthentication = UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
      tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication))
      verify(telemetryClient).trackEvent(
        "CreateAccessToken",
        mapOf("username" to "authenticateduser", "clientId" to "client", "clientIpAddress" to "12.21.23.24"),
        null
      )
    }

    @Test
    fun `createAccessToken throw error when request not from allowed IP`() {
      whenever(clientAllowedIpsRepository.findById(anyString())).thenReturn(Optional.of(ClientAllowedIps("client", listOf("12.21.23.24"))))
      doThrow(AllowedIpException()).whenever(authIpSecurity).validateClientIpAllowed(anyString(), any())
      val userAuthentication = UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
      Assertions.assertThatThrownBy { tokenServices.createAccessToken(OAuth2Authentication(OAUTH_2_REQUEST, userAuthentication)) }
        .isInstanceOf(
          AllowedIpException::class.java
        ).hasMessage("Unable to issue token as request is not from ip within allowed list")
    }
  }

  @Nested
  inner class `refresh access token` {
    private val refreshToken =
      JwtAuthHelper().createJwt(JwtParameters(additionalClaims = mapOf("ati" to "accessTokenId")))

    @Test
    fun refreshAccessToken() {
      whenever(tokenStore.readRefreshToken(anyString())).thenReturn(DefaultOAuth2RefreshToken("newValue"))
      whenever(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(
        OAuth2Authentication(
          OAUTH_2_REQUEST,
          UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
        )
      )
      tokenServices.refreshAccessToken(refreshToken, TokenRequest(emptyMap(), "client", emptySet(), "refresh"))
      verify(telemetryClient).trackEvent(
        "RefreshAccessToken",
        mapOf("username" to "authenticateduser", "clientId" to "client", "clientIpAddress" to "12.21.23.24"),
        null
      )
    }

    @Test
    fun `refresh access token calls token verification service`() {
      whenever(tokenStore.readRefreshToken(anyString())).thenReturn(DefaultOAuth2RefreshToken("newValue"))
      whenever(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(
        OAuth2Authentication(
          OAUTH_2_REQUEST,
          UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
        )
      )
      tokenServices.refreshAccessToken(refreshToken, TokenRequest(emptyMap(), "client", emptySet(), "refresh"))
      verify(restTemplate).postForLocation(
        eq("/token/refresh?accessJwtId={accessJwtId}"),
        check {
          assertThat(it).isInstanceOf(String::class.java).asString().hasSize(27)
        },
        eq("accessTokenId")
      )
    }

    @Test
    fun `refresh access token ignores token verification service if disabled`() {
      whenever(tokenStore.readRefreshToken(anyString())).thenReturn(DefaultOAuth2RefreshToken("newValue"))
      whenever(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(
        OAuth2Authentication(
          OAUTH_2_REQUEST,
          UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
        )
      )
      tokenServicesVerificationDisabled.refreshAccessToken(
        refreshToken,
        TokenRequest(emptyMap(), "client", emptySet(), "refresh")
      )
      verifyNoInteractions(restTemplate)
    }

    @Test
    fun `refresh access token ignores token verification service if client is token verification`() {
      val tokenVerificationAuthRequest = OAuth2Request(
        emptyMap(),
        "token-verification-client-id",
        emptySet(),
        true,
        emptySet(),
        emptySet(),
        "redirect",
        null,
        null
      )

      whenever(tokenStore.readRefreshToken(anyString())).thenReturn(DefaultOAuth2RefreshToken("newValue"))
      whenever(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(
        OAuth2Authentication(
          tokenVerificationAuthRequest,
          UsernamePasswordAuthenticationToken(USER_DETAILS, "credentials")
        )
      )
      tokenServicesVerificationDisabled.refreshAccessToken(
        refreshToken,
        TokenRequest(emptyMap(), "token-verification-client-id", emptySet(), "refresh")
      )
      verifyNoInteractions(restTemplate)
    }
  }

  companion object {
    private val OAUTH_2_REQUEST =
      OAuth2Request(emptyMap(), "client", emptySet(), true, emptySet(), emptySet(), "redirect", null, null)
    private val USER_DETAILS = UserDetailsImpl("authenticateduser", "name", emptySet(), "none", "userid", "jwtId")
    private val OAUTH_2_SCOPE_REQUEST = OAuth2Request(
      emptyMap(),
      "community-api-client",
      listOf(GrantedAuthority { "ROLE_COMMUNITY" }),
      true,
      setOf("proxy-user"),
      emptySet(),
      "redirect",
      null,
      null
    )
  }
}
