package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Controller
class EmailDomainController(
  private val emailDomainService: EmailDomainService,
  private val telemetryClient: TelemetryClient,
) {

  @GetMapping("/email-domains")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun domainList(): ModelAndView {
    return toDomainListView(emailDomainService.domainList())
  }

  @PostMapping("/email-domains")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun addEmailDomain(authentication: Authentication, @Valid @RequestBody emailDomain: CreateEmailDomainDto): ModelAndView {
    val allEmailDomains = emailDomainService.addDomain(emailDomain)
    recordEmailDomainStateChangeEvent("EmailDomainCreateSuccess", authentication, "domain", emailDomain.name)
    return toDomainListView(allEmailDomains)
  }

  @DeleteMapping("/email-domains/{id}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun deleteEmailDomain(authentication: Authentication, @PathVariable id: String): ModelAndView {
    val allEmailDomains = emailDomainService.removeDomain(id)
    recordEmailDomainStateChangeEvent("EmailDomainDeleteSuccess", authentication, "id", id)
    return toDomainListView(allEmailDomains)
  }

  private fun recordEmailDomainStateChangeEvent(
    eventName: String,
    authentication: Authentication,
    identifierName: String,
    identifierValue: String
  ) {
    val data = mapOf("username" to (authentication.principal as UserPersonDetails).name, identifierName to identifierValue)
    telemetryClient.trackEvent(eventName, data, null)
  }

  private fun toDomainListView(emailDomains: List<EmailDomain>): ModelAndView {
    val domainDtoList = emailDomains.map { emailDomain -> EmailDomainDto(emailDomain.id.toString(), emailDomain.name) }
    return ModelAndView("ui/emailDomains", mapOf("emailDomains" to domainDtoList))
  }
}

data class EmailDomainDto(val id: String, val domain: String)

data class CreateEmailDomainDto(
  @field:NotBlank(message = "email domain name must be supplied")
  @field:Pattern(regexp = "^%.*$", message = "email domain name must start with %")
  @field:Size(min = 2, max = 100)
  val name: String,

  @field:Size(max = 200)
  val description: String,
)
