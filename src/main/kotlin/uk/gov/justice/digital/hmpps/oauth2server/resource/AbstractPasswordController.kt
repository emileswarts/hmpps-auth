package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.LockedException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.PasswordService
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.util.Optional

open class AbstractPasswordController(
  private val passwordService: PasswordService,
  private val tokenService: TokenService,
  private val userService: UserService,
  private val telemetryClient: TelemetryClient,
  private val startAgainViewOrUrl: String,
  private val failureViewName: String,
  private val passwordDenylist: Set<String>,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createModelWithTokenUsernameAndIsAdmin(
    tokenType: UserToken.TokenType,
    token: String,
    viewName: String
  ): ModelAndView {
    val userToken = tokenService.getToken(tokenType, token)
    val modelAndView = ModelAndView(viewName, "token", token)
    addUsernameAndIsAdminToModel(userToken.orElseThrow(), modelAndView)
    return modelAndView
  }

  fun processSetPassword(
    tokenType: UserToken.TokenType,
    metricsPrefix: String,
    token: String,
    newPassword: String?,
    confirmPassword: String?
  ): Pair<Optional<ModelAndView>, String> {
    val userTokenOptional = tokenService.checkToken(tokenType, token)
    if (userTokenOptional.isPresent) {
      val modelAndView: ModelAndView = if (startAgainViewOrUrl.startsWith("redirect")) {
        ModelAndView(String.format(startAgainViewOrUrl, userTokenOptional.get()))
      } else {
        ModelAndView(startAgainViewOrUrl, "error", userTokenOptional.get())
      }
      return Pair(Optional.of(modelAndView), AuthSource.none.name)
    }
    // token checked already by service, so can just get it here
    val userToken = tokenService.getToken(tokenType, token).orElseThrow()
    val authSource = getAuthSourceFromToken(userToken)
    val username = userToken.user.username
    val validationResult = validate(username, newPassword, confirmPassword)
    if (!validationResult.isEmpty()) {
      val modelAndView = ModelAndView(failureViewName, "token", token)
      addUsernameAndIsAdminToModel(userToken, modelAndView)
      return Pair(trackAndReturn(tokenType, username, modelAndView, validationResult), authSource)
    }
    try {
      passwordService.setPassword(token, newPassword)
    } catch (e: Exception) {
      val modelAndView = ModelAndView(failureViewName, "token", token)
      addUsernameAndIsAdminToModel(userToken, modelAndView)
      if (e is PasswordValidationFailureException) {
        return Pair(trackAndReturn(tokenType, username, modelAndView, "validation"), authSource)
      }
      if (e is ReusedPasswordException) {
        return Pair(trackAndReturn(tokenType, username, modelAndView, "reused"), authSource)
      }
      if (e is LockedException) {
        return Pair(trackAndReturn(tokenType, username, modelAndView, "state"), authSource)
      }
      // let any other exception bubble up
      log.info("Failed to ${tokenType.description} due to ${e.javaClass.name}", e)
      telemetryClient.trackEvent(
        "${tokenType.description}Failure",
        mapOf("username" to username, "reason" to e.javaClass.simpleName),
        null
      )
      throw e
    }
    log.info("Successfully changed password for {}", username)
    telemetryClient.trackEvent(
      "${metricsPrefix}PasswordSuccess",
      mapOf("username" to username),
      null
    )
    return Pair(Optional.empty(), authSource)
  }

  fun getAuthSourceFromToken(userToken: UserToken): String {
    return userService.findMasterUserPersonDetails(userToken.user.username).orElseThrow().authSource
  }

  private fun validate(username: String, newPassword: String?, confirmPassword: String?): MultiValueMap<String, Any?> {
    val builder = LinkedMultiValueMap<String, Any?>()

    if (newPassword.isNullOrBlank() || confirmPassword.isNullOrBlank()) {

      if (newPassword.isNullOrBlank()) {
        builder.add("errornew", "newmissing")
      }
      if (confirmPassword.isNullOrBlank()) {
        builder.add("errorconfirm", "confirmmissing")
      }

      // Bomb out now as either new password or confirm new password is missing
      return builder
    }

    // user must be present in order for authenticate to work above
    val user = userService.findMasterUserPersonDetails(username).orElseThrow()

    // Ensuring alphanumeric will ensure that we can't get SQL Injection attacks - since for oracle the password
    // cannot be used in a prepared statement
    if (!StringUtils.isAlphanumeric(newPassword)) {
      builder.add("errornew", "alphanumeric")
    }
    val digits = StringUtils.getDigits(newPassword)
    if (digits.isEmpty()) {
      builder.add("errornew", "nodigits")
    }
    if (digits.length == newPassword.length) {
      builder.add("errornew", "alldigits")
    }
    if (passwordDenylist.contains(newPassword.lowercase())) {
      builder.add("errornew", "denylist")
    }
    if (StringUtils.containsIgnoreCase(newPassword, username)) {
      builder.add("errornew", "username")
    }
    if (newPassword.chars().distinct().count() < 4) {
      builder.add("errornew", "four")
    }
    if (!StringUtils.equals(newPassword, confirmPassword)) {
      builder.add("errorconfirm", "mismatch")
    }
    if (user.isAdmin) {
      if (newPassword.length < 14) {
        builder.add("errornew", "length14")
      }
    } else if (newPassword.length < 9) {
      builder.add("errornew", "length9")
    }
    if (newPassword.length > 30) {
      builder.add("errornew", "long")
    }
    return builder
  }

  private fun trackAndReturn(
    tokenType: UserToken.TokenType,
    username: String,
    modelAndView: ModelAndView,
    validationResult: MultiValueMap<String, Any?>
  ): Optional<ModelAndView> {
    log.info("Failed to ${tokenType.description}  due to $validationResult ")
    telemetryClient.trackEvent(
      "${tokenType.description}Failure",
      mapOf("username" to username, "reason" to validationResult.toString()),
      null
    )
    modelAndView.addAllObjects(validationResult)
    modelAndView.addObject("error", true)
    return Optional.of(modelAndView)
  }

  private fun trackAndReturn(
    tokenType: UserToken.TokenType,
    username: String,
    modelAndView: ModelAndView,
    reason: String
  ): Optional<ModelAndView> {
    log.info("Failed to ${tokenType.description} due to $reason")
    telemetryClient.trackEvent(
      "${tokenType.description}Failure",
      mapOf("username" to username, "reason" to reason),
      null
    )
    modelAndView.addObject("errornew", reason)
    modelAndView.addObject("error", true)
    return Optional.of(modelAndView)
  }

  private fun addUsernameAndIsAdminToModel(userToken: UserToken, modelAndView: ModelAndView) {
    val username = userToken.user.username
    modelAndView.addObject("username", username)
    val isAdmin = userService.findMasterUserPersonDetails(username).orElseThrow().isAdmin
    modelAndView.addObject("isAdmin", isAdmin)
  }
}
