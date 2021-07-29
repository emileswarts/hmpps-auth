package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest

class AuthUserGroupsControllerIntTest : IntegrationTest() {

  private val invalidToken =
    "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJlZGYzOWQwMy03YmJkLTQ2ZGYtOTQ5Ny1mYzI2MDg2ZWIzYTgiLCJzdWIiOiJJVEFHX1VTRVJfQURNIiwidXNlcl9uYW1lIjoiSVRBR19VU0VSX0FETSIsImNsaWVudF9pZCI6ImVsaXRlMmFwaWNsaWVudCIsImF1dGhvcml0aWVzIjpbXSwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImV4cCI6MTYwMzM2NjY0N30.Vi4z77ylpS94ztVyEQoilkRuMDDDfvYVPblQRmUA5ACo3TF4-9NW2xE1Hm4hURwesayMs_apBrW2iAbPVtiTRC_TiMFApPXU-SoMadO5QcqKumXx_z2HfV_J_1eQKS0RJBxaz89xdeR2ilTTEmUyk38IulFJ0IVY2k65gCkQffKn6uE3K4NDBATQXbBwQZ7Soqr89fmsh-xym9JCA63AB_aU42S39sWl7OtUildrf9UgNv81rnOSs1eLDFdcmgztUSdac2hyX01u0vai51biz93-IBF5xdIdAInDmNktF9jwrYsindDu3LCiubrqGuK3MScZDB7A_OW5gHSfyCHmvw"

  @Test
  fun `Auth User Groups add group by userId endpoint adds a group to a user`() {

    callGetGroupsByUserId(userId = "7CA04ED7-8275-45B2-AFB4-4FF51432D1EC")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_2')]")
      .doesNotExist()

    webTestClient
      .put().uri("/api/authuser/id/7CA04ED7-8275-45B2-AFB4-4FF51432D1EC/groups/site_1_group_2")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isNoContent

    callGetGroupsByUserId(userId = "7CA04ED7-8275-45B2-AFB4-4FF51432D1EC")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_2')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_2", "groupName" to "Site 1 - Group 2"))
  }

  @Test
  fun `Auth User Groups remove group by userId endpoint removes a group from a user`() {
    callGetGroupsByUserId(userId = "7CA04ED7-8275-45B2-AFB4-4FF51432D1EC")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_1", "groupName" to "Site 1 - Group 1"))

    webTestClient
      .delete().uri("/api/authuser/id/7CA04ED7-8275-45B2-AFB4-4FF51432D1EC/groups/site_1_group_1")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isNoContent

    callGetGroupsByUserId(userId = "7CA04ED7-8275-45B2-AFB4-4FF51432D1EC")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .doesNotExist()
  }

  @Test
  fun `Auth User Groups add group by userId endpoint adds a group to a user - group manager`() {

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA030")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_2')]")
      .doesNotExist()

    webTestClient
      .put().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA030/groups/SITE_1_GROUP_2")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isNoContent

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA030")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_2')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_2", "groupName" to "Site 1 - Group 2"))
  }

  @Test
  fun `Auth User Groups remove group by userId endpoint removes a group from a user - group manager`() {
    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02F")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_1", "groupName" to "Site 1 - Group 1"))

    webTestClient
      .delete().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA02F/groups/SITE_1_GROUP_1")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isNoContent

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02F")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .doesNotExist()
  }

  @Test
  fun `Auth User Groups add group by userId endpoint does not add group if group Manager not member of group`() {

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02C")
      .jsonPath(".[?(@.groupCode == 'PECS_DRB8')]")
      .doesNotExist()

    webTestClient
      .put().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA02C/groups/PECS_DRB8")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isBadRequest

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02C")
      .jsonPath(".[?(@.groupCode == 'PECS_DRB8')]")
      .doesNotExist()
  }

  @Test
  fun `Auth User Groups add group by userId endpoint does not add group if user not in group managers groups`() {

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA44A")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .doesNotExist()

    webTestClient
      .put().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA44A/groups/SITE_1_GROUP_1")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json(
        """
      {"error":"User not with your groups","error_description":"Unable to maintain user: Ryan-Auth Orton4 with reason: User not with your groups","field":"username"}
        """.trimIndent()
      )

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA44A")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .doesNotExist()
  }

  @Test
  fun `Auth User Groups remove group by userId endpoint does not remove group if group Manager not member of group`() {
    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02C")
      .jsonPath(".[?(@.groupCode == 'GC_DEL_4')]")
      .isEqualTo(mapOf("groupCode" to "GC_DEL_4", "groupName" to "Group 4 for deleting"))

    webTestClient
      .delete().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA02C/groups/GC_DEL_4")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isBadRequest

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA02C")
      .jsonPath(".[?(@.groupCode == 'GC_DEL_4')]")
      .isEqualTo(mapOf("groupCode" to "GC_DEL_4", "groupName" to "Group 4 for deleting"))
  }

  @Test
  fun `Auth User Groups remove group By userId endpoint does not remove group if group Manager and users last group`() {
    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA44B")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_1", "groupName" to "Site 1 - Group 1"))

    webTestClient
      .delete().uri("/api/authuser/id/90F930E1-2195-4AFD-92CE-0EB5672DA44B/groups/SITE_1_GROUP_1")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isForbidden

    callGetGroupsByUserId(userId = "90F930E1-2195-4AFD-92CE-0EB5672DA44B")
      .jsonPath(".[?(@.groupCode == 'SITE_1_GROUP_1')]")
      .isEqualTo(mapOf("groupCode" to "SITE_1_GROUP_1", "groupName" to "Site 1 - Group 1"))
  }

  @Test
  fun `Auth User Groups endpoint returns user groups no children`() {
    webTestClient
      .get().uri("/api/authuser/auth_ro_vary_user/groups?children=false")
      .headers(setAuthorisation("ITAG_USER_ADM"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"SITE_1_GROUP_2","groupName":"Site 1 - Group 2"}       
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups endpoint returns user groups with children by default`() {
    webTestClient
      .get().uri("/api/authuser/auth_ro_vary_user/groups")
      .headers(setAuthorisation("ITAG_USER_ADM"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"CHILD_1","groupName":"Child - Site 1 - Group 2"}
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups endpoint returns user groups with children`() {
    callGetGroups("auth_ro_vary_user", children = true)
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"CHILD_1","groupName":"Child - Site 1 - Group 2"}
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/authuser/auth_ro_vary_user/groups")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Auth User Groups by userId endpoint returns user groups no children - admin user`() {
    webTestClient
      .get().uri("/api/authuser/id/5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8/groups?children=false")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"SITE_1_GROUP_2","groupName":"Site 1 - Group 2"}       
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint returns user groups no children - group manager`() {
    webTestClient
      .get().uri("/api/authuser/id/5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8/groups?children=false")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"SITE_1_GROUP_2","groupName":"Site 1 - Group 2"}       
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint returns user groups with children by default - admin user`() {
    webTestClient
      .get().uri("/api/authuser/id/5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8/groups")
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"CHILD_1","groupName":"Child - Site 1 - Group 2"}
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint returns user groups with children by default - group manager`() {
    webTestClient
      .get().uri("/api/authuser/id/5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8/groups")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"CHILD_1","groupName":"Child - Site 1 - Group 2"}
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint returns forbidden - user not in group manager group`() {
    webTestClient
      .get().uri("/api/authuser/id/9E84F1E4-59C8-4B10-927A-9CF9E9A30792/groups")
      .headers(setAuthorisation("AUTH_GROUP_MANAGER", listOf("ROLE_AUTH_GROUP_MANAGER")))
      .exchange()
      .expectStatus().isForbidden
      .expectBody()
      .json(
        """
      {"error":"User not with your groups","error_description":"Unable to maintain user: Auth Expired with reason: User not with your groups","field":"username"}
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint returns user groups with children`() {
    callGetGroupsByUserId("5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8", children = true)
      .json(
        """[
          {"groupCode":"SITE_1_GROUP_1","groupName":"Site 1 - Group 1"},
          {"groupCode":"CHILD_1","groupName":"Child - Site 1 - Group 2"}
        ]
        """.trimIndent()
      )
  }

  @Test
  fun `Auth User Groups by userId endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/authuser/id/5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8/groups")
      .exchange()
      .expectStatus().isUnauthorized
  }

  private fun callGetGroups(user: String = "AUTH_RO_USER", children: Boolean = false): BodyContentSpec = webTestClient
    .get().uri("/api/authuser/$user/groups?children=$children")
    .headers(setAuthorisation("ITAG_USER_ADM"))
    .exchange()
    .expectStatus().isOk
    .expectBody()

  private fun callGetGroupsByUserId(
    userId: String = "7CA04ED7-8275-45B2-AFB4-4FF51432D1EA",
    children: Boolean = false
  ): BodyContentSpec = webTestClient
    .get().uri("/api/authuser/id/$userId/groups?children=$children")
    .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
    .exchange()
    .expectStatus().isOk
    .expectBody()
}
