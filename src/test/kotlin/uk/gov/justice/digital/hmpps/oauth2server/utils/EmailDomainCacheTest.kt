package uk.gov.justice.digital.hmpps.oauth2server.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import java.util.UUID

class EmailDomainCacheTest {

  private val emailDomainRepository: EmailDomainRepository = mock()

  private var emailDomainCache = EmailDomainCache(emailDomainRepository)

  init {
    whenever(emailDomainRepository.findAll()).thenReturn(
      mutableListOf(
        EmailDomain(id = UUID.randomUUID(), name = "acc.com", description = "description"),
        EmailDomain(id = UUID.randomUUID(), name = "%adc.com", description = "description"),
        EmailDomain(id = UUID.randomUUID(), name = "%.abc.com", description = "description"),
      )
    )
  }

  @Test
  fun shouldRetrieveEmailDomainListFromCache() {

    val actualEmailDomainList = emailDomainCache.getEmailDomainCache()["EMAIL_DOMAIN"].size
    assertEquals(3, actualEmailDomainList)
  }
}
