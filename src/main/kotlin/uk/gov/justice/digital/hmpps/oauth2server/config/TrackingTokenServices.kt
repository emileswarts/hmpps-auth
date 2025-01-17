@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.config

import com.microsoft.applicationinsights.TelemetryClient
import com.nimbusds.jwt.JWTParser
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientConfigRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthIpSecurity
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientService
import uk.gov.justice.digital.hmpps.oauth2server.utils.IpAddressHelper
import java.time.LocalDate

open class TrackingTokenServices(
  private val authIpSecurity: AuthIpSecurity,
  private val telemetryClient: TelemetryClient,
  private val restTemplate: RestTemplate,
  private val clientRepository: ClientRepository,
  private val clientConfigRepository: ClientConfigRepository,
  private val tokenVerificationClientCredentials: TokenVerificationClientCredentials,
  private val tokenVerificationEnabled: Boolean,
) : DefaultTokenServices() {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  override fun createAccessToken(authentication: OAuth2Authentication): OAuth2AccessToken {
    val clientId = authentication.oAuth2Request.clientId
    val baseClientId = ClientService.baseClientId(clientId)
    val clientConfig = clientConfigRepository.findByIdOrNull(baseClientId)
    val clientIpAddress = IpAddressHelper.retrieveIpFromRequest()

    if (!clientConfig?.ips.isNullOrEmpty()) {
      // temp custom event to see if the validateClientIpAllowed is called
      telemetryClient.trackEvent(
        "CreateAccessTokenAllowedIps",
        mapOf("clientId" to clientId, "clientIpAddress" to clientIpAddress, "allowedIps" to clientConfig!!.ips.toString()),
        null
      )
      authIpSecurity.validateClientIpAllowed(clientIpAddress, clientConfig.ips)
    }

    if (clientConfig?.clientEndDate != null && clientConfig.clientEndDate!!.isBefore(LocalDate.now())) throw EndDateClientException()

    val token = super.createAccessToken(authentication)
    val username = retrieveUsernameFromToken(token)

    val name = if (authentication.isClientOnly) "CreateSystemAccessToken" else "CreateAccessToken"
    telemetryClient.trackEvent(
      name,
      mapOf(
        "username" to username, "clientId" to clientId, "clientIpAddress" to clientIpAddress
      ),
      null
    )

    clientRepository.findByIdOrNull(clientId)?.resetLastAccessed()
      ?: throw RuntimeException("Unable to find client $clientId")

    if (tokenVerificationClientCredentials.clientId != clientId) {
      val jwtId = sendAuthJwtIdToTokenVerification(authentication, token)
      log.info("Created access token for {} and client {} with jwt id of {}", username, clientId, jwtId)
    }
    return token
  }

  override fun refreshAccessToken(refreshTokenValue: String, tokenRequest: TokenRequest): OAuth2AccessToken {
    val token = super.refreshAccessToken(refreshTokenValue, tokenRequest)
    val username = retrieveUsernameFromToken(token)
    val clientId = tokenRequest.clientId
    if (tokenVerificationClientCredentials.clientId != clientId) {
      val jwtId = sendRefreshToTokenVerification(refreshTokenValue, token)
      log.info("Created refresh token for {} and client {} with jwt id of {}", username, clientId, jwtId)
    }
    telemetryClient.trackEvent(
      "RefreshAccessToken",
      mapOf(
        "username" to username, "clientId" to clientId, "clientIpAddress" to IpAddressHelper.retrieveIpFromRequest()
      ),
      null
    )
    return token
  }

  private fun sendRefreshToTokenVerification(refreshTokenValue: String, token: OAuth2AccessToken): String? {
    // refresh tokens have an ati field which links back to the original access token
    val accessTokenId = JWTParser.parse(refreshTokenValue).jwtClaimsSet.getStringClaim("ati")
    if (tokenVerificationEnabled) {
      // now send token to token verification service so can validate them
      restTemplate.postForLocation("/token/refresh?accessJwtId={accessJwtId}", token.value, accessTokenId)
    }
    return accessTokenId
  }

  private fun sendAuthJwtIdToTokenVerification(
    authentication: OAuth2Authentication,
    token: OAuth2AccessToken,
  ): String? {
    val jwtId = if (authentication.principal is UserDetailsImpl) {
      (authentication.principal as UserDetailsImpl).jwtId
    } else {
      // if we're using a password grant then there won't be any authentication, so just use the jti
      token.additionalInformation["jti"] as String?
    }
    val grantType = authentication.oAuth2Request.requestParameters.get("grant_type")
    if (tokenVerificationEnabled && grantType != "client_credentials" && !jwtId.isNullOrEmpty()) {
      // now send token to token verification service so can validate them
      restTemplate.postForLocation("/token?authJwtId={authJwtId}", token.value, jwtId)
    }
    return jwtId
  }

  private fun retrieveUsernameFromToken(token: OAuth2AccessToken): String {
    val username = token.additionalInformation[JWTTokenEnhancer.SUBJECT] as String?
    return if (username.isNullOrEmpty()) "none" else username
  }
}

class AllowedIpException :
  OAuth2AccessDeniedException("Unable to issue token as request is not from ip within allowed list")

class EndDateClientException : OAuth2AccessDeniedException("Unable to issue token as client has end date in past")
