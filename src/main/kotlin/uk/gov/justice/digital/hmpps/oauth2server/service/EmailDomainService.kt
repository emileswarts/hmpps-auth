package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import uk.gov.justice.digital.hmpps.oauth2server.config.EmailDomainExclusions
import uk.gov.justice.digital.hmpps.oauth2server.resource.CreateEmailDomainDto
import java.util.UUID

@Service
@Transactional
class EmailDomainService(
  private val emailDomainRepository: EmailDomainRepository,
  private val emailDomainExclusions: EmailDomainExclusions,
) {

  @Transactional(readOnly = true)
  fun domainList(): List<EmailDomain> = emailDomainRepository.findAllByOrderByName()

  @Throws(EmailDomainNotFoundException::class)
  fun domain(id: String): EmailDomain {
    return retrieveDomain(id, "retrieve")
  }

  @Throws(EmailDomainExcludedException::class)
  fun addDomain(newDomain: CreateEmailDomainDto) {
    val existingDomain = emailDomainRepository.findByName(newDomain.name)

    existingDomain ?: run {
      if (emailDomainExclusions.contains(newDomain.name)) {
        throw EmailDomainExcludedException(newDomain.name, "domain present in excluded list")
      }
      emailDomainRepository.save(EmailDomain(name = newDomain.name, description = newDomain.description))
    }
  }

  @Throws(EmailDomainNotFoundException::class)
  fun removeDomain(id: String) {
    val emailDomain = retrieveDomain(id, "delete")
    emailDomainRepository.delete(emailDomain)
  }

  private fun retrieveDomain(id: String, action: String): EmailDomain {
    val uuid: UUID = UUID.fromString(id)
    return emailDomainRepository.findByIdOrNull(uuid) ?: throw EmailDomainNotFoundException(action, id, "notfound")
  }
}

class EmailDomainExcludedException(val domain: String, val errorCode: String) :
  Exception("Unable to add email domain: $domain to allowed list with reason: $errorCode")

class EmailDomainNotFoundException(val action: String, val id: String, val errorCode: String) :
  Exception("Unable to $action email domain id: $id with reason: $errorCode")
