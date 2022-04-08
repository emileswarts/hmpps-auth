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
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService
import java.util.UUID

class EmailDomainControllerTest {
  private val emailDomainService: EmailDomainService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val authentication: Authentication = mock()
  private val principal: UserPersonDetails = mock()

  private val id1 = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()

  private val controller = EmailDomainController(emailDomainService, telemetryClient)

  @Test
  fun shouldRespondWithEmailDomainsRetrieved() {
    whenever(emailDomainService.domainList()).thenReturn(
      listOf(
        buildEmailDomain(id1, "%advancecharity.org.uk"),
        buildEmailDomain(id2, "%123.co.uk"),
      )
    )

    val expectedEmailDomainList = listOf(
      EmailDomainDto(id1, "%advancecharity.org.uk"),
      EmailDomainDto(id2, "%123.co.uk"),
    )

    val modelAndView = controller.domainList()

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/emailDomains")
    assertEquals(modelAndView.model["emailDomains"], expectedEmailDomainList)
  }

  @Test
  fun shouldAddEmailDomain() {
    whenever(authentication.principal).thenReturn(principal)
    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    controller.addEmailDomain(authentication, newEmailDomain)

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

    controller.addEmailDomain(authentication, newEmailDomain)

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
  fun shouldRespondWithDomainListOnSuccessfulAdd() {
    whenever(authentication.principal).thenReturn(principal)

    val newEmailDomain = CreateEmailDomainDto("%123.co.uk", "test")

    whenever(emailDomainService.addDomain(newEmailDomain)).thenReturn(
      listOf(
        buildEmailDomain(id1, "%advancecharity.org.uk"),
        buildEmailDomain(id2, "%123.co.uk"),
      )
    )

    val expectedEmailDomainList = listOf(
      EmailDomainDto(id1, "%advancecharity.org.uk"),
      EmailDomainDto(id2, "%123.co.uk"),
    )

    val modelAndView = controller.addEmailDomain(authentication, newEmailDomain)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/emailDomains")
    assertEquals(modelAndView.model["emailDomains"], expectedEmailDomainList)
  }

  @Test
  fun shouldRespondWithDomainListOnSuccessfulDelete() {
    whenever(authentication.principal).thenReturn(principal)
    val id = UUID.randomUUID().toString()

    whenever(emailDomainService.removeDomain(id)).thenReturn(
      listOf(
        buildEmailDomain(id1, "%advancecharity.org.uk"),
        buildEmailDomain(id2, "%123.co.uk"),
      )
    )

    val expectedEmailDomainList = listOf(
      EmailDomainDto(id1, "%advancecharity.org.uk"),
      EmailDomainDto(id2, "%123.co.uk"),
    )

    val modelAndView = controller.deleteEmailDomain(authentication, id)

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/emailDomains")
    assertEquals(modelAndView.model["emailDomains"], expectedEmailDomainList)
  }

  private fun buildEmailDomain(id: String, domain: String) = EmailDomain(UUID.fromString(id), domain)
}
