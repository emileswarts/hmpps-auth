package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import uk.gov.justice.digital.hmpps.oauth2server.config.EmailDomainExclusions
import uk.gov.justice.digital.hmpps.oauth2server.resource.CreateEmailDomainDto
import java.util.UUID

class EmailDomainServiceTest {

  private val emailDomainRepository: EmailDomainRepository = mock()
  private val emailDomainExclusions: EmailDomainExclusions = mock()
  private val emailDomains: List<EmailDomain> = mock()
  private val newDomain = CreateEmailDomainDto("%123.co.uk", "test")

  private val service = EmailDomainService(emailDomainRepository, emailDomainExclusions)

  @Test
  fun shouldRetrieveEmailDomainList() {
    whenever(emailDomainRepository.findAllByOrderByName()).thenReturn(emailDomains)

    val actualEmailDomainList = service.domainList()

    assertEquals(actualEmailDomainList, emailDomains)
    verifyNoInteractions(emailDomains)
  }

  @Test
  fun shouldNotAddDomainWhenAlreadyPresent() {
    whenever(emailDomainRepository.findByName(newDomain.name)).thenReturn(EmailDomain(name = newDomain.name, description = newDomain.description))
    whenever(emailDomainRepository.findAllByOrderByName()).thenReturn(emailDomains)

    val actualEmailDomainList = service.addDomain(newDomain)

    assertEquals(actualEmailDomainList, emailDomains)
    verifyNoInteractions(emailDomainExclusions)
    verify(emailDomainRepository, never()).save(any())
  }

  @Test
  fun shouldNotAddDomainWhenExcluded() {
    whenever(emailDomainExclusions.contains(newDomain.name)).thenReturn(true)
    assertThatThrownBy { service.addDomain(newDomain) }
      .isInstanceOf(EmailDomainExcludedException::class.java)
      .hasMessage("Unable to add email domain: ${newDomain.name} to allowed list with reason: domain present in excluded list")

    verify(emailDomainRepository, never()).save(any())
  }

  @Test
  fun shouldAddDomainWhenNotAlreadyPresentOrExcluded() {
    whenever(emailDomainRepository.findAllByOrderByName()).thenReturn(emailDomains)

    val actualEmailDomainList = service.addDomain(newDomain)

    val emailDomainCaptor = argumentCaptor<EmailDomain>()
    verify(emailDomainRepository).save(emailDomainCaptor.capture())
    val actualEmailDomain = emailDomainCaptor.firstValue

    assertEquals(actualEmailDomain.name, newDomain.name)
    assertEquals(actualEmailDomain.description, newDomain.description)
    assertEquals(actualEmailDomainList, emailDomains)
  }

  @Test
  fun shouldNotRemoveDomainWhenDomainNotPresent() {
    val randomUUID = UUID.randomUUID()
    val id = randomUUID.toString()
    whenever(emailDomainRepository.existsById(randomUUID)).thenReturn(false)

    assertThatThrownBy { service.removeDomain(id) }
      .isInstanceOf(EmailDomainNotFoundException::class.java)
      .hasMessage("Unable to delete email domain id: $id with reason: notfound")

    verify(emailDomainRepository, never()).deleteById(randomUUID)
  }

  @Test
  fun shouldRemoveDomainWhenDomainPresent() {
    val randomUUID = UUID.randomUUID()
    val id = randomUUID.toString()
    whenever(emailDomainRepository.existsById(randomUUID)).thenReturn(true)
    whenever(emailDomainRepository.findAllByOrderByName()).thenReturn(emailDomains)

    val actualEmailDomainList = service.removeDomain(id)

    assertEquals(actualEmailDomainList, emailDomains)
    verify(emailDomainRepository).deleteById(randomUUID)
  }
}
