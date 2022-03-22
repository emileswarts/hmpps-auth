package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.Base64Utils

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["tokenverification.enabled=false"]
)
class ClientIpAllowListIntTest : IntegrationTest() {

  @Test
  fun `get token - ip allow list empty returns token`() {
    val username = "hmpps-manage-users-api"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `get token - localhost ip in allow list token return`() {
    val username = "max-duplicate-client"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `get token - ip in allow list token returned`() {
    val username = "max-duplicate-client-1"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .header("x-forwarded-for", "35.176.93.186")
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `get token - localhost ip not in allow list forbidden`() {
    val username = "max-duplicate-client-1"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json("""{"error": "access_denied", "error_description": "Unable to issue token as request is not from ip within allowed list"}""")
  }

  @Test
  fun `get token - ip not in allow list receives forbidden`() {
    val username = "max-duplicate-client-1"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .header("x-forwarded-for", "235.177.93.186")
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json("""{"error": "access_denied", "error_description": "Unable to issue token as request is not from ip within allowed list"}""")
  }

  @Test
  fun `get token - IP address using CIDR notation in allow list token returned`() {
    val username = "max-duplicate-client-2"
    val token = "clientsecret"
    webTestClient.post().uri("/oauth/token?grant_type=client_credentials")
      .header("Authorization", "Basic " + Base64Utils.encodeToString(("$username:$token").toByteArray()))
      .header("x-forwarded-for", "35.176.3.1")
      .exchange()
      .expectStatus().isOk
  }
}
