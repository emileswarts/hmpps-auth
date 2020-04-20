package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HealthDeliusDownIntTest : IntegrationTest() {
  @Test
  fun `Health reports delius info`() {
    webTestClient.get().uri("/auth/health/deliusApiHealth")
        .exchange()
        .expectBody().jsonPath("status").isEqualTo("UP")
        .jsonPath("details.error").value<String> {
          assertThat(it).contains("I/O error on GET request for")
        }
  }
}
