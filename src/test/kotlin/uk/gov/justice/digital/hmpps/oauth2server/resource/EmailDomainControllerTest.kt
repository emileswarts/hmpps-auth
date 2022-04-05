package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.service.EmailDomainService
import java.util.UUID

class EmailDomainControllerTest {
  private val emailDomainService: EmailDomainService = mock()

  private val controller = EmailDomainController(emailDomainService)

  @Test
  fun shouldRespondWithEmailDomainsRetrieved() {
    val id1 = UUID.randomUUID().toString()
    val id2 = UUID.randomUUID().toString()

    whenever(emailDomainService.domainList()).thenReturn(
      listOf(
        buildEmailDomain(id1, "hotmail.com"),
        buildEmailDomain(id2, "yahoo.co.uk"),
      )
    )

    val expectedEmailDomainList = listOf(
      EmailDomainDto(id1, "hotmail.com"),
      EmailDomainDto(id2, "yahoo.co.uk"),
    )

    val modelAndView = controller.domainList()

    assertTrue(modelAndView.hasView())
    assertEquals(modelAndView.viewName, "ui/emailDomains")
    assertEquals(modelAndView.model["emailDomains"], expectedEmailDomainList)
  }

  private fun buildEmailDomain(id: String, domain: String) = EmailDomain(UUID.fromString(id), domain)
}
