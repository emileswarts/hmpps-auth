package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.OPEN
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService

@ExtendWith(NomisExtension::class)
class PrisonUserControllerIntTest : IntegrationTest() {
  @MockBean
  private lateinit var telemetryClient: TelemetryClient
  @MockBean
  private lateinit var nomisUserApiService: NomisUserApiService
  @SpyBean
  private lateinit var verifyEmailService: VerifyEmailService

  @Test
  fun `Can request email sync`() {
    whenever(nomisUserApiService.findUserByUsername("SYNC_ME")).thenReturn(createSampleNomisUser())

    webTestClient
      .post().uri("/api/prisonuser/SYNC_ME/email/sync")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
      .exchange()
      .expectStatus().isOk

    verify(verifyEmailService).syncEmailWithNOMIS("SYNC_ME")
  }

  @Test
  fun `Can request email sync as local admin`() {
    whenever(nomisUserApiService.findUserByUsername("SYNC_ME")).thenReturn(createSampleNomisUser())

    webTestClient
      .post().uri("/api/prisonuser/SYNC_ME/email/sync")
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_ACCESS_ROLES")))
      .exchange()
      .expectStatus().isOk

    verify(verifyEmailService).syncEmailWithNOMIS("SYNC_ME")
  }
  @Test
  fun `Prison user end-point returns results`() {
    whenever(nomisUserApiService.findUsers("ryAn", "OrtoN")).thenReturn(
      listOf(
        NomisUserSummaryDto(
          "RO_USER_TEST", "11", "Ryan", "Orton", true, null, "nomis@email"
        )
      )
    )

    webTestClient
      .get().uri("/api/prisonuser?firstName=ryAn&lastName=OrtoN")
      .headers(setAuthorisation("UOF_REVIEWER_USER", listOf("ROLE_USE_OF_FORCE")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
        [{
          "username":"RO_USER_TEST",
          "staffId":11,
          "email":"ro_user_test@digital.justice.gov.uk",
          "verified":true,
          "firstName":"Ryan",
          "lastName":"Orton",
          "name":"Ryan Orton"
        }]
        """.trimIndent()
      )
  }

  @Test
  fun `Prison user end-point returns results when using STAFF_SEARCH role`() {
    whenever(nomisUserApiService.findUsers("ryAn", "OrtoN")).thenReturn(
      emptyList()
    )

    webTestClient
      .get().uri("/api/prisonuser?firstName=ryAn&lastName=OrtoN")
      .headers(setAuthorisation("UOF_REVIEWER_USER", listOf("ROLE_STAFF_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `Prison user end-point rejects invalid query`() {
    webTestClient
      .get().uri("/api/prisonuser")
      .headers(setAuthorisation("UOF_REVIEWER_USER", listOf("ROLE_USE_OF_FORCE")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
  }

  @Test
  fun `Prison user end-point rejects unauthorised request`() {
    webTestClient
      .get().uri("/api/prisonuser?firstName=ryAn&lastName=OrtoN")
      .headers(setAuthorisation("UOF_REVIEWER_USER", listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
  }

  @Test
  fun `Prison user email endpoint fails if no email specified`() {
    webTestClient
      .post().uri("/api/prisonuser/NOMIS_EMAIL_TEST/email")
      .body(BodyInserters.fromValue(mapOf("email" to "  ")))
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$").value<Map<String, Any>> {
        assertThat(it).containsAllEntriesOf(
          mapOf(
            "error" to "Bad Request",
          )
        )
        assertThat(it["error_description"] as String).contains("Email must not be blank")
      }
  }

  @Test
  fun `Prison user email endpoint fails if no privilege`() {
    webTestClient
      .post().uri("/api/prisonuser/NOMIS_EMAIL_TEST/email")
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
  fun `Prison user email endpoint succeeds to alter user email`() {
    whenever(nomisUserApiService.findUserByUsername("NOMIS_EMAIL_TEST")).thenReturn(
      NomisUserPersonDetails(
        username = "NOMIS_EMAIL_TEST",
        userId = "47",
        firstName = "Nomis",
        surname = "Email Test",
        activeCaseLoadId = "BXI",
        email = "nomis@email",
        accountStatus = OPEN,
        staffStatus = "ACTIVE",
      )
    )

    // calling email endpoint will automatically add the user to auth database
    webTestClient
      .post().uri("/api/user/email")
      .body(BodyInserters.fromValue(listOf("NOMIS_EMAIL_TEST")))
      .headers(setAuthorisation("ITAG_USER", listOf("ROLE_MAINTAIN_ACCESS_ROLES")))
      .exchange()
      .expectStatus().isOk

    webTestClient
      .post().uri("/api/prisonuser/NOMIS_EMAIL_TEST/email")
      .body(BodyInserters.fromValue(mapOf("email" to "bobby.b@digital.justice.gov.uk")))
      .headers(setAuthorisation("ITAG_USER_ADM", listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
      .exchange()
      .expectStatus().isOk

    verify(telemetryClient).trackEvent(
      "VerifyEmailRequestSuccess",
      mapOf("username" to "NOMIS_EMAIL_TEST"), null
    )
    verify(notificationClient).sendEmail(
      eq("4b285841-41ba-47d1-b6b0-104440c4e312"),
      eq("bobby.b@digital.justice.gov.uk"),
      check {
        assertThat(it).containsAllEntriesOf(
          mapOf("firstName" to "Nomis", "fullName" to "Nomis Email Test")
        )
        assertThat(it["verifyLink"] as String).contains("/auth/verify-email-confirm?token")
      },
      isNull()
    )
  }
}
