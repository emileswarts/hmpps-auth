package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class AuthServicesIntTest : IntegrationTest() {
  @Test
  fun `Auth Services endpoint returns all possible enabled services`() {
    webTestClient
      .get().uri("/api/services")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("[?(@.code == 'prison-staff-hub')]")
      .isEqualTo(
        mapOf(
          "code" to "prison-staff-hub",
          "name" to "Digital Prison Service",
          "description" to "View and Manage Offenders in Prison (Old name was NEW NOMIS)",
          "contact" to "feedback@digital.justice.gov.uk",
          "url" to "http://localhost:3000"
        )
      )
      .jsonPath("[*].code").value<List<String>> {
        assertThat(it).hasSizeGreaterThan(5)
      }
  }

  @Test
  fun `Auth Services endpoint returns my possible enabled services`() {
    webTestClient
      .get().uri("/api/services/me")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_PRISON")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("[?(@.code == 'prison-staff-hub')]")
      .isEqualTo(
        mapOf(
          "code" to "prison-staff-hub",
          "name" to "Digital Prison Service",
          "description" to "View and Manage Offenders in Prison (Old name was NEW NOMIS)",
          "contact" to "feedback@digital.justice.gov.uk",
          "url" to "http://localhost:3000"
        )
      )
      .jsonPath("[*].code").value<List<String>> {
        assertThat(it).containsExactlyInAnyOrder("prison-staff-hub", "DETAILS")
      }
  }

  @Test
  fun `Auth Roles endpoint accessible without valid token`() {
    webTestClient.get().uri("/api/services")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].code").value<List<String>> {
        assertThat(it).hasSizeGreaterThan(5)
      }
  }

  @Test
  fun `Auth Roles Me endpoint inaccessible without valid token`() {
    webTestClient.get().uri("/api/services/me")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Nested
  inner class ServiceByCode {

    @Test
    fun `Not accessible without valid token`() {
      webTestClient.get().uri("/api/services/book-a-secure-move-ui")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Not accessible without correct role`() {
      webTestClient.get().uri("/api/services/book-a-secure-move-ui")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_PRISON")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Responds with not found when service does not exist`() {
      webTestClient.get().uri("/api/services/service-not-found")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Responds with service details when exists`() {
      webTestClient.get().uri("/api/services/book-a-secure-move-ui")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("code").isEqualTo("book-a-secure-move-ui")
        .jsonPath("name").isEqualTo("Book a secure move")
        .jsonPath("description").isEqualTo("Book a secure move")
        .jsonPath("contact").isEqualTo("bookasecuremove@digital.justice.gov.uk")
        .jsonPath("url").isEqualTo("https://bookasecuremove.service.justice.gov.uk")
    }
  }
}
