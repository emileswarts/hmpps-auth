package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.validation.BindingResult
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService
import java.util.UUID

class EmailDomainControllerTest {
  private val emailDomainService: EmailDomainService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val authentication: Authentication = mock()
  private val principal: UserPersonDetails = mock()
  private val result: BindingResult = mock()
  private val emailDomains: List<EmailDomainDto> = mock()

  private val id1 = UUID.randomUUID().toString()

  private val controller = EmailDomainController(emailDomainService, telemetryClient)

  @Test
  fun shouldRespondWithEmailDomainsRetrieved() {
    whenever(emailDomainService.domainList()).thenReturn(emailDomains)

    val modelAndView = controller.domainList()

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/emailDomains")
    assertEquals(modelAndView.model["emailDomains"], emailDomains)
    verifyNoInteractions(emailDomains)
  }

  @Test
  fun shouldAddEmailDomain() {
    whenever(authentication.principal).thenReturn(principal)
    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    controller.addEmailDomain(authentication, newEmailDomain, result)

    verify(emailDomainService).addDomain(newEmailDomain)
  }

  @Test
  fun shouldDeleteEmailDomain() {
    whenever(authentication.principal).thenReturn(principal)
    val id = UUID.randomUUID().toString()

    controller.deleteEmailDomain(authentication, id)

    verify(emailDomainService).removeDomain(id)
  }

  @Test
  fun shouldRecordEmailDomainCreateSuccessEvent() {
    whenever(authentication.principal).thenReturn(principal)
    whenever(principal.name).thenReturn("Fred")
    val eventDetails = argumentCaptor<Map<String, String>>()
    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    controller.addEmailDomain(authentication, newEmailDomain, result)

    verify(telemetryClient).trackEvent(eq("EmailDomainCreateSuccess"), eventDetails.capture(), anyOrNull())
    assertEquals("Fred", eventDetails.firstValue.getValue("username"))
    assertEquals("%123.co.uk", eventDetails.firstValue.getValue("domain"))
  }

  @Test
  fun shouldRecordEmailDomainDeleteSuccessEvent() {
    whenever(authentication.principal).thenReturn(principal)
    whenever(principal.name).thenReturn("Fred")
    val eventDetails = argumentCaptor<Map<String, String>>()
    val id = UUID.randomUUID().toString()

    controller.deleteEmailDomain(authentication, id)

    verify(telemetryClient).trackEvent(eq("EmailDomainDeleteSuccess"), eventDetails.capture(), anyOrNull())
    assertEquals("Fred", eventDetails.firstValue.getValue("username"))
    assertEquals(id, eventDetails.firstValue.getValue("id"))
  }

  @Test
  fun shouldRedirectToDomainListOnSuccessfulAdd() {
    whenever(authentication.principal).thenReturn(principal)

    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    val modelAndView = controller.addEmailDomain(authentication, newEmailDomain, result)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "redirect:/email-domains")
  }

  @Test
  fun shouldReturnToAddEmailDomainFormOnValidationErrors() {
    whenever(authentication.principal).thenReturn(principal)
    whenever(result.hasErrors()).thenReturn(true)

    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    val modelAndView = controller.addEmailDomain(authentication, newEmailDomain, result)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/newEmailDomainForm")
    assertEquals(modelAndView.model["createEmailDomainDto"], newEmailDomain)
  }

  @Test
  fun shouldRedirectToDomainListOnSuccessfulDelete() {
    whenever(authentication.principal).thenReturn(principal)
    val id = UUID.randomUUID().toString()

    val modelAndView = controller.deleteEmailDomain(authentication, id)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "redirect:/email-domains")
  }

  @Test
  fun shouldRouteToDeleteConfirm() {
    whenever(emailDomainService.domain(id1)).thenReturn(EmailDomainDto(id1, "advancecharity.org.uk"))

    val modelAndView = controller.deleteConfirm(authentication, id1)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/deleteEmailDomainConfirm")
    assertEquals(EmailDomainDto(id1, "advancecharity.org.uk"), modelAndView.model["emailDomain"])
  }
}
