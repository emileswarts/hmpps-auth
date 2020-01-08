package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.security.JwtAuthenticationSuccessHandler
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaService
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@Validated
@RequestMapping("/mfa-challenge")
open class MfaController(private val jwtAuthenticationSuccessHandler: JwtAuthenticationSuccessHandler,
                         private val tokenService: TokenService,
                         private val userService: UserService,
                         private val telemetryClient: TelemetryClient,
                         private val mfaService: MfaService,
                         @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean) {
  @GetMapping
  open fun mfaChallengeRequest(@RequestParam token: String): ModelAndView {

    val optionalError = tokenService.checkToken(TokenType.MFA, token)

    return optionalError.map { ModelAndView("redirect:/login?error=mfa${it}") }
        .orElse(ModelAndView("mfaChallenge", "token", token))
  }

  @PostMapping
  @Throws(IOException::class, ServletException::class)
  open fun mfaChallenge(@RequestParam token: String,
                        @RequestParam code: String,
                        request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
    val optionalErrorForToken = tokenService.checkToken(TokenType.MFA, token)
    if (optionalErrorForToken.isPresent) {
      return ModelAndView("redirect:/login?error=mfa${optionalErrorForToken.get()}")
    }

    val optionalErrorForCode = mfaService.validateMfaCode(code)
    if (optionalErrorForCode.isPresent) {
      return ModelAndView("mfaChallenge", mapOf("token" to token, "error" to optionalErrorForCode.get()))
    }

    // can just grab token here as validated above
    val username = tokenService.getToken(TokenType.MFA, token).map { it.user.username }.orElseThrow()

    // now load the user
    val userPersonDetails = userService.findMasterUserPersonDetails(username).orElseThrow()
    val successToken = UsernamePasswordAuthenticationToken(userPersonDetails, "code")

    // now remove tokens to ensure it can't be used again
    tokenService.removeToken(TokenType.MFA, token)
    tokenService.removeToken(TokenType.MFA_CODE, code)

    // success, so forward on
    telemetryClient.trackEvent("MfaAuthenticateSuccess", mapOf("username" to username), null)
    jwtAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, successToken)

    // return here is not required, since the success handler will have redirected
    return null
  }
}