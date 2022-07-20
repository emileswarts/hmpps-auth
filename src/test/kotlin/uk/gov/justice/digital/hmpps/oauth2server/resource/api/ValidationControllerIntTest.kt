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

  @Test
  fun `Validate email`() {

    val isValid = webTestClient
      .get().uri("/api/validate/email?email=test@1careuk.com")
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
  fun `email validation should fail (together) for invalid email id`() {

    webTestClient
      .get().uri("/api/validate/email?email=test.@1careuk.com")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        Assertions.assertThat(it).containsAllEntriesOf(
          mapOf(
            "error" to "email.together",
            "error_description" to "Email address failed validation",
            "field" to "email"
          )
        )
      }
  }

  @Test
  fun `email validation should fail (multiple @) for invalid email id`() {
    webTestClient
      .get().uri("/api/validate/email?email=test@1careuk@.com")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        Assertions.assertThat(it).containsAllEntriesOf(
          mapOf(
            "error" to "email.together",
            "error_description" to "Email address failed validation",
            "field" to "email"
          )
        )
      }
  }

  @Test
  fun `email validation should fail with authorization error`() {
    webTestClient
      .get().uri("/api/validate/email?email=test@1careuk@.com")
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        Assertions.assertThat(it).containsAllEntriesOf(
          mapOf(
            "error" to "unauthorized",
            "error_description" to "Full authentication is required to access this resource"
          )
        )
      }
  }
}
