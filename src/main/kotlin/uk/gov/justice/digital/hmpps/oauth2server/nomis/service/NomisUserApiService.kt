package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class NomisUserApiService(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
) {
  fun changePassword(username: String, password: String) {
    webClient.put().uri("/{username}/change-password", username)
      .bodyValue(password)
      .retrieve()
      .toBodilessEntity()
      .block()
  }
}
