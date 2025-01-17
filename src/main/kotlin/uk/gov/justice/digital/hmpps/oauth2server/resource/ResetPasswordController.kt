package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.service.NotificationClientRuntimeException
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailHelper
import uk.gov.justice.digital.hmpps.oauth2server.utils.removeAllCrLf
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordService
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordServiceImpl.ResetPasswordException
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.ValidEmailException
import javax.servlet.http.HttpServletRequest

@Controller
@Validated
class ResetPasswordController(
  private val resetPasswordService: ResetPasswordService,
  private val tokenService: TokenService,
  userService: UserService,
  private val verifyEmailService: VerifyEmailService,
  private val telemetryClient: TelemetryClient,
  @param:Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean,
  @Value("\${application.authentication.denylist}") passwordDenylist: Set<String>,
) :
  AbstractPasswordController(
    resetPasswordService,
    tokenService,
    userService,
    telemetryClient,
    "resetPassword",
    "setPassword",
    passwordDenylist
  ) {

  @GetMapping("/reset-password")
  fun resetPasswordRequest(): String = "resetPassword"

  @GetMapping("/reset-password-success")
  fun resetPasswordSuccess(
    @RequestParam(name = "auth-source", required = false) authSource: String? = "none"
  ): ModelAndView {
    return ModelAndView(
      "resetPasswordSuccess",
      mapOf("legacyIdentityProvider" to AuthSource.getSourceLegacyName(authSource))
    )
  }

  @PostMapping("/reset-password")
  fun resetPasswordRequest(
    @RequestParam(required = false) usernameOrEmail: String?,
    request: HttpServletRequest,
  ): ModelAndView {
    if (usernameOrEmail.isNullOrBlank()) {
      telemetryClient.trackEvent("ResetPasswordRequestFailure", mapOf("error" to "missing"), null)
      return ModelAndView("resetPassword", "error", "missing")
    }
    if (usernameOrEmail.contains("@")) {
      try {
        verifyEmailService.validateEmailAddressExcludingGsi(EmailHelper.format(usernameOrEmail)!!, User.EmailType.PRIMARY)
      } catch (e: ValidEmailException) {
        log.info("Validation failed for reset password email address due to {}", e.reason)
        telemetryClient.trackEvent(
          "ResetPasswordRequestFailure",
          mapOf("email" to usernameOrEmail, "reason" to "email." + e.reason),
          null
        )
        return ModelAndView(
          "resetPassword",
          mapOf("error" to "email." + e.reason, "usernameOrEmail" to usernameOrEmail)
        )
      }
    }

    return try {
      val resetLink = resetPasswordService.requestResetPassword(usernameOrEmail, request.requestURL.toString())
      val modelAndView = ModelAndView("resetPasswordSent")
      if (resetLink.isPresent) {
        log.info("Reset password request success for {}", usernameOrEmail)
        telemetryClient.trackEvent("ResetPasswordRequestSuccess", mapOf("username" to usernameOrEmail), null)
        if (smokeTestEnabled) {
          modelAndView.addObject("resetLink", resetLink.get())
        }
      } else {
        log.info("Reset password request failed, no link provided for $usernameOrEmail".removeAllCrLf())
        telemetryClient.trackEvent(
          "ResetPasswordRequestFailure",
          mapOf("username" to usernameOrEmail, "error" to "nolink"),
          null
        )
        if (smokeTestEnabled) {
          modelAndView.addObject("resetLinkMissing", true)
        }
      }
      modelAndView
    } catch (e: ResetPasswordException) {
      log.error("Failed to reset password due to ", e)
      telemetryClient.trackEvent(
        "ResetPasswordRequestDisallowed",
        mapOf("username" to usernameOrEmail, "error" to e.javaClass.simpleName),
        null
      )
      ModelAndView("resetPasswordDisallowed", "error", "other")
    } catch (e: NotificationClientRuntimeException) {
      log.error("Failed to send reset password due to", e)
      telemetryClient.trackEvent(
        "ResetPasswordRequestFailure",
        mapOf("username" to usernameOrEmail, "error" to e.javaClass.simpleName),
        null
      )
      ModelAndView("resetPassword", "error", "other")
    }
  }

  @GetMapping("/reset-password-select")
  fun resetPasswordSelect(@RequestParam token: String): ModelAndView {
    val userTokenOptional = tokenService.checkToken(TokenType.RESET, token)
    return userTokenOptional.map { ModelAndView("resetPassword", "error", it) }
      .orElseGet { ModelAndView("setPasswordSelect", "token", token) }
  }

  @PostMapping("/reset-password-select")
  fun resetPasswordChosen(
    @RequestParam token: String,
    @RequestParam username: String,
  ): ModelAndView {
    val userTokenOptional = tokenService.checkToken(TokenType.RESET, token)
    return userTokenOptional.map { ModelAndView("resetPassword", "error", it) }.orElseGet {
      try {
        val newToken = resetPasswordService.moveTokenToAccount(token, username)
        log.info("Successful reset password select for {}", username)
        telemetryClient.trackEvent("ResetPasswordSelectSuccess", mapOf("username" to username), null)
        createModelWithTokenUsernameAndIsAdmin(TokenType.RESET, newToken, "setPassword")
      } catch (e: ResetPasswordException) {
        log.info("Validation failed due to {} for reset password select for {}", e.reason, username)
        telemetryClient.trackEvent(
          "ResetPasswordSelectFailure",
          mapOf("username" to username, "error" to e.reason),
          null
        )
        ModelAndView("setPasswordSelect", mapOf("error" to e.reason, "username" to username, "token" to token))
      }
    }
  }

  @GetMapping("/reset-password-confirm")
  fun resetPasswordConfirm(@RequestParam token: String): ModelAndView {
    val errorTypeOptional = tokenService.checkToken(TokenType.RESET, token)
    return errorTypeOptional.map { ModelAndView("resetPassword", "error", it) }
      .orElseGet { createModelWithTokenUsernameAndIsAdmin(TokenType.RESET, token, "setPassword") }
  }

  @PostMapping("/set-password")
  fun setPassword(
    @RequestParam token: String,
    @RequestParam newPassword: String?,
    @RequestParam confirmPassword: String?,
    @RequestParam(required = false) initial: Boolean?,
  ): ModelAndView {
    val result = processSetPassword(
      TokenType.RESET,
      if (initial == true) "Initial" else "Reset",
      token,
      newPassword,
      confirmPassword,
    )
    val authSource = result.second
    return result.first.map { if (initial == true) it.addObject("initial", initial) else it }.orElseGet {
      ModelAndView(if (initial == true) "redirect:/initial-password-success" else "redirect:/reset-password-success?auth-source=$authSource")
    }
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
