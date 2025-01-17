package uk.gov.justice.digital.hmpps.oauth2server.security

import com.microsoft.applicationinsights.TelemetryClient
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.security.LockingAuthenticationProvider.MfaRequiredException
import uk.gov.justice.digital.hmpps.oauth2server.security.LockingAuthenticationProvider.MfaUnavailableException
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaService
import uk.gov.justice.digital.hmpps.oauth2server.utils.ServiceUnavailableThreadLocal
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.io.IOException
import java.util.StringJoiner
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class UserStateAuthenticationFailureHandler(
  private val tokenService: TokenService,
  private val mfaService: MfaService,
  @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean,
  private val telemetryClient: TelemetryClient,
) : SimpleUrlAuthenticationFailureHandler(FAILURE_URL) {
  private companion object {
    private const val FAILURE_URL = "/sign-in"
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    isAllowSessionCreation = false
  }

  @Throws(IOException::class)
  override fun onAuthenticationFailure(
    request: HttpServletRequest,
    response: HttpServletResponse,
    exception: AuthenticationException,
  ) {
    val username = request.getParameter("username")?.trim()?.uppercase()
    return onAuthenticationFailureForUsername(request, response, exception, username)
  }

  @Throws(IOException::class)
  fun onAuthenticationFailureForUsername(
    request: HttpServletRequest,
    response: HttpServletResponse,
    exception: AuthenticationException,
    username: String?,
  ) {
    val failures = when (exception) {
      is LockedException -> Pair("locked", null)
      is CredentialsExpiredException -> {
        // special handling for expired users and feature switch turned on
        val token = tokenService.createToken(TokenType.CHANGE, username!!)
        trackFailure(username, "expired")
        redirectStrategy.sendRedirect(request, response, "/change-password?token=$token")
        return
      }
      is MfaRequiredException -> {
        // need to break out to perform mfa for the user
        try {
          val (token, code, mfaType) = mfaService.createTokenAndSendMfaCode(username!!)
          val urlBuilder = UriComponentsBuilder.fromPath("/mfa-challenge")
            .queryParam("token", token)
            .queryParam("mfaPreference", mfaType)
          if (smokeTestEnabled) urlBuilder.queryParam("smokeCode", code)
          val url = urlBuilder.build().toString()

          redirectStrategy.sendRedirect(request, response, url)
          return
        } catch (e: MfaUnavailableException) {
          Pair("mfaunavailable", null)
        }
      }
      is MfaUnavailableException -> {
        Pair("mfaunavailable", null)
      }
      is MissingCredentialsException -> {
        if (username.isNullOrBlank()) {
          if (StringUtils.isBlank(request.getParameter("password"))) {
            Pair("missinguser", "missingpass")
          } else {
            Pair("missinguser", null)
          }
        } else {
          Pair("missingpass", null)
        }
      }
      is DeliusAuthenticationServiceException -> Pair("invalid", "deliusdown")
      is OAuth2AuthenticationException -> {
        log.error("Unable to connect to azure due to:", exception)
        Pair("justiceunavailable", null)
      }
      else -> {
        if (ServiceUnavailableThreadLocal.service?.containsAll(listOf(AuthSource.nomis, AuthSource.delius)) == true) {
          Pair("invalid", "nomisdeliusdown")
        } else if (ServiceUnavailableThreadLocal.service?.contains(AuthSource.nomis) == true) {
          Pair("invalid", "nomisdown")
        } else if (ServiceUnavailableThreadLocal.service?.contains(AuthSource.delius) == true) {
          Pair("invalid", "deliusdown")
        } else {
          Pair("invalid", null)
        }
      }
    }

    val builder = StringJoiner("&error=", "?error=", "")
    with(failures) {
      builder.add(first)
      second?.run { builder.add(this) }
      trackFailure(username, first)
    }

    ServiceUnavailableThreadLocal.clear()

    val redirectUrl = FAILURE_URL + builder.toString()
    redirectStrategy.sendRedirect(request, response, redirectUrl)
  }

  private fun trackFailure(usernameParam: String?, type: String) {
    telemetryClient.trackEvent(
      "AuthenticateFailure",
      mapOf("username" to (usernameParam ?: "missinguser"), "type" to type),
      null
    )
  }
}
