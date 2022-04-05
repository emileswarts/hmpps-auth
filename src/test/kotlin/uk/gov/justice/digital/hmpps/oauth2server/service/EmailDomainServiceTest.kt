package uk.gov.justice.digital.hmpps.oauth2server.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository

class EmailDomainServiceTest {

  private val emailDomainRepository: EmailDomainRepository = mock()
  private val emailDomains: List<EmailDomain> = mock()

  private val service = EmailDomainService(emailDomainRepository)

  @Test
  fun `shouldRetrieveEmailDomainList`() {
    whenever(emailDomainRepository.findAllByOrderByName()).thenReturn(emailDomains)

    val actualEmailDomainList = service.domainList()

    assertEquals(actualEmailDomainList, emailDomains)
    verifyNoInteractions(emailDomains)
  }
}
