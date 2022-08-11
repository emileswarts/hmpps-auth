package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.resource.DeliusExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension

@ExtendWith(DeliusExtension::class, NomisExtension::class)
class UserControllerIntTest : IntegrationTest() {
  @MockBean
  private lateinit var nomisUserApiService: NomisUserApiService

  @Test
  fun `User Me endpoint returns principal user data for client credentials grant`() {
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "1",
        firstName = "Itag",
        surname = "User",
        activeCaseLoadId = "MDI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        enabled = true,
        staffStatus = "ACTIVE"
      )
    )

    webTestClient
      .get().uri("/api/user/me")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "ITAG_USER",
            "active" to true,
            "name" to "Itag User",
            "staffId" to 1,
            "activeCaseLoadId" to "MDI",
            "authSource" to "nomis",
            "userId" to "1",
            "uuid" to "a04c70ee-51c9-4852-8d0d-130da5c85c42",
          )
        )
      }
  }

  @Test
  fun `User Me endpoint returns principal user data for auth user`() {
    webTestClient
      .get().uri("/api/user/me")
      .headers(setAuthorisation("AUTH_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_USER",
            "active" to true,
            "name" to "Auth Only",
            "authSource" to "auth",
            "userId" to "608955ae-52ed-44cc-884c-011597a77949",
            "uuid" to "608955ae-52ed-44cc-884c-011597a77949",
          )
        )
      }
  }

  @Test
  fun `User Me endpoint returns principal user data for delius user`() {
    webTestClient
      .get().uri("/api/user/me")
      .headers(setAuthorisation("DELIUS"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "username" to "DELIUS",
            "active" to true,
            "name" to "Delius Smith",
            "authSource" to "delius",
            "userId" to "2500077027",
          )
        )
      }
  }

  @Test
  fun `User username endpoint returns user data`() {
    whenever(nomisUserApiService.findUserByUsername("RO_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "RO_USER",
        userId = "4",
        firstName = "Licence Responsible",
        surname = "Officer",
        activeCaseLoadId = "BEL",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        enabled = true,
        staffStatus = "ACTIVE"
      )
    )

    webTestClient
      .get().uri("/api/user/RO_USER")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "username" to "RO_USER",
            "active" to true,
            "name" to "Licence Responsible Officer",
            "authSource" to "nomis",
            "staffId" to 4,
            "activeCaseLoadId" to "BEL",
            "userId" to "4",
          )
        )
      }
  }

  @Test
  fun `User Me endpoint returns principal user data`() {
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "1",
        firstName = "Itag",
        surname = "User",
        activeCaseLoadId = "MDI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        enabled = true,
        staffStatus = "ACTIVE"
      )
    )

    webTestClient
      .get().uri("/api/user/ITAG_USER")
      .headers(setAuthorisation("ITAG_USER_ADM"))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json("user_data.json".readFile())
  }

  @Test
  fun `User username endpoint returns user data for auth user`() {
    webTestClient
      .get().uri("/api/user/AUTH_USER")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_USER",
            "active" to true,
            "name" to "Auth Only",
            "authSource" to "auth",
            "userId" to "608955ae-52ed-44cc-884c-011597a77949",
            "uuid" to "608955ae-52ed-44cc-884c-011597a77949",
          )
        )
      }
  }

  @Test
  fun `User username endpoint returns user data for delius user`() {
    webTestClient
      .get().uri("/api/user/me")
      .headers(setAuthorisation("delius"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "username" to "DELIUS",
            "active" to true,
            "name" to "Delius Smith",
            "authSource" to "delius",
            "userId" to "2500077027",
          )
        )
      }
  }

  @Test
  fun `My email endpoint returns user data for auth user`() {
    webTestClient
      .get().uri("/api/me/email")
      .headers(setAuthorisation("AUTH_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_USER",
            "email" to "auth_user@digital.justice.gov.uk",
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `User email endpoint returns user data for auth user`() {
    webTestClient
      .get().uri("/api/user/AUTH_USER/email")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_USER",
            "email" to "auth_user@digital.justice.gov.uk",
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `User email endpoint returns no content for unverified auth user`() {
    webTestClient
      .get().uri("/api/user/AUTH_UNVERIFIED/email")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isNoContent
  }

  @Test
  fun `User email endpoint returns unverified auth user email`() {
    webTestClient
      .get().uri("/api/user/AUTH_UNVERIFIED/email?unverified=true")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_UNVERIFIED",
            "email" to "auth_unverified@digital.justice.gov.uk",
            "verified" to false,
          )
        )
      }
  }

  @Test
  fun `My email endpoint returns unverified auth user email`() {
    webTestClient
      .get().uri("/api/me/email?unverified=true")
      .headers(setAuthorisation("AUTH_UNVERIFIED"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "AUTH_UNVERIFIED",
            "email" to "auth_unverified@digital.justice.gov.uk",
            "verified" to false,
          )
        )
      }
  }

  @Test
  fun `User email endpoint returns user data for nomis user`() {
    webTestClient
      .get().uri("/api/user/ITAG_USER/email")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "ITAG_USER",
            "email" to "itag_user@digital.justice.gov.uk",
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `User email endpoint returns empty for nomis user without email`() {
    whenever(nomisUserApiService.findUserByUsername("IEP_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "IEP_USER",
        userId = "1",
        firstName = "Itag",
        surname = "User",
        activeCaseLoadId = "MDI",
        email = null,
        accountStatus = AccountStatus.OPEN,
        staffStatus = "ACTIVE"
      )
    )

    webTestClient
      .get().uri("/api/user/IEP_USER/email?unverified=true")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "IEP_USER",
            "verified" to false,
          )
        )
      }
  }

  @Test
  fun `User email endpoint returns user data for delius user`() {
    webTestClient
      .get().uri("/api/user/delius_email/email")
      .headers(setAuthorisation("delius_email"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "username" to "DELIUS_EMAIL",
            "email" to "delius_user@digital.justice.gov.uk",
            "verified" to true,
          )
        )
      }
  }

  @Test
  fun `User emails endpoint returns user data`() {
    webTestClient
      .post().uri("/api/user/email")
      .body(BodyInserters.fromValue(listOf("AUTH_USER", "ITAG_USER", "delius_email", "DM_USER", "nobody")))
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_ACCESS_ROLES")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$[*].email").value<List<String>> {
        assertThat(it).containsExactlyInAnyOrderElementsOf(
          listOf(
            "auth_user@digital.justice.gov.uk",
            "itag_user@digital.justice.gov.uk",
            "delius_user@digital.justice.gov.uk",
          )
        )
      }
  }

  @Test
  fun `User emails endpoint returns user data forbidden`() {
    webTestClient
      .post().uri("/api/user/email")
      .body(BodyInserters.fromValue(listOf("AUTH_USER", "ITAG_USER", "delius_email", "DM_USER", "nobody")))
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `User email endpoint returns no user data for unverified email address`() {
    webTestClient
      .get().uri("/api/user/DM_USER/email")
      .headers(setAuthorisation("ITAG_USER"))
      .exchange()
      .expectStatus().isNoContent
  }

  @Test
  fun `User Me Roles endpoint returns principal user data`() {
    webTestClient
      .get().uri("/api/user/me/roles")
      .headers(
        setAuthorisation(
          "ITAG_USER",
          listOf("ROLE_MAINTAIN_ACCESS_ROLES", "ROLE_MAINTAIN_OAUTH_USERS", "ROLE_OAUTH_ADMIN")
        )
      )
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("MAINTAIN_OAUTH_USERS")
        assertThat(it).contains("OAUTH_ADMIN")
      }
  }

  @Test
  fun `User Me Roles endpoint returns principal user data for auth user`() {
    webTestClient
      .get().uri("/api/user/me/roles")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_GLOBAL_SEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("GLOBAL_SEARCH")
      }
  }

  @Test
  fun `User Me Roles endpoint returns principal user data for delius user`() {
    webTestClient
      .get().uri("/api/user/me/roles")
      .headers(setAuthorisation("DELIUS", listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("PROBATION")
      }
  }

  @Test
  fun `User Roles endpoint returns roles for nomis user`() {
    whenever(nomisUserApiService.findUserByUsername("ITAG_USER")).thenReturn(
      NomisUserPersonDetails(
        username = "ITAG_USER",
        userId = "1",
        firstName = "Itag",
        surname = "User",
        activeCaseLoadId = "MDI",
        email = "nomis@email",
        accountStatus = AccountStatus.OPEN,
        roles = setOf(SimpleGrantedAuthority("PRISON"), SimpleGrantedAuthority("GLOBAL_SEARCH")),
        staffStatus = "ACTIVE"
      )
    )

    webTestClient
      .get().uri("/api/user/ITAG_USER/roles")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_PCMS_USER_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("PRISON")
        assertThat(it).contains("GLOBAL_SEARCH")
      }
  }

  @Test
  fun `User Roles endpoint returns roles for auth user`() {
    webTestClient
      .get().uri("/api/user/AUTH_ADM/roles")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_PCMS_USER_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("OAUTH_ADMIN")
        assertThat(it).contains("MAINTAIN_OAUTH_USERS")
      }
  }

  @Test
  fun `User Roles endpoint returns roles for delius user`() {
    webTestClient
      .get().uri("/api/user/DELIUS/roles")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_PCMS_USER_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].roleCode").value<List<String>> {
        assertThat(it).contains("LICENCE_RO")
        assertThat(it).contains("PROBATION")
        assertThat(it).contains("GLOBAL_SEARCH")
      }
  }

  @Test
  fun `User Roles endpoint returns not found for unknown username`() {
    webTestClient
      .get().uri("/api/user/UNKNOWN/roles")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_PCMS_USER_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `User Me endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/user/me")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `User Me Roles endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/user/me/roles")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `User Roles endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/user/ITAG_USER/roles")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `User username endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/user/bob")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `User email endpoint not accessible without valid token`() {
    webTestClient
      .get().uri("/api/user/bob/email")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `User search by multiple auth sources`() {
    webTestClient
      .get()
      .uri("/api/user/search?name=test2&authSources=nomis&authSources=delius&authSources=auth&authSources=nomis")
      .headers(setAuthorisation("INTEL_ADMIN", listOf("ROLE_INTEL_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json("auth_user_search_multiple_source.json".readFile())
  }

  @Test
  fun `User search by the defaulted auth source`() {
    webTestClient
      .get().uri("/api/user/search?name=test2")
      .headers(setAuthorisation("INTEL_ADMIN", listOf("ROLE_INTEL_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json("auth_user_search_default_source.json".readFile())
  }

  @Test
  fun `User search by multiple auth sources and status filter`() {
    webTestClient
      .get()
      .uri("/api/user/search?name=test2&status=INACTIVE&authSources=nomis&authSources=delius")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_INTEL_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json("auth_user_search_multiple_source_inactive.json".readFile())
  }

  @Nested
  inner class GetAuthUsersAndEmails {

    @Test
    fun `User emails endpoint returns user data forbidden`() {
      webTestClient
        .get().uri("/api/users/email")
        .headers(setAuthorisation("ITAG_USER"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Find all user emails endpoint returns auth users by default`() {
      webTestClient
        .get().uri("/api/users/email")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].email").value<List<String>> {
          assertThat(it).containsAnyElementsOf(
            listOf("auth_user@digital.justice.gov.uk")
          )
          assertThat(it).doesNotContainAnyElementsOf(
            listOf("itag_user@digital.justice.gov.uk")
          )
        }
    }

    @Test
    fun `Find all User emails endpoint returns nomis users`() {
      webTestClient
        .get().uri("/api/users/email?authSource=nomis")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_OAUTH_USERS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].email").value<List<String>> {
          assertThat(it).containsAnyElementsOf(
            listOf(
              "itag_user@digital.justice.gov.uk"
            )
          )
          assertThat(it).doesNotContainAnyElementsOf(
            listOf("auth_user@digital.justice.gov.uk")
          )
        }
    }
  }

  @Test
  fun `User Me MFA endpoint returns mfa options that have been verified`() {
    webTestClient
      .get().uri("/api/user/me/mfa")
      .headers(setAuthorisation("AUTH_USER"))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsExactlyInAnyOrderEntriesOf(
          mapOf(
            "emailVerified" to true,
            "mobileVerified" to false,
            "backupVerified" to false,
          )
        )
      }
  }
}
