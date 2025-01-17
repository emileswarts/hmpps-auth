@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters.fromValue
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class RolesControllerIntTest : IntegrationTest() {

  @Autowired
  private lateinit var roleRepository: RoleRepository

  @Nested
  inner class CreateRole {

    @Test
    fun `Create role`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "RC",
              "roleName" to " New role",
              "roleDescription" to "New role description",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      val role = roleRepository.findByRoleCode("RC")
      if (role != null) {
        roleRepository.delete(role)
      }
    }

    @Test
    fun `Create role passes regex validation`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "RC",
              "roleName" to "good's & Role(),.-",
              "roleDescription" to "good's & Role(),.-lineone\r\nlinetwo",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      val role = roleRepository.findByRoleCode("RC")
      if (role != null) {
        roleRepository.delete(role)
      }
    }

    @Test
    fun `Create role with empty role description`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "RC",
              "roleName" to " New role",
              "roleDescription" to "",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      val role = roleRepository.findByRoleCode("RC")
      if (role != null) {
        roleRepository.delete(role)
      }
    }

    @Test
    fun `Create role with no role description`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "RC",
              "roleName" to " New role",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      val role = roleRepository.findByRoleCode("RC")
      if (role != null) {
        roleRepository.delete(role)
      }
    }

    @Test
    fun `Create role error`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "",
              "roleName" to "",
              "adminType" to listOf<String>()
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "createRole"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleCode],30,2]")
          assertThat(it["error_description"] as String).contains("default message [roleName],128,4]")
        }
    }

    @Test
    fun `Create role length too long`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "x".repeat(30) + "x",
              "roleName" to "x".repeat(128) + "y",
              "roleDescription" to "x".repeat(1024) + "y",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "createRole"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleCode],30,2]")
          assertThat(it["error_description"] as String).contains("default message [roleName],128,4]")
          assertThat(it["error_description"] as String).contains("default message [roleDescription],1024,0]")
        }
    }

    @Test
    fun `Create role admin type empty`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLE_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "xxxx", "roleName" to "123456", "adminType" to listOf<String>()
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "createRole"
            )
          )
          assertThat(it["error_description"] as String)
            .contains("default message [Admin type cannot be empty]")
        }
    }

    @Test
    fun `Create role failed regex`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "a-b",
              "roleName" to "a\$here",
              "roleDescription" to "a\$description",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "createRole"
            )
          )
          assertThat(it["error_description"] as String)
            .contains("default message [roleCode],[Ljavax.validation.constraints.Pattern")
          assertThat(it["error_description"] as String)
            .contains("default message [roleName],[Ljavax.validation.constraints.Pattern")
          assertThat(it["error_description"] as String)
            .contains("default message [roleDescription],[Ljavax.validation.constraints.Pattern")
        }
    }

    @Test
    fun `Create role endpoint returns forbidden when dose not have admin role `() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("bob"))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "ROLE3",
              "roleName" to " role 3",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
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
    fun `Create role - role already exists`() {
      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "ROLE3",
              "roleName" to " role 3",
              "roleDescription" to "New role description",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient
        .post().uri("/api/roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleCode" to "ROLE3",
              "roleName" to " role 3",
              "roleDescription" to "New role description",
              "adminType" to listOf("EXT_ADM")
            )
          )
        )
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsExactlyInAnyOrderEntriesOf(
            mapOf(
              "error" to "role code already exists",
              "error_description" to "Unable to create role: ROLE3 with reason: role code already exists",
              "field" to "role"
            )
          )
        }

      val role = roleRepository.findByRoleCode("ROLE3")
      if (role != null) {
        roleRepository.delete(role)
      }
    }

    @Test
    fun `Create role endpoint not accessible without valid token`() {
      webTestClient.post().uri("/api/roles")
        .exchange()
        .expectStatus().isUnauthorized
    }
  }
  @Nested
  inner class GetAllRoles {
    @Test
    fun `Get all Roles endpoint not accessible without valid token`() {
      webTestClient.get().uri("/api/roles")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Get all Roles endpoint returns forbidden when does not have correct role `() {
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
    fun `Get all Roles endpoint returns all roles if no adminType set`() {
      webTestClient
        .get().uri("/api/roles")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.[*].roleName").value<List<String>> {
          assertThat(it).hasSizeGreaterThanOrEqualTo(68)
        }
    }

    @Test
    fun `Get all Roles endpoint returns roles filter requested adminType`() {
      webTestClient
        .get().uri("/api/roles?adminTypes=DPS_LSA")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.[*].roleName").value<List<String>> { assertThat(it).hasSize(23) }
        .jsonPath("$[0].roleName").isEqualTo("Add Secure Case Notes")
        .jsonPath("$[0].roleCode").isEqualTo("ADD_SENSITIVE_CASE_NOTES")
        .jsonPath("$[0].adminType[0].adminTypeCode").isEqualTo("DPS_ADM")
        .jsonPath("$[0].adminType[1].adminTypeCode").isEqualTo("DPS_LSA")
        .jsonPath("$[0].adminType[2].adminTypeCode").doesNotExist()
    }

    @Test
    fun `Get all Roles endpoint returns roles filter requested multiple adminTypes`() {
      webTestClient
        .get().uri("/api/roles?adminTypes=EXT_ADM&adminTypes=DPS_LSA")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.[*].roleName").value<List<String>> {
          assertThat(it).hasSize(5)
        }
        .jsonPath("$[0].roleName").isEqualTo("Artemis user")
        .jsonPath("$[0].roleCode").isEqualTo("ARTEMIS_USER")
        .jsonPath("$[0].adminType[0].adminTypeCode").isEqualTo("DPS_ADM")
        .jsonPath("$[0].adminType[1].adminTypeCode").isEqualTo("DPS_LSA")
        .jsonPath("$[0].adminType[2].adminTypeCode").isEqualTo("EXT_ADM")
    }
  }

  @Nested
  inner class ManageRoles {

    @Test
    fun `Manage Roles endpoint not accessible without valid token`() {
      webTestClient.get().uri("/api/roles/paged")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Manage Roles endpoint returns forbidden when does not have admin role `() {
      webTestClient
        .get().uri("/api/roles/paged")
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
        .get().uri("/api/roles/paged")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("manage_roles.json".readFile())
    }

    @Test
    fun `Manage Roles endpoint returns roles filtered by requested roleCode`() {
      webTestClient
        .get().uri("/api/roles/paged?roleCode=GLOBAL")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("manage_roles2.json".readFile())
    }

    @Test
    fun `Manage Roles endpoint returns roles filter requested roleName when user has role ROLE_ROLES_ADMIN`() {
      webTestClient
        .get().uri("/api/roles/paged?roleName=admin")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("manage_roles3.json".readFile())
    }

    @Test
    fun `Manage Roles endpoint returns roles filter requested adminType when user has role ROLE_ROLES_ADMIN`() {
      webTestClient
        .get().uri("/api/roles/paged?adminTypes=DPS_LSA")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("manage_roles1.json".readFile())
    }

    @Test
    fun `Manage Roles endpoint returns roles filter requested multiple adminTypes when user has role ROLE_ROLES_ADMIN`() {
      webTestClient
        .get().uri("/api/roles/paged?adminTypes=DPS_ADM&adminTypes=DPS_LSA")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.content[0].roleName").isEqualTo("Add Secure Case Notes")
        .jsonPath("$.content[0].roleCode").isEqualTo("ADD_SENSITIVE_CASE_NOTES")
        .jsonPath("$.content[0].adminType[0].adminTypeCode").isEqualTo("DPS_ADM")
        .jsonPath("$.content[0].adminType[1].adminTypeCode").isEqualTo("DPS_LSA")
    }

    @Test
    fun `Manage Roles endpoint returns roles filter requested when user has role ROLE_ROLES_ADMIN`() {
      webTestClient
        .get().uri("/api/roles/paged?roleName=add&roleCode=ADD_SENSITIVE_CASE_NOTES&adminTypes=DPS_ADM&adminTypes=DPS_LSA")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.content[0].roleName").isEqualTo("Add Secure Case Notes")
        .jsonPath("$.content[0].roleCode").isEqualTo("ADD_SENSITIVE_CASE_NOTES")
        .jsonPath("$.content[0].adminType[0].adminTypeCode").isEqualTo("DPS_ADM")
        .jsonPath("$.content[0].adminType[1].adminTypeCode").isEqualTo("DPS_LSA")
        .jsonPath("$.content[0].adminType[2].adminTypeCode").doesNotExist()
        .jsonPath("$.totalElements").isEqualTo(1)
    }
  }

  @Nested
  inner class ManageRolesPaging {
    @Test
    fun `find page of roles with default sort`() {
      webTestClient.get().uri("/api/roles/paged?page=0&size=3")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[2].roleName").isEqualTo("Approve Category assessments")
        .jsonPath("$.content[2].roleCode").isEqualTo("APPROVE_CATEGORISATION")
    }

    @Test
    fun `find page of roles sorting by role code`() {
      webTestClient.get().uri("/api/roles/paged?page=5&size=3&sort=roleCode")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[1].roleName").isEqualTo("HWPV Band 5")
        .jsonPath("$.content[1].roleCode").isEqualTo("HWPV_CASEWORK_MANAGER_BAND_5")
    }

    @Test
    fun `find page of roles sorting by role code descending`() {
      webTestClient.get().uri("/api/roles/paged?page=7&size=3&sort=roleCode,desc")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .assertPageOfMany()
        .jsonPath("$.content[1].roleName").isEqualTo("PECS Person Escort Record Author")
        .jsonPath("$.content[1].roleCode").isEqualTo("PECS_PER_AUTHOR")
        .jsonPath("$.content[1].roleDescription").isEmpty
        .jsonPath("$.content[1].adminType[0].adminTypeCode").isEqualTo("EXT_ADM")
        .jsonPath("$.content[1].adminType[0].adminTypeName").isEqualTo("External Administrator")
    }

    private fun WebTestClient.BodyContentSpec.assertPageOfMany() =
      this.jsonPath("$.content.length()").isEqualTo(3)
        .jsonPath("$.size").isEqualTo(3)
        .jsonPath("$.totalElements").isEqualTo(73)
        .jsonPath("$.totalPages").isEqualTo(25)
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
          assertThat(it).containsExactlyInAnyOrderEntriesOf(
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

  @Nested
  inner class AmendRoleName {

    @Test
    fun `Change role name endpoint not accessible without valid token`() {
      webTestClient.put().uri("/api/roles/ANY_ROLE")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Change role name endpoint returns forbidden when does not have admin role `() {
      webTestClient
        .put().uri("/api/roles/ANY_ROLE")
        .headers(setAuthorisation("bob"))
        .body(fromValue(mapOf("roleName" to "new role name")))
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
    fun `Change role name returns error when role not found`() {
      webTestClient
        .put().uri("/api/roles/Not_A_Role")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleName" to "new role name")))
        .exchange()
        .expectStatus().isNotFound
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsExactlyInAnyOrderEntriesOf(
            mapOf(
              "error" to "Not Found",
              "error_description" to "Unable to maintain role: Not_A_Role with reason: notfound",
              "field" to "role"
            )
          )
        }
    }

    @Test
    fun `Change role name returns error when length too short`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleName" to "tim")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleNameAmendment"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleName],100,4]")
        }
    }

    @Test
    fun `Change role name returns error when length too long`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleName" to "12345".repeat(20) + "y",
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleNameAmendment"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleName],100,4]")
        }
    }

    @Test
    fun `Change role name failed regex`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleName" to "a\$here",
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleNameAmendment"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleName],[Ljavax.validation.constraints.Pattern")
        }
    }

    @Test
    fun `Change role name success`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleName" to "new role name")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Change role name passes regex validation`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleName" to "good's & Role(),.-"
            )
          )
        )
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  inner class AmendRoleDescription {

    @Test
    fun `Change role description endpoint not accessible without valid token`() {
      webTestClient.put().uri("/api/roles/ANY_ROLE/description")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Change role description endpoint returns forbidden when does not have admin role `() {
      webTestClient
        .put().uri("/api/roles/ANY_ROLE/description")
        .headers(setAuthorisation("bob"))
        .body(fromValue(mapOf("roleDescription" to "new role description")))
        .exchange()
        .expectStatus().isForbidden
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json(
          """
      {"error":"access_denied","error_description":"Access is denied"}
          """.trimIndent()
        )
    }

    @Test
    fun `Change role description returns error when role not found`() {
      webTestClient
        .put().uri("/api/roles/Not_A_Role/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleDescription" to "new role description")))
        .exchange()
        .expectStatus().isNotFound
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsExactlyInAnyOrderEntriesOf(
            mapOf(
              "error" to "Not Found",
              "error_description" to "Unable to maintain role: Not_A_Role with reason: notfound",
              "field" to "role"
            )
          )
        }
    }

    @Test
    fun `Change role description returns error when length too long`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleDescription" to "12345".repeat(205) + "y",
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleDescriptionAmendment"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleDescription],1024,0]")
        }
    }

    @Test
    fun `Change role description failed regex`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleDescription" to "a\$here",
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleDescriptionAmendment"
            )
          )
          assertThat(it["error_description"] as String).contains("default message [roleDescription],[Ljavax.validation.constraints.Pattern")
        }
    }

    @Test
    fun `Change role description success`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleDescription" to "new role description")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Change role description returns success for empty roleDescription`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleDescription" to "")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Change role description returns success for no role description`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("roleDescription" to null)))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Change role description passes regex validation`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/description")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(
          fromValue(
            mapOf(
              "roleDescription" to "good's & Role(),.-lineone\r\nlinetwo"
            )
          )
        )
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  inner class AmendRoleAdminType {

    @Test
    fun `Change role adminType endpoint not accessible without valid token`() {
      webTestClient.put().uri("/api/roles/ANY_ROLE/admintype")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Change role adminType endpoint returns forbidden when does not have admin role `() {
      webTestClient
        .put().uri("/api/roles/ANY_ROLE/admintype")
        .headers(setAuthorisation("bob"))
        .body(fromValue(mapOf("adminType" to listOf("DPS_ADM"))))
        .exchange()
        .expectStatus().isForbidden
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json(
          """
      {"error":"access_denied","error_description":"Access is denied"}
          """.trimIndent()
        )
    }

    @Test
    fun `Change role admin type returns error when role not found`() {
      webTestClient
        .put().uri("/api/roles/Not_A_Role/admintype")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("adminType" to listOf("DPS_ADM"))))
        .exchange()
        .expectStatus().isNotFound
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsExactlyInAnyOrderEntriesOf(
            mapOf(
              "error" to "Not Found",
              "error_description" to "Unable to maintain role: Not_A_Role with reason: notfound",
              "field" to "role"
            )
          )
        }
    }

    @Test
    fun `Change role adminType returns bad request for no admin type`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/admintype")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("adminType" to listOf<String>())))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "error" to "Bad Request",
              "field" to "roleAdminTypeAmendment"
            )
          )
          assertThat(it["error_description"] as String)
            .contains("default message [Admin type cannot be empty]")
        }
    }

    @Test
    fun `Change role admin type returns bad request when adminType does not exist`() {
      webTestClient
        .put().uri("/api/roles/OAUTH_ADMIN/admintype")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_ROLES_ADMIN")))
        .body(fromValue(mapOf("adminType" to listOf("DOES_NOT_EXIST"))))
        .exchange()
        .expectStatus().isBadRequest
        .expectHeader().contentType(APPLICATION_JSON)
    }
  }
}
