package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
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

  @Nested
  inner class CreateNewTokenByUserName {
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
              source = AuthSource.nomis,
              firstName = "Joe",
              lastName = "Smith",
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
              source = AuthSource.nomis,
              firstName = "Joe",
              lastName = "Smith"
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
              source = AuthSource.nomis,
              firstName = "Joe",
              lastName = "Smith"
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

  @Nested
  inner class CreateTokenByEmailType {

    @Test
    fun `CreateToken based on email type`() {

      val token = webTestClient
        .post().uri("/api/token/email-type")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
        .body(
          BodyInserters.fromValue(
            TokenByEmailTypeRequest(
              username = "joe",
              emailType = User.EmailType.PRIMARY
            )
          )
        )
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .returnResult().responseBody

      assertThat(token).isNotNull
    }

    @Test
    fun `Should fail with user not found for non-existing user`() {

      webTestClient
        .post().uri("/api/token/email-type")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
        .body(
          BodyInserters.fromValue(
            TokenByEmailTypeRequest(
              username = "joe11",
              emailType = User.EmailType.PRIMARY
            )
          )
        )
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .json(
          """
        {"error":"Not Found","error_description":"User with username joe11 not found","field":"username"} 
          """.trimIndent()
        )
    }
    @Test
    fun `New token endpoint not accessible without valid token`() {
      webTestClient.post().uri("/api/token/email-type")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Should fail with user not found for non-existing user1`() {

      webTestClient
        .post().uri("/api/token/email-type")
        .headers(setAuthorisation("AUTH_ADM", listOf("ROLE_CREATE_EMAIL_TOKEN")))
        .body(BodyInserters.fromValue(mapOf("username" to "joe", "emailType" to "INVALID_TYPE")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .json(
          """
        {"status":400,"error":"Bad Request","path":"/auth/api/token/email-type"} 
          """.trimIndent()
        )
    }
  }
}
