package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource

class TokenControllerIntTest : IntegrationTest() {

  @Autowired
  private lateinit var repository: UserRepository

  @Autowired
  private lateinit var userTokenRepository: UserTokenRepository

  @Test
  fun `CreateToken based on user details provided`() {

    val tokenFromRestEndPoint = webTestClient
      .post().uri("/api/new-token")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .body(
        BodyInserters.fromValue(
          CreateTokenRequest(
            username = "joe",
            email = "joe@gov.uk",
            source = AuthSource.nomis
          )
        )
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val retrievedEntity = repository.findByUsername("JOE")

    assertThat(retrievedEntity).hasValueSatisfying {
      assertThat(it.username).isEqualTo("JOE")
      assertThat(it.email).isEqualTo("joe@gov.uk")
      assertThat(it.source).isEqualTo(AuthSource.nomis)
      val tokenObj = userTokenRepository.findByUserId(it.id)
      assertThat(tokenObj.get().token).isEqualTo(tokenFromRestEndPoint)
    }
  }

  @Test
  fun `Return token of existing user`() {

    val token = webTestClient
      .post().uri("/api/new-token")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .body(
        BodyInserters.fromValue(
          CreateTokenRequest(
            username = "bill",
            email = "bill@gov.uk",
            source = AuthSource.nomis
          )
        )
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val retrievedEntity = repository.findByUsername("BILL")

    assertThat(retrievedEntity).hasValueSatisfying {
      assertThat(it.username).isEqualTo("BILL")
      assertThat(it.email).isEqualTo("bill@gov.uk")
      assertThat(it.source).isEqualTo(AuthSource.nomis)
    }

    // Request token with same user details
    val retrieveToken = webTestClient
      .post().uri("/api/new-token")
      .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
      .body(
        BodyInserters.fromValue(
          CreateTokenRequest(
            username = "bill",
            email = "bill@gov.uk",
            source = AuthSource.nomis
          )
        )
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val retrievedSameEntity = repository.findByUsername("BILL")
    assertThat(retrievedSameEntity).hasValueSatisfying {
      assertThat(it.username).isEqualTo("BILL")
      assertThat(it.email).isEqualTo("bill@gov.uk")
    }
    // Should return same token for the
    assertThat(token).isEqualTo(retrieveToken)
  }
}
