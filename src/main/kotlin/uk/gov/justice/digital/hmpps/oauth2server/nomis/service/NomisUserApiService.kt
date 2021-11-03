package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException

@Service
class NomisUserApiService(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  fun changePassword(username: String, password: String) {
    webClient.put().uri("/users/{username}/change-password", username)
      .bodyValue(password)
      .retrieve()
      .toBodilessEntity()
      .onErrorResume(WebClientResponseException::class.java) {
        errorWhenBadRequest(it) { content ->
          val error = objectMapper.readValue<PasswordChangeError>(content)
          when (error.errorCode) {
            1001 -> ReusedPasswordException()
            else -> PasswordValidationFailureException()
          }
        }
      }
      .block()
  }

  fun lockAccount(username: String) {
    webClient.put().uri("/users/{username}/lock-user", username)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun unlockAccount(username: String) {
    webClient.put().uri("/users/{username}/unlock-user", username)
      .retrieve()
      .toBodilessEntity()
      .block()
  }
}

fun <T> errorWhenBadRequest(
  exception: WebClientResponseException,
  errorMapper: (content: String) -> AuthenticationException
): Mono<T> =
  if (exception.rawStatusCode == BAD_REQUEST.value()) Mono.error(errorMapper(exception.responseBodyAsString)) else Mono.error(
    exception
  )

data class PasswordChangeError(val errorCode: Int? = 0)
