@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class RolesControllerIntTest : IntegrationTest() {

  @Nested
  inner class ManageRoles {

    @Test
    fun `Manage Roles endpoint not accessible without valid token`() {
      webTestClient.get().uri("/api/roles")
        .exchange()
        .expectStatus().isUnauthorized
    }

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
        .json("manage_roles.json".readFile())
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

  @Nested
  inner class RoleDetails {

    @Test
    fun `Role details endpoint not accessible without valid token`() {
      webTestClient.get().uri("/api/roles/ANY_ROLE")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Role details endpoint returns forbidden when does not have admin role`() {
      webTestClient
        .get().uri("/api/roles/ANY_ROLE")
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
    fun `Role details endpoint returns error when role does not exist`() {
      webTestClient
        .get().uri("/api/roles/ROLE_DOES_NOT_EXIST")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          Assertions.assertThat(it).containsExactlyInAnyOrderEntriesOf(
            mapOf(
              "error" to "Not Found",
              "error_description" to "Unable to get role: ROLE_DOES_NOT_EXIST with reason: notfound",
              "field" to "role"
            )
          )
        }
    }

    @Test
    fun `Role details endpoint returns success`() {
      webTestClient
        .get().uri("/api/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("role_details.json".readFile())
    }
  }
}
