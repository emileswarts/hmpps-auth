@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.springframework.security.oauth2.common.util.OAuth2Utils.USER_OAUTH_APPROVAL
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
class UserSelectorController {
  @PostMapping("/user-selector")
  fun selectUser(
    @RequestParam requireMfa: Boolean?,
    @RequestParam user_oauth_approval: String,
  ): ModelAndView =
    if (requireMfa == true) ModelAndView(
      "redirect:/service-mfa-send-challenge", USER_OAUTH_APPROVAL, user_oauth_approval
    )
    else ModelAndView("forward:/oauth/authorize")
}
