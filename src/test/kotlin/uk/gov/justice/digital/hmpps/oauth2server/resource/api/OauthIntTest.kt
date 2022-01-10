package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.auth0.jwt.JWT
import com.microsoft.applicationinsights.TelemetryClient
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.resource.DeliusExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension
import java.util.Base64

@Suppress("DEPRECATION")
@ExtendWith(DeliusExtension::class, NomisExtension::class)
class OauthIntTest : IntegrationTest() {
  @MockBean
  private lateinit var telemetryClient: TelemetryClient
  @MockBean
  private lateinit var tokenVerificationApiRestTemplate: OAuth2RestTemplate
  @MockBean
  private lateinit var nomisUserApiService: NomisUserApiService

  @BeforeEach
  internal override fun setupTokenVerification() {
    // no action required as mocking
  }

  @Test
  fun `Existing auth code stored in database can be redeemed for access token`() {
    // from database oauth_code table.  To regenerate - put a breakpoint in AbstractAuthSpecification.getAccessToken
    // just before the call to get the access token.  Then go to the /auth/h2-console (blank username or password) and
    // look at the last row in the oauth_code table
    val authCode = "5bDHCW"
    val clientUrl = "http://localhost:8081/login" // same as row in oauth_code table
    webTestClient
      .post().uri("/oauth/token?grant_type=authorization_code&code=$authCode&redirect_uri=$clientUrl")
      .headers(setBasicAuthorisation("ZWxpdGUyYXBpY2xpZW50OmNsaWVudHNlY3JldA=="))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath(".user_name").isEqualTo("ITAG_USER")
      .jsonPath(".user_id").isEqualTo("1")
      .jsonPath(".user_uuid").isEqualTo("a04c70ee-51c9-4852-8d0d-130da5c85c42")
      .jsonPath(".sub").isEqualTo("ITAG_USER")
      .jsonPath(".auth_source").isEqualTo("nomis")
  }

  @Test
  fun `Client Credentials Login`() {
    val encodedClientAndSecret = convertToBase64("deliusnewtech", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
      }
  }

  @Test
  fun `Client Credentials Login doesn't create token verification`() {
    val encodedClientAndSecret = convertToBase64("deliusnewtech", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
      }
    verifyNoInteractions(tokenVerificationApiRestTemplate)
  }

  @Test
  fun `Client Credentials incorrect login creates telemetry event`() {
    val encodedClientAndSecret = convertToBase64("delisnewtech", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isUnauthorized

    verify(telemetryClient).trackEvent(
      "CreateAccessTokenFailure",
      mapOf("clientId" to "delisnewtech", "clientIpAddress" to "127.0.0.1"),
      null
    )
  }

  @Test
  fun `Client Credentials Login hold subject`() {
    val encodedClientAndSecret = convertToBase64("deliusnewtech", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it["sub"]).isEqualTo("deliusnewtech")
      }
  }

  @Test
  fun `Client Credentials Login With username identifier`() {
    // whenever(nomisUserApiService.authenticateUser("CA_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("CA_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "CA_USER",
        userId = "47",
        firstName = "Licence Case",
        surname = "Admin",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )
    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    val token = getClientCredentialsTokenWithUsername(encodedClientAndSecret, "CA_USER")

    webTestClient
      .get().uri("/api/user/me")
      .header("Authorization", "Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "name" to "Licence Case Admin",
          )
        )
      }
  }

  @Test
  fun `Client Credentials Login access token`() {

    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials&username=CA_USER")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
        assertThat(it["auth_source"]).isEqualTo("none")
      }
  }

  @Test
  fun `Client Credentials Login access token for auth user`() {

    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials&username=AUTH_USER")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
        assertThat(it["auth_source"]).isEqualTo("none")
      }
  }

  @Test
  fun `Client Credentials Login access token with auth source`() {

    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials&username=AUTH_USER&auth_source=delius")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
        assertThat(it["auth_source"]).isEqualTo("delius")
      }
  }

  @Test
  fun `Client Credentials Login access token for proxy user with no username`() {

    val encodedClientAndSecret = convertToBase64("community-api-client", "community-api-client")
    webTestClient
      .post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(3600)
        assertThat(it).doesNotContainKey("refreshToken")
        assertThat(it["auth_source"]).isEqualTo("none")
      }
  }

  @Test
  fun `Client Credentials Login With username identifier for auth user`() {

    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    val token = getClientCredentialsTokenWithUsername(encodedClientAndSecret, "AUTH_USER")

    webTestClient
      .get().uri("/api/user/me")
      .header("Authorization", "Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "name" to "Auth Only",
          )
        )
      }
  }

  @Test
  fun `Password Credentials Login`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(28800)
        assertThat(it["refresh_token"]).isNotNull
        assertThat(it["auth_source"]).isEqualTo("nomis")
        assertThat(it["sub"]).isEqualTo("ITAG_USER")
      }
  }

  @Test
  fun `Password Credentials Login calls token verification`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(28800)
        assertThat(it["refresh_token"]).isNotNull
        assertThat(it["auth_source"]).isEqualTo("nomis")
        assertThat(it["sub"]).isEqualTo("ITAG_USER")
      }
    verify(tokenVerificationApiRestTemplate).postForLocation(eq("/token?authJwtId={authJwtId}"), any(), any<Any>())
  }

  @Test
  fun `Password Credentials Login for auth user`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=AUTH_USER&password=password123456")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(28800)
        assertThat(it["refresh_token"]).isNotNull
        assertThat(it["auth_source"]).isEqualTo("auth")
        assertThat(it["sub"]).isEqualTo("AUTH_USER")
      }
  }

  @Test
  fun `Password Credentials Login for delius user`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=delius&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsKey("expires_in")
        assertThat(it["expires_in"] as Int).isLessThan(28800)
        assertThat(it["refresh_token"]).isNotNull
        assertThat(it["auth_source"]).isEqualTo("delius")
        assertThat(it["sub"]).isEqualTo("DELIUS")
      }
  }

  @Test
  fun `Refresh token can be obtained`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    val (accessToken, refreshToken) = getAccessAndRefreshTokens(encodedClientAndSecret, "ITAG_USER", "password")

    webTestClient
      .post().uri("/oauth/token?grant_type=refresh_token&refresh_token=$refreshToken")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".['refresh_token','access_token']").value<JSONArray> {
        val newAccessToken = (it[0] as Map<*, *>)["access_token"].toString()
        val newRefreshToken = (it[0] as Map<*, *>)["refresh_token"].toString()
        assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken)
        assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken)
      }
  }

  @Test
  fun `Refresh token can be obtained for auth user`() {
    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    val (accessToken, refreshToken) = getAccessAndRefreshTokens(encodedClientAndSecret, "AUTH_USER", "password123456")

    webTestClient
      .post().uri("/oauth/token?grant_type=refresh_token&refresh_token=$refreshToken")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".['refresh_token','access_token']").value<JSONArray> {
        val newAccessToken = (it[0] as Map<*, *>)["access_token"].toString()
        val newRefreshToken = (it[0] as Map<*, *>)["refresh_token"].toString()
        assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken)
        assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken)
      }
  }

  @Test
  fun `Refresh token can be obtained for delius user`() {
    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    val (accessToken, refreshToken) = getAccessAndRefreshTokens(encodedClientAndSecret, "delius", "password")

    webTestClient
      .post().uri("/oauth/token?grant_type=refresh_token&refresh_token=$refreshToken")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath(".['refresh_token','access_token']").value<JSONArray> {
        val newAccessToken = (it[0] as Map<*, *>)["access_token"].toString()
        val newRefreshToken = (it[0] as Map<*, *>)["refresh_token"].toString()
        assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken)
        assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken)
      }
  }

  @Test
  fun `Password Credentials Login with Bad password credentials`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER_BAD_PW&password=password2")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "error" to "invalid_grant",
            "error_description" to "Bad credentials",
          )
        )
      }
  }

  @Test
  fun `Password Credentials Login with Bad client credentials`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecretBAD")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Password Credentials Login with Wrong client Id`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclientBAD", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Password Credentials Login with Expired Login`() {
    whenever(nomisUserApiService.authenticateUser("EXPIRED_USER", "password123456")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("EXPIRED_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "EXPIRED_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = false,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=EXPIRED_USER&password=password123456")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "error" to "invalid_grant",
            "error_description" to "User credentials have expired",
          )
        )
      }
  }

  @Test
  fun `Password Credentials Login with Locked Login`() {
    whenever(nomisUserApiService.authenticateUser("LOCKED_USER", "password123456")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("LOCKED_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "LOCKED_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = false,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=LOCKED_USER&password=password123456")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "error" to "invalid_grant",
            "error_description" to "User account is locked",
          )
        )
      }
  }

  @Test
  fun `Password Credentials Login with Locked Login for Delius User`() {

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    webTestClient
      .post().uri("/oauth/token?grant_type=password&username=DELIUS_ERROR_LOCKED&password=password123456")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "error" to "unauthorized",
            "error_description" to "User is disabled",
          )
        )
      }
  }

  @Test
  fun `Password Credentials Login can get api data`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Itag",
        surname = "User",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    val token = getPasswordCredentialsToken(encodedClientAndSecret)

    webTestClient
      .get().uri("/api/user/me")
      .header("Authorization", "Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "name" to "Itag User",
          )
        )
      }
  }

  @Test
  fun `Client Credentials Login can get api data`() {

    val encodedClientAndSecret = convertToBase64("omicadmin", "clientsecret")
    val token = getClientCredentialsToken(encodedClientAndSecret)

    webTestClient
      .get().uri("/api/user/me")
      .header("Authorization", "Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "username" to "omicadmin",
          )
        )
      }
  }

  @Test
  fun `Kid header is returned`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val encodedClientAndSecret = convertToBase64("elite2apiclient", "clientsecret")
    val result = webTestClient
      .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
      .header("Authorization", "Basic $encodedClientAndSecret")
      .exchange()
      .expectStatus().isOk
      .expectBody<String>().returnResult()

    val token = JSONObject(result.responseBody).get("access_token")
    assertThat(JWT.decode(token as String?).getHeaderClaim("kid").asString()).isEqualTo("dps-client-key")
  }

  @Test
  fun `Grant type is returned in client_credentials token`() {
    val token = getClientCredentialsToken(convertToBase64("deliusnewtech", "clientsecret"))

    assertThat(JWT.decode(token).getClaim("grant_type").asString()).isEqualTo("client_credentials")
  }

  @Test
  fun `Grant type is returned in client_credentials token with username`() {
    val token = getClientCredentialsTokenWithUsername(convertToBase64("deliusnewtech", "clientsecret"), "username")

    assertThat(JWT.decode(token).getClaim("grant_type").asString()).isEqualTo("client_credentials")
  }

  @Test
  fun `Grant type is returned in password token`() {
    whenever(nomisUserApiService.authenticateUser("ITAG_USER", "password")).thenReturn(true)
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        accountNonLocked = true,
        credentialsNonExpired = true,
        enabled = true
      )
    )

    val token = getPasswordCredentialsToken(convertToBase64("elite2apiclient", "clientsecret"))

    assertThat(JWT.decode(token).getClaim("grant_type").asString()).isEqualTo("password")
  }

  private fun convertToBase64(client: String, secret: String): String =
    Base64.getEncoder().encodeToString("$client:$secret".toByteArray())

  private fun getClientCredentialsTokenWithUsername(encodedClientAndSecret: String, username: String): String {
    val result =
      webTestClient
        .post().uri("/oauth/token?grant_type=client_credentials&username=$username")
        .header("Authorization", "Basic $encodedClientAndSecret")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>().returnResult()

    return JSONObject(result.responseBody).get("access_token") as String
  }

  private fun getClientCredentialsToken(encodedClientAndSecret: String): String {
    val result =
      webTestClient
        .post().uri("/oauth/token?grant_type=client_credentials")
        .header("Authorization", "Basic $encodedClientAndSecret")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>().returnResult()

    return JSONObject(result.responseBody).get("access_token") as String
  }

  private fun getPasswordCredentialsToken(encodedClientAndSecret: String): String {
    val result =
      webTestClient
        .post().uri("/oauth/token?grant_type=password&username=ITAG_USER&password=password")
        .header("Authorization", "Basic $encodedClientAndSecret")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>().returnResult()

    return JSONObject(result.responseBody).get("access_token") as String
  }

  private fun getAccessAndRefreshTokens(
    encodedClientAndSecret: String,
    username: String,
    password: String
  ): Pair<String, String> {
    val result =
      webTestClient
        .post().uri("/oauth/token?grant_type=password&username=$username&password=$password")
        .header("Authorization", "Basic $encodedClientAndSecret")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>().returnResult()

    val accessToken = JSONObject(result.responseBody).get("access_token") as String
    val refreshToken = JSONObject(result.responseBody).get("refresh_token") as String
    return Pair(accessToken, refreshToken)
  }
}
