package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.time.LocalDateTime
import java.util.Optional

class HMPPSAccountsControllerTest : IntegrationTest() {

  @MockBean
  private lateinit var nomisUserApiService: NomisUserApiService

  @MockBean
  private lateinit var userService: UserService

  @MockBean
  private lateinit var deliusUserService: DeliusUserService

  @Test
  internal fun `must have a valid authentication token`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(PageImpl<NomisUserSummaryDto>(listOf()))

    webTestClient
      .get().uri("/api/accounts/multiple")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  internal fun `must have the correct role`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(PageImpl<NomisUserSummaryDto>(listOf()))

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_BANANAS")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  internal fun `will call nomis api and return no results if no users found`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        listOf(),
        Pageable.ofSize(200),
        0
      )
    )

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(0)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
  }

  @Test
  internal fun `will retrieve associated Auth user that has previously logged into Auth`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        listOf(
          NomisUserSummaryDto(
            username = "TIM.BEANS",
            staffId = "123",
            firstName = "TIM",
            lastName = "BEANS",
            active = true,
            activeCaseload = null,
            email = "tim.beans@justice.gov.uk",
          )
        ),
        Pageable.ofSize(200), 1
      )
    )

    whenever(userService.findUser(any())).thenReturn(Optional.empty())

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(userService).findUser("TIM.BEANS")
  }

  @Test
  internal fun `will not return user if not logged in for a long time`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        listOf(
          NomisUserSummaryDto(
            username = "TIM.BEANS",
            staffId = "123",
            firstName = "TIM",
            lastName = "BEANS",
            active = true,
            activeCaseload = null,
            email = "tim.beans@justice.gov.uk",
          )
        ),
        Pageable.ofSize(200), 1
      )
    )

    whenever(userService.findUser(any())).thenReturn(
      Optional.of(
        User(
          username = "TIM.BEANS",
          email = "tim.beans@justice.gov.uk",
          source = AuthSource.nomis
        ).apply {
          lastLoggedIn = LocalDateTime.now().minusYears(10)
        }
      )
    )

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(deliusUserService, never()).getDeliusUsersByEmail(any())
  }

  @Test
  internal fun `will not return user if user has no email address in Auth`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        listOf(
          NomisUserSummaryDto(
            username = "TIM.BEANS",
            staffId = "123",
            firstName = "TIM",
            lastName = "BEANS",
            active = true,
            activeCaseload = null,
            email = "tim.beans@justice.gov.uk",
          )
        ),
        Pageable.ofSize(200), 1
      )
    )

    whenever(userService.findUser(any())).thenReturn(
      Optional.of(
        User(
          username = "TIM.BEANS",
          email = null,
          source = AuthSource.nomis
        ).apply {
          lastLoggedIn = LocalDateTime.now().minusMinutes(10)
        }
      )
    )

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(deliusUserService, never()).getDeliusUsersByEmail(any())
  }

  @Test
  internal fun `will user when they have active accounts in both NOMIS and Delius`() {
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        listOf(
          NomisUserSummaryDto(
            username = "NOMIS.TIM.BEANS",
            staffId = "123",
            firstName = "TIM",
            lastName = "BEANS",
            active = true,
            activeCaseload = null,
            email = "tim.beans@justice.gov.uk",
          )
        ),
        Pageable.ofSize(200), 1
      )
    )

    whenever(userService.findUser(any())).thenReturn(
      Optional.of(
        User(
          username = "TIM.BEANS",
          email = "tim.beans@justice.gov.uk",
          source = AuthSource.nomis
        ).apply {
          lastLoggedIn = LocalDateTime.now().minusMinutes(10)
        }
      )
    )

    whenever(deliusUserService.getDeliusUsersByEmail(any())).thenReturn(
      listOf(
        DeliusUserPersonDetails(
          username = "DELIUS.TIM.BEANS",
          userId = "123",
          firstName = "Tim",
          surname = "Beans",
          email = "tim.beans@justice.gov.uk",
          enabled = true
        )
      )
    )

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(1)
      }
      .jsonPath("$[0].email").isEqualTo("tim.beans@justice.gov.uk")
      .jsonPath("$[0].accounts[0].accountType").isEqualTo("NOMIS")
      .jsonPath("$[0].accounts[0].username").isEqualTo("NOMIS.TIM.BEANS")
      .jsonPath("$[0].accounts[0].firstName").isEqualTo("TIM")
      .jsonPath("$[0].accounts[0].lastName").isEqualTo("BEANS")
      .jsonPath("$[0].accounts[1].accountType").isEqualTo("Delius")
      .jsonPath("$[0].accounts[1].username").isEqualTo("DELIUS.TIM.BEANS")
      .jsonPath("$[0].accounts[1].firstName").isEqualTo("Tim")
      .jsonPath("$[0].accounts[1].lastName").isEqualTo("Beans")
  }

  @Test
  internal fun `will page through all active NOMIS users`() {
    val twoHundredUsers = (1..200).map {
      NomisUserSummaryDto(
        username = "NOMIS.TIM.BEANS.$it",
        staffId = "123",
        firstName = "TIM",
        lastName = "BEANS",
        active = true,
        activeCaseload = null,
        email = "tim.beans@justice.gov.uk",
      )
    }
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        twoHundredUsers, Pageable.ofSize(200), 600
      )
    )

    whenever(userService.findUser(any())).thenReturn(
      Optional.empty()
    )

    webTestClient
      .get().uri("/api/accounts/multiple")
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(nomisUserApiService, times(2)).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(0)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(1)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(2)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
  }

  @Test
  internal fun `will page through select pages of active NOMIS users`() {
    val twoHundredUsers = (1..200).map {
      NomisUserSummaryDto(
        username = "NOMIS.TIM.BEANS.$it",
        staffId = "123",
        firstName = "TIM",
        lastName = "BEANS",
        active = true,
        activeCaseload = null,
        email = "tim.beans@justice.gov.uk",
      )
    }
    whenever(nomisUserApiService.findAllActiveUsers(any())).thenReturn(
      PageImpl<NomisUserSummaryDto>(
        twoHundredUsers, Pageable.ofSize(200), 200_000
      )
    )

    whenever(userService.findUser(any())).thenReturn(
      Optional.empty()
    )

    webTestClient
      .get()
      .uri { it.path("/api/accounts/multiple").queryParam("page", "20").queryParam("pageCount", "5").build() }
      .headers(setAuthorisation("SOME_USER", listOf("ROLE_ACCOUNT_RESEARCH")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$").value<List<Any>> {
        assertThat(it).hasSize(0)
      }

    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(0)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )

    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(20)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(21)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(22)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(23)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
    verify(nomisUserApiService).findAllActiveUsers(
      check {
        assertThat(it.pageNumber).isEqualTo(24)
        assertThat(it.pageSize).isEqualTo(200)
      }
    )
  }
}
