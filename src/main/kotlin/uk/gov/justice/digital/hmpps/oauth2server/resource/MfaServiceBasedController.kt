@file:Suppress("SpringJavaInjectionPointsAutowiringInspection", "SpringMVCViewInspection", "DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.MfaPreferenceType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.security.JwtAuthenticationSuccessHandler
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaService
import uk.gov.justice.digital.hmpps.oauth2server.utils.CookieHelper
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.io.IOException
import java.time.Duration
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@SessionAttributes(
  "authorizationRequest",
  "org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint.ORIGINAL_AUTHORIZATION_REQUEST"
)
@Validated
class MfaServiceBasedController(
  private val jwtAuthenticationSuccessHandler: JwtAuthenticationSuccessHandler,
  private val tokenService: TokenService,
  private val mfaRememberMeCookieHelper: MfaRememberMeCookieHelper,
  private val clientsDetailsService: ClientDetailsService,
  telemetryClient: TelemetryClient,
  mfaService: MfaService,
  @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean,
) : AbstractMfaController(
  tokenService,
  telemetryClient,
  mfaService,
  smokeTestEnabled,
  "ServiceBased",
  "/",
  "/service-mfa-challenge",
) {
  @GetMapping("/service-mfa-send-challenge")
  fun mfaSendChallengeServiceBased(
    authentication: Authentication,
    @RequestParam user_oauth_approval: String?,
  ): ModelAndView = mfaSendChallenge(authentication, extraModel(user_oauth_approval))

  @GetMapping("/service-mfa-challenge")
  fun mfaChallengeRequestServiceBased(
    @RequestParam error: String?,
    @RequestParam token: String?,
    @RequestParam mfaPreference: MfaPreferenceType?,
    @RequestParam user_oauth_approval: String?,
    @ModelAttribute("authorizationRequest") authorizationRequest: AuthorizationRequest?,
  ): ModelAndView {

    if (authorizationRequest == null) {
      telemetryClient.trackEvent("MissingAuthorizationRequest", null, null)
      throw AuthorizationRequestMissingException()
    }

    return mfaChallengeRequest(error, token, mfaPreference, extraModel(user_oauth_approval, authorizationRequest))
  }

  @PostMapping("/service-mfa-challenge")
  @Throws(IOException::class, ServletException::class)
  fun mfaChallengeServiceBased(
    @RequestParam token: String,
    @RequestParam mfaPreference: MfaPreferenceType,
    @RequestParam code: String,
    @RequestParam rememberMe: Boolean?,
    @RequestParam user_oauth_approval: String?,
    @ModelAttribute("authorizationRequest") authorizationRequest: AuthorizationRequest,
    request: HttpServletRequest,
    response: HttpServletResponse,
    authentication: Authentication,
  ): ModelAndView? = mfaChallenge(token, mfaPreference, code, authentication.name, extraModel(user_oauth_approval, authorizationRequest)) {
    jwtAuthenticationSuccessHandler.updateMfaInRequest(request, response, authentication)

    if (rememberMe == true) {
      val rememberMeToken = tokenService.createToken(TokenType.MFA_RMBR, authentication.name)
      mfaRememberMeCookieHelper.addCookieToResponse(request, response, rememberMeToken)
    }

    ModelAndView("forward:/oauth/authorize")
  }

  @GetMapping("/service-mfa-resend")
  fun mfaResendRequest(
    @RequestParam token: String,
    @RequestParam user_oauth_approval: String?,
    @RequestParam mfaPreference: MfaPreferenceType,
    authentication: Authentication
  ): ModelAndView = createMfaResendRequest(token, mfaPreference, authentication.name, extraModel(user_oauth_approval))

  @PostMapping("/service-mfa-resend")
  fun mfaResend(
    @RequestParam token: String,
    @RequestParam user_oauth_approval: String?,
    @RequestParam mfaResendPreference: MfaPreferenceType,
    authentication: Authentication
  ): ModelAndView = createMfaResend(token, mfaResendPreference, authentication.name, extraModel(user_oauth_approval))

  private fun extraModel(user_oauth_approval: String?) = mapOf("user_oauth_approval" to user_oauth_approval)
  private fun extraModel(user_oauth_approval: String?, authorizationRequest: AuthorizationRequest): Map<String, Any?> {
    val clientDetails = clientsDetailsService.loadClientByClientId(authorizationRequest.clientId)
    val mfaRememberMe = clientDetails.additionalInformation.get("mfaRememberMe") as Boolean?

    return extraModel(user_oauth_approval) + ("mfaRememberMe" to mfaRememberMe)
  }
}

@Component
class MfaRememberMeCookieHelper : CookieHelper("mfa_remember_me", Duration.ofDays(7))

open class AuthorizationRequestMissingException() : Exception("End point invoked without AuthorizationRequest") {
  var field: String = "authorizationRequest"
  var errorCode: String = "authorizationRequest.notfound"
}
