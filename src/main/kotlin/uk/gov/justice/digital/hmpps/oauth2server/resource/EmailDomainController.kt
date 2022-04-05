package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService

@Controller
class EmailDomainController(
  private val emailDomainService: EmailDomainService
) {

  @GetMapping("/email-domains")
  fun domainList(): ModelAndView {
    val allEmailDomains = emailDomainService.domainList().map { emailDomain -> EmailDomainDto(emailDomain.id.toString(), emailDomain.name) }
    return ModelAndView("ui/emailDomains", mapOf("emailDomains" to allEmailDomains))
  }
}

data class EmailDomainDto(val id: String, val domain: String)
