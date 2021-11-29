package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException

@Service
class NomisUserApiService(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
  @Qualifier("nomisUserWebClient") private val nomisUserWebClient: WebClient,
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

  fun findUsersByEmailAddress(emailAddress: String): List<NomisApiUserPersonDetails> {
    val userDetails = webClient.get().uri {
      it.path("/users/user")
        .queryParam("email", emailAddress)
        .build()
    }
      .retrieve()
      .bodyToMono(NomisUserList::class.java)
      .block()
    return userDetails.map(::mapUserDetailsToNomisUser)
  }

  fun findUsers(firstName: String, lastName: String): List<NomisUserSummaryDto> {
    return nomisUserWebClient.get().uri {
      it.path("/users/staff")
        .queryParam("firstName", firstName)
        .queryParam("lastName", lastName)
        .build()
    }
      .retrieve()
      .bodyToMono(NomisUserSummaryList::class.java)
      .block()!!
  }
}

class NomisUserList : MutableList<NomisApiUserDetails> by ArrayList()

class NomisUserSummaryList : MutableList<NomisUserSummaryDto> by ArrayList()

data class NomisUserSummaryDto(
  val username: String,
  val staffId: String,
  val firstName: String,

  val lastName: String,
  val active: Boolean,
  val activeCaseload: PrisonCaseload?
)
data class PrisonCaseload(
  val id: String,
  val name: String
)

fun <T> errorWhenBadRequest(
  exception: WebClientResponseException,
  errorMapper: (content: String) -> AuthenticationException
): Mono<T> =
  if (exception.rawStatusCode == BAD_REQUEST.value()) Mono.error(errorMapper(exception.responseBodyAsString)) else Mono.error(
    exception
  )

data class PasswordChangeError(val errorCode: Int? = 0)

private fun mapUserDetailsToNomisUser(userDetails: NomisApiUserDetails): NomisApiUserPersonDetails =
  NomisApiUserPersonDetails(
    username = userDetails.username.uppercase(),
    userId = userDetails.staffId,
    firstName = userDetails.firstName,
    surname = userDetails.surname,
    email = userDetails.email.lowercase(),
    enabled = userDetails.enabled,
    roles = userDetails.roles.map { roleCode -> SimpleGrantedAuthority(roleCode) }
  )
