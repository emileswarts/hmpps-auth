package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.NomisUserServiceException
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException

@Service
class NomisUserApiService(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
  @Qualifier("nomisUserWebClient") private val nomisUserWebClient: WebClient,
  @Value("\${nomis.enabled:false}") private val nomisEnabled: Boolean,
  private val objectMapper: ObjectMapper,
) {
  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun changePassword(username: String, password: String) {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, not changing password for {}", username)
      return
    }
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

  fun changeEmail(username: String, email: String) {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, not changing email for {}", username)
      return
    }
    webClient.put().uri("/users/{username}/change-email", username)
      .bodyValue(email)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun lockAccount(username: String) {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, not locking account for {}", username)
      return
    }
    webClient.put().uri("/users/{username}/lock-user", username)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun unlockAccount(username: String) {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, not unlocking account for {}", username)
      return
    }
    webClient.put().uri("/users/{username}/unlock-user", username)
      .retrieve()
      .toBodilessEntity()
      .block()
  }

  fun findUsersByEmailAddressAndUsernames(emailAddress: String, usernames: Set<String>): List<NomisUserPersonDetails> {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, returning empty for {}", emailAddress)
      return listOf()
    }
    val userDetails = webClient.post().uri {
      it.path("/users/user")
        .queryParam("email", "{emailAddress}")
        .build(emailAddress)
    }
      .bodyValue(usernames)
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<NomisUserDetails>>() {})
      .block()!!
    return userDetails.map(::mapUserDetailsToNomisUser)
  }

  fun findUsers(firstName: String, lastName: String): List<NomisUserSummaryDto> {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, returning empty for {} {}", firstName, lastName)
      return listOf()
    }
    return nomisUserWebClient.get().uri {
      it.path("/users/staff")
        .queryParam("firstName", "{firstName}")
        .queryParam("lastName", "{lastName}")
        .build(firstName, lastName)
    }
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<NomisUserSummaryDto>>() {})
      .block()!!
  }

  fun findUserByUsername(username: String): NomisUserPersonDetails? {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, returning empty for {}", username)
      return null
    }
    val userDetails = webClient.get().uri("/users/{username}", username)
      .retrieve()
      .bodyToMono(NomisUserDetails::class.java)
      .onErrorResume(
        WebClientResponseException.NotFound::class.java
      ) {
        log.debug("User not found in NOMIS due to {}", it.message)
        Mono.empty()
      }
      .onErrorResume(WebClientResponseException::class.java) {
        log.warn(
          "Unable to retrieve details from NOMIS for user {} due to {}",
          username,
          (it as WebClientResponseException).statusCode
        )
        Mono.error(NomisUserServiceException(username))
      }
      .block()
    return userDetails?.let {
      mapUserDetailsToNomisUser(userDetails)
    }
  }

  fun findAllActiveUsers(page: PageRequest): PageImpl<NomisUserSummaryDto> {
    return webClient.get().uri {
      it.path("/users")
        .queryParam("status", "ACTIVE")
        .queryParam("page", page.pageNumber)
        .queryParam("size", page.pageSize)
        .build()
    }
      .retrieve()
      .bodyToMono(typeReference<RestResponsePage<NomisUserSummaryDto>>())
      .block()!!
  }

  fun authenticateUser(username: String, password: String): Boolean {
    if (!nomisEnabled) {
      log.debug("Nomis integration disabled, returning false for {}", username)
      return false
    }

    return webClient.post().uri("/users/{username}/authenticate", username)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(Authentication(password))
      .retrieve()
      .bodyToMono(Boolean::class.java)
      .defaultIfEmpty(true)
      .onErrorResume(WebClientResponseException.Unauthorized::class.java) {
        log.debug("Authentication failed for user {} due to {}", username, it.message)
        Mono.just(false)
      }
      .onErrorResume(WebClientResponseException::class.java) {
        log.warn("Unable to authenticate user {}", username, it)
        Mono.just(false)
      }
      .onErrorResume(Exception::class.java) {
        log.warn("Unable to authenticate user for user {}", username, it)
        Mono.just(false)
      }
      .block()!!
  }
}

class RestResponsePage<T> @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("content") content: List<T>,
  @JsonProperty("number") number: Int,
  @JsonProperty("size") size: Int,
  @JsonProperty("totalElements") totalElements: Long,
  @Suppress("UNUSED_PARAMETER") @JsonProperty(
    "pageable"
  ) pageable: JsonNode
) : PageImpl<T>(content, PageRequest.of(number, size), totalElements)

data class NomisUserSummaryDto(
  val username: String,
  val staffId: String,
  val firstName: String,
  val lastName: String,
  val active: Boolean,
  val activeCaseload: PrisonCaseload?,
  val email: String?,
)

data class PrisonCaseload(
  val id: String,
  val name: String
)

data class Authentication(val password: String)

fun <T> errorWhenBadRequest(
  exception: WebClientResponseException,
  errorMapper: (content: String) -> AuthenticationException
): Mono<T> =
  if (exception.rawStatusCode == BAD_REQUEST.value()) Mono.error(errorMapper(exception.responseBodyAsString)) else Mono.error(
    exception
  )

data class PasswordChangeError(val errorCode: Int? = 0)

private fun mapUserDetailsToNomisUser(userDetails: NomisUserDetails): NomisUserPersonDetails =
  NomisUserPersonDetails(
    username = userDetails.username.uppercase(),
    userId = userDetails.staffId,
    firstName = userDetails.firstName,
    surname = userDetails.surname,
    activeCaseLoadId = userDetails.activeCaseloadId,
    email = userDetails.email?.lowercase(),
    roles = userDetails.roles.map { roleCode -> SimpleGrantedAuthority(roleCode) },
    accountStatus = userDetails.accountStatus,
    accountNonLocked = userDetails.accountNonLocked,
    credentialsNonExpired = userDetails.credentialsNonExpired,
    enabled = userDetails.enabled,
    admin = userDetails.admin
  )

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
