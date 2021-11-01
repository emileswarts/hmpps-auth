package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class NomisUserApiService(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
) {
  fun changePassword(username: String, password: String) {
    webClient.put().uri("/users/{username}/change-password", username)
      .bodyValue(password)
      .retrieve()
      .toBodilessEntity()
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
