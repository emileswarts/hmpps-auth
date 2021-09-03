@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class RolesControllerIntTest : IntegrationTest() {

  @Nested
  inner class ManageRoles {

    @Test
    fun `Manage Roles endpoint returns forbidden when does not have admin role `() {
      webTestClient
        .get().uri("/api/roles")
        .headers(setAuthorisation("bob"))
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json(
          """
      {"error":"access_denied","error_description":"Access is denied"}
          """.trimIndent()
        )
    }

    @Test
    fun `Manage Roles endpoint returns (default size=10) roles when user has role ROLE_ROLES_ADMIN`() {
      webTestClient
        .get().uri("/api/roles")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("manage_roles_data.json".readFile())
    }
  }

  @Nested
  inner class ManageRolesPaging {
    @Test
    fun `find page of roles with default sort`() {
      webTestClient.get().uri("/api/roles?page=0&size=3")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[2].roleName").isEqualTo("Auth Client Management (admin)")
        .jsonPath("$.content[2].roleCode").isEqualTo("OAUTH_ADMIN")
    }

    @Test
    fun `find page of roles sorting by role code`() {
      webTestClient.get().uri("/api/roles?page=4&size=3&sort=roleCode")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[2].roleName")
        .isEqualTo("Auth Client Management (admin)")
        .jsonPath("$.content[2].roleCode").isEqualTo("OAUTH_ADMIN")
    }

    @Test
    fun `find page of roles sorting by role code descending`() {
      webTestClient.get().uri("/api/roles?page=7&size=3&sort=roleCode,desc")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[0].roleName").isEqualTo("Auth Client Management (admin)")
        .jsonPath("$.content[0].roleCode").isEqualTo("OAUTH_ADMIN")
    }

    private fun WebTestClient.BodyContentSpec.assertPageOfMany() =
      this.jsonPath("$.content.length()").isEqualTo(3)
        .jsonPath("$.size").isEqualTo(3)
        .jsonPath("$.totalElements").isEqualTo(36)
        .jsonPath("$.totalPages").isEqualTo(12)
        .jsonPath("$.last").isEqualTo(false)
  }
}
