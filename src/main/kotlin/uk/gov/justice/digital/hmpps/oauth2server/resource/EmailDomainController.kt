package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainAdditionBarredException
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailDomainCache
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Controller
class EmailDomainController(
  private val emailDomainService: EmailDomainService,
  private val telemetryClient: TelemetryClient,
  private val emailDomainCache: EmailDomainCache,
) {

  @GetMapping("/email-domains/form")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun newDomainForm(): ModelAndView {
    return newEmailDomainView(CreateEmailDomainDto())
  }

  @GetMapping("/email-domains")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun domainList(): ModelAndView {
    return toDomainListView(emailDomainService.domainList())
  }

  @GetMapping("/email-domains/{id}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun deleteConfirm(authentication: Authentication, @PathVariable id: UUID): ModelAndView {
    val emailDomain = emailDomainService.domain(id)
    return ModelAndView("ui/deleteEmailDomainConfirm", mapOf("emailDomain" to emailDomain))
  }

  @PostMapping("/email-domains")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun addEmailDomain(authentication: Authentication, @Valid @ModelAttribute emailDomain: CreateEmailDomainDto, result: BindingResult): ModelAndView {
    if (result.hasErrors()) {
      return newEmailDomainView(emailDomain)
    }

    return try {
      emailDomainService.addDomain(emailDomain)
      recordEmailDomainStateChangeEvent("EmailDomainCreateSuccess", authentication, "domain", emailDomain.name)
      emailDomainCache.refreshEmailDomainCache()
      redirectToDomainListView()
    } catch (e: EmailDomainAdditionBarredException) {
      newEmailDomainView(emailDomain).addObject("error", e.message)
    }
  }

  @DeleteMapping("/email-domains/{id}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_EMAIL_DOMAINS')")
  fun deleteEmailDomain(authentication: Authentication, @PathVariable id: UUID): ModelAndView {
    emailDomainService.removeDomain(id)
    recordEmailDomainStateChangeEvent("EmailDomainDeleteSuccess", authentication, "id", id.toString())
    return redirectToDomainListView()
  }

  private fun recordEmailDomainStateChangeEvent(
    eventName: String,
    authentication: Authentication,
    identifierName: String,
    identifierValue: String?
  ) {
    val data = mapOf("username" to (authentication.principal as UserPersonDetails).name, identifierName to identifierValue)
    telemetryClient.trackEvent(eventName, data, null)
  }

  private fun newEmailDomainView(createEmailDomainDto: CreateEmailDomainDto): ModelAndView {
    return ModelAndView("ui/newEmailDomainForm", "createEmailDomainDto", createEmailDomainDto)
  }

  private fun redirectToDomainListView(): ModelAndView {
    return ModelAndView("redirect:/email-domains")
  }

  private fun toDomainListView(emailDomains: List<EmailDomainDto>): ModelAndView {
    return ModelAndView("ui/emailDomains", mapOf("emailDomains" to emailDomains))
  }
}

data class EmailDomainDto(val id: String, val domain: String, val description: String)

data class CreateEmailDomainDto(
  @field:NotBlank(message = "email domain name must be supplied")
  @field:Size(min = 6, max = 100, message = "email domain name must be between 6 and 100 characters in length (inclusive)")
  val name: String = "",

  @field:Size(max = 200, message = "email domain description cannot be greater than 200 characters in length")
  val description: String? = null,
)
