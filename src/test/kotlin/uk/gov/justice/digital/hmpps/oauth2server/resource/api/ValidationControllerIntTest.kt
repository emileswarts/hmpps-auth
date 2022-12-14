package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class ValidationControllerIntTest : IntegrationTest() {

  @Test
  fun `Validate email domain`() {

    val isValid = webTestClient
      .get().uri("/api/validate/email-domain?emailDomain=careuk.com")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .exchange()
      .expectStatus().isOk
      .expectBody<Boolean>()
      .returnResult().responseBody

    if (isValid != null) {
      Assertions.assertThat(isValid).isEqualTo(true)
    }
  }

  @Test
  fun `Validate email domain matching existing domains`() {

    val isValid = webTestClient
      .get().uri("/api/validate/email-domain?emailDomain=1careuk.com")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .exchange()
      .expectStatus().isOk
      .expectBody<Boolean>()
      .returnResult().responseBody

    if (isValid != null) {
      Assertions.assertThat(isValid).isEqualTo(true)
    }
  }

  @Test
  fun `Should fail for invalid email domain`() {

    val isValid = webTestClient
      .get().uri("/api/validate/email-domain?emailDomain=invaliddomain.com")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .exchange()
      .expectStatus().isOk
      .expectBody<Boolean>()
      .returnResult().responseBody

    if (isValid != null) {
      Assertions.assertThat(isValid).isEqualTo(false)
    }
  }
}
