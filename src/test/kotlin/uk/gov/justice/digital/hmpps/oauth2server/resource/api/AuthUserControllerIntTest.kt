package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class AuthUserControllerIntTest : IntegrationTest() {

  @Test
  fun `Auth Admin can get users details by userId`() {
    webTestClient
      .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75255",
            "username" to "AUTH_STATUS",
            "email" to null,
            "firstName" to "Auth",
            "lastName" to "Status",
            "locked" to false,
            "enabled" to false,
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `Group Manager can get users details by userId`() {
    webTestClient
      .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75299")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75299",
            "username" to "AUTH_STATUS5",
            "email" to null,
            "firstName" to "Auth",
            "lastName" to "Status5",
            "locked" to false,
            "enabled" to false,
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `Group manager get user by userId endpoint fails user not in group manager group forbidden`() {
    webTestClient
      .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json(
        """
      {"error":"User not with your groups","error_description":"Unable to maintain user: Auth Status with reason: User not with your groups","field":"username"}
        """.trimIndent()
      )
  }

  @Test
  fun `get user Details by userId endpoint fails is not an admin user`() {
    webTestClient
      .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255")
      .headers(setAuthorisation("ITAG_USER", listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json(
        """
      {"error":"access_denied","error_description":"Access is denied"}
        """.trimIndent()
      )
  }

  @Nested
  inner class EnableUserByUserId {

    @Test
    fun `Auth User Enable endpoint enables user`() {
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255/enable")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75255",
              "username" to "AUTH_STATUS",
              "email" to null,
              "firstName" to "Auth",
              "lastName" to "Status",
              "locked" to false,
              "enabled" to true,
              "verified" to true,
            )
          )
        }
    }

    @Test
    fun `Group manager Enable endpoint enables user`() {
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75266/groups/site_1_group_2")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75266/enable")
        .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .get().uri("/api/authuser/AUTH_STATUS2")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75266",
              "username" to "AUTH_STATUS2",
              "email" to null,
              "firstName" to "Auth",
              "lastName" to "Status2",
              "locked" to false,
              "enabled" to true,
              "verified" to true,
            )
          )
        }
    }

    @Test
    fun `Group manager Enable endpoint fails user not in group manager group forbidden`() {
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75266/enable")
        .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json(
          """
      {"error":"User not with your groups","error_description":"Unable to maintain user: Auth Status2 with reason: User not with your groups","field":"username"}
          """.trimIndent()
        )
    }

    @Test
    fun `Auth User Enable endpoint fails is not an admin user`() {
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75266/enable")
        .headers(setAuthorisation("ITAG_USER", listOf()))
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
    fun `Auth User Enable by userId endpoint fails is not an admin user`() {
      webTestClient
        .put().uri("/api/authuser/id/FC494152-F9AD-48A0-A87C-9ADC8BD75255/enable")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json(
          """
            { "error":"access_denied", "error_description":"Access is denied" }
          """.trimIndent()
        )
    }
  }

  @Nested
  inner class DisableUserByUserId {
    @Test
    fun `Auth User Disable by userId endpoint disables user`() {
      val reason = DeactivateReason("left department")
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255/disable")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .bodyValue(reason)
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75255",
              "username" to "AUTH_STATUS",
              "email" to null,
              "firstName" to "Auth",
              "lastName" to "Status",
              "locked" to false,
              "enabled" to false,
              "verified" to true,
            )
          )
        }
    }

    @Test
    fun `Group manager Disable by userId endpoint enables user`() {
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75288/groups/site_1_group_2")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75288/enable")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isNoContent

      val reason = DeactivateReason("left department")
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75288/disable")
        .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .bodyValue(reason)
        .exchange()
        .expectStatus().isNoContent

      webTestClient
        .get().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75288")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<Map<String, Any>> {
          assertThat(it).containsAllEntriesOf(
            mapOf(
              "userId" to "fc494152-f9ad-48a0-a87c-9adc8bd75288",
              "username" to "AUTH_STATUS4",
              "email" to null,
              "firstName" to "Auth",
              "lastName" to "Status4",
              "locked" to false,
              "enabled" to false,
              "verified" to true,
            )
          )
        }
    }

    @Test
    fun `Group manager Disable by userId endpoint fails user not in group manager group forbidden`() {
      val reason = DeactivateReason("left department")
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75288/disable")
        .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .bodyValue(reason)
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json(
          """
      {"error":"User not with your groups","error_description":"Unable to maintain user: Auth Status4 with reason: User not with your groups","field":"username"}
          """.trimIndent()
        )
    }

    @Test
    fun `Auth User Disable by userId endpoint fails is not an admin user`() {
      val reason = DeactivateReason("left department")
      webTestClient
        .put().uri("/api/authuser/id/fc494152-f9ad-48a0-a87c-9adc8bd75255/disable")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(reason)
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json(
          """
            { "error":"access_denied", "error_description":"Access is denied" }
          """.trimIndent()
        )
    }
  }

  @Test
  fun `Amend User Email endpoint succeeds to alter user email`() {
    webTestClient
      .post().uri("/api/authuser/id/67A789DE-7D29-4863-B9C2-F2CE715DC4BC/email")
      .body(BodyInserters.fromValue(mapOf("email" to "bobby.b@digital.justice.gov.uk")))
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk

    webTestClient
      .get().uri("/api/authuser/id/67A789DE-7D29-4863-B9C2-F2CE715DC4BC")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "userId" to "67a789de-7d29-4863-b9c2-f2ce715dc4bc",
            "username" to "AUTH_NEW_USER",
            "email" to "bobby.b@digital.justice.gov.uk",
            "firstName" to "Auth",
            "lastName" to "New-User",
            "locked" to false,
            "enabled" to true,
            "verified" to false,
          )
        )
      }
  }

  @Test
  fun `Amend User Email endpoint fails to alter user email for user whose username is email address and email already taken`() {
    webTestClient
      .post().uri("/api/authuser/id/2E285CCD-DCFD-4497-9E24-D6E8E10A2D3F/email")
      .body(BodyInserters.fromValue(mapOf("email" to "auth_user_email_test@justice.gov.uk")))
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "error" to "email.duplicate",
            "error_description" to "Email address failed validation",
            "field" to "email"
          )
        )
      }
  }

  @Test
  fun `Amend User Email endpoint amends username as well as email address`() {
    webTestClient
      .post().uri("/api/authuser/id/2E285CCD-DCFD-4497-9E26-D6E8E10A2D3F/email")
      .body(BodyInserters.fromValue(mapOf("email" to "auth_user_email_test3@digital.justice.gov.uk")))
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk

    webTestClient
      .get().uri("/api/authuser/id/2E285CCD-DCFD-4497-9E26-D6E8E10A2D3F")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "userId" to "2e285ccd-dcfd-4497-9e26-d6e8e10a2d3f",
            "username" to "AUTH_USER_EMAIL_TEST3@DIGITAL.JUSTICE.GOV.UK",
            "email" to "auth_user_email_test3@digital.justice.gov.uk",
            "firstName" to "User",
            "lastName" to "Email Test",
            "locked" to false,
            "enabled" to true,
            "verified" to false,
          )
        )
      }
  }

  @Test
  fun `Amend User Email endpoint fails if no privilege`() {
    webTestClient
      .post().uri("/api/authuser/id/2E285CCD-DCFD-4497-9E26-D6E8E10A2D3F/email")
      .body(BodyInserters.fromValue(mapOf("email" to "bobby.b@digital.justice.gov.uk")))
      .headers(setAuthorisation("ITAG_USER_ADM"))
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
  fun `Auth User Assignable Groups endpoint for normal user returns their own groups`() {
    webTestClient
      .get().uri("/api/authuser/me/assignable-groups")
      .headers(setAuthorisation("AUTH_RO_VARY_USER", listOf("ROLE_AUTH_RO_VARY_USER")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """
       [{"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},{"groupCode":"SITE_1_GROUP_2","groupName":"Site 1 - Group 2"}]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Assignable Groups endpoint for super user returns all groups`() {
    webTestClient
      .get().uri("/api/authuser/me/assignable-groups")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.[*].groupCode").value<List<String>> {
        assertThat(it).hasSizeGreaterThan(5)
        assertThat(it).contains("SITE_1_GROUP_1")
        assertThat(it).contains("SITE_1_GROUP_2")
        assertThat(it).contains("SITE_2_GROUP_1")
        assertThat(it).contains("SITE_3_GROUP_1")
      }
  }

  @Nested
  inner class SearchableRoles {
    @Test
    fun `Searchable roles for group manager user returns their roles based on the groups they manage`() {
      webTestClient
        .get().uri("/api/authuser/me/searchable-roles")
        .headers(setAuthorisation("AUTH_GROUP_MANAGER2", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
       [{"roleCode":"PF_POLICE","roleName":"Pathfinder Police"}]
          """.trimIndent()
        )
    }

    @Test
    fun `Searchable roles for user with MAINTAIN_OAUTH_USERS role returns all roles excluding OAUTH_ADMIN`() {
      webTestClient
        .get().uri("/api/authuser/me/searchable-roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[*].roleCode").value<List<String>> {
          assertThat(it).hasSizeGreaterThan(22)
          assertThat(it).contains("MAINTAIN_OAUTH_USERS")
          assertThat(it).doesNotContain("OAUTH_ADMIN")
        }
    }

    @Test
    fun `Searchable roles for user with MAINTAIN_OAUTH_USERS and OAUTH_ADMIN role returns all roles`() {
      webTestClient
        .get().uri("/api/authuser/me/searchable-roles")
        .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS", "ROLE_OAUTH_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[*].roleCode").value<List<String>> {
          assertThat(it).hasSizeGreaterThan(23)
          assertThat(it).contains("AUTH_GROUP_MANAGER")
          assertThat(it).contains("OAUTH_ADMIN")
        }
    }

    @Test
    fun `Searchable roles for User without MAINTAIN_OAUTH_USERS role and has no groups will not return any roles`() {
      webTestClient
        .get().uri("/api/authuser/me/searchable-roles")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_AUTH_GROUP_MANAGER")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[*].roleCode").value<List<String>> {
          assertThat(it).hasSize(0)
        }
    }
  }
}
