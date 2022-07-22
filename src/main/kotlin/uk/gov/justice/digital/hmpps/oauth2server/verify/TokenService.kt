package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.hibernate.Hibernate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityNotFoundException

@Service
@Transactional(readOnly = true)
class TokenService(
  private val userTokenRepository: UserTokenRepository,
  private val userService: UserService,
  private val telemetryClient: TelemetryClient,
  @Value("\${token.reset.expiry.days}") private val tokenExpiryDays: Long,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getToken(tokenType: TokenType, token: String): Optional<UserToken> {
    val userTokenOptional = userTokenRepository.findById(token)
    return userTokenOptional.filter { t -> t.tokenType == tokenType }
  }

  fun getUserFromToken(tokenType: TokenType, token: String): User {
    val userTokenOptional = userTokenRepository.findById(token)
    val userToken = userTokenOptional.filter { t -> t.tokenType == tokenType }
      .orElseThrow { EntityNotFoundException("Token not found $token") }
    Hibernate.initialize(userToken.user.contacts)
    return userToken.user
  }

  fun checkToken(tokenType: TokenType, token: String): Optional<String> {
    return checkTokenActive(tokenType, getToken(tokenType, token))
  }

  @Transactional
  fun createToken(tokenType: TokenType, username: String): String {
    log.info("Requesting {} for {}", tokenType.description, username)
    val user = userService.getOrCreateUser(username).orElseThrow()
    val userToken = user.createToken(tokenType)
    telemetryClient.trackEvent(
      "${tokenType.description}Request",
      mapOf("username" to username),
      null
    )
    return userToken.token
  }

  @Transactional
  fun createTokenForNewUser(tokenType: TokenType, createTokenRequest: CreateTokenRequest): String {
    log.info("Requesting {} for {}", tokenType.description, createTokenRequest.username)
    val user = userService.createUser(createTokenRequest).orElseThrow()
    val userToken = user.createToken(tokenType)
    telemetryClient.trackEvent(
      "${tokenType.description}Request",
      mapOf("username" to createTokenRequest.username),
      null
    )
    userToken.tokenExpiry = LocalDateTime.now().plusDays(tokenExpiryDays)
    return userToken.token
  }

  @Transactional
  fun removeToken(tokenType: TokenType, token: String) =
    getToken(tokenType, token).ifPresent { userTokenRepository.delete(it) }

  @Transactional
  fun isValid(tokenType: TokenType, token: String?, username: String?): Boolean {
    if (token.isNullOrBlank()) return false
    val userTokenOptional = getToken(tokenType, token)

    val errors = checkTokenActive(tokenType, userTokenOptional)
    val success = errors.map { false }.orElseGet { tokenIssuedToUser(tokenType, userTokenOptional, username) }

    // delete token if something went wrong e.g. token expired
    if (!success) removeToken(userTokenOptional)
    return success
  }

  @Transactional
  fun checkTokenForUser(tokenType: TokenType, token: String, username: String?): Optional<String> {
    if (token.isBlank()) {
      recordInvalidTokenEvent(tokenType)
      return invalidTokenResponse()
    }
    val userTokenOptional = getToken(tokenType, token)
    val errorOptional = checkTokenActive(tokenType, userTokenOptional)
    val response = errorOptional.or { checkTokenOwnedByUser(tokenType, userTokenOptional, username) }
    response.ifPresent { removeToken(userTokenOptional) }

    return response
  }

  private fun removeToken(userTokenOptional: Optional<UserToken>) {
    if (userTokenOptional.isPresent) userTokenRepository.delete(userTokenOptional.get())
  }

  private fun checkTokenOwnedByUser(
    tokenType: TokenType,
    token: Optional<UserToken>,
    username: String?
  ): Optional<String> {
    if (tokenIssuedToUser(tokenType, token, username)) {
      return Optional.empty()
    }
    return invalidTokenResponse()
  }

  private fun tokenIssuedToUser(
    tokenType: TokenType,
    userTokenOptional: Optional<UserToken>,
    username: String?
  ): Boolean {
    val tokenIssuedToUser = userTokenOptional.orElseThrow().user.username == username
    if (!tokenIssuedToUser) {
      recordInvalidTokenEvent(tokenType)
    }

    return tokenIssuedToUser
  }

  private fun checkTokenActive(tokenType: TokenType, userTokenOptional: Optional<UserToken>): Optional<String> {
    if (userTokenOptional.isEmpty) {
      recordInvalidTokenEvent(tokenType)
      return invalidTokenResponse()
    }
    val userToken = userTokenOptional.get()
    if (userToken.hasTokenExpired()) {
      log.info("Failed to {} due to expired token", tokenType.description)
      val username = userToken.user.username
      telemetryClient.trackEvent(
        "${tokenType.description}Failure",
        mapOf("username" to username, "reason" to "expired"),
        null
      )
      return Optional.of("expired")
    }
    return Optional.empty()
  }

  private fun recordInvalidTokenEvent(tokenType: TokenType) {
    log.info("Failed to {} due to invalid token", tokenType.description)
    telemetryClient.trackEvent(
      "${tokenType.description}Failure",
      mapOf("reason" to "invalid"),
      null
    )
  }

  private fun invalidTokenResponse() = Optional.of("invalid")
}
