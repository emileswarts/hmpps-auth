package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService
import uk.gov.justice.digital.hmpps.oauth2server.service.UserContextService
import uk.gov.justice.digital.hmpps.oauth2server.utils.ServiceUnavailableThreadLocal

@Controller
class HomeController(
  private val authServicesService: AuthServicesService,
  private val userContextService: UserContextService,
  @Value("\${application.link.accounts}") private val linkAccounts: Boolean,
) {
  @GetMapping("/")
  fun home(authentication: Authentication): ModelAndView {
    val userDetails = authentication.principal as UserDetailsImpl
    val authSource = AuthSource.fromNullableString(userDetails.authSource)
    val authorities = if (authSource == AuthSource.azuread || linkAccounts) {
      userContextService.discoverUsers(userDetails).flatMap { it.authorities }
    } else authentication.authorities

    val services = authServicesService.listEnabled(authorities)

    val modelAndView = ModelAndView("landing", "services", services).addObject("userRoleCount", authorities.size)
    if (ServiceUnavailableThreadLocal.service?.containsAll(listOf(AuthSource.nomis, AuthSource.delius)) == true) {
      modelAndView.addObject("serviceDown", "nomisdeliusdown")
    } else if (ServiceUnavailableThreadLocal.service?.contains(AuthSource.nomis) == true) {
      modelAndView.addObject("serviceDown", "nomisdown")
    } else if (ServiceUnavailableThreadLocal.service?.contains(AuthSource.delius) == true) {
      modelAndView.addObject("serviceDown", "deliusdown")
    }
    return modelAndView
  }

  @GetMapping("/terms")
  fun terms(): String = "terms"
}
