package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class AzureUserControllerIntTest : IntegrationTest() {

  @Test
  fun `Azure User endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/azureuser/2E285CED-DCFD-4497-9E22-89E8E10A2A6A")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Azure User endpoint not not found if doesnt exist`() {
    webTestClient
      .get().uri("/api/azureuser/12345678-1234-5678-9abc-123456789abc")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Azure User endpoint returns user data`() {
    webTestClient
      .get().uri("/api/azureuser/2E285CED-DCFD-4497-9E22-89E8E10A2A6A")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json("azure_user_data.json".readFile())
  }
}
