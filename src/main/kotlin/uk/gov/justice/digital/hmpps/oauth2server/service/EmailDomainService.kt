package uk.gov.justice.digital.hmpps.oauth2server.service

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

  @Throws(EmailDomainExcludedException::class)
  fun addDomain(newDomain: CreateEmailDomainDto): List<EmailDomain> {
    val existingDomain = emailDomainRepository.findByName(newDomain.name)

    existingDomain ?: run {
      if (emailDomainExclusions.contains(newDomain.name)) {
        throw EmailDomainExcludedException(newDomain.name, "domain present in excluded list")
      }
      emailDomainRepository.save(EmailDomain(name = newDomain.name, description = newDomain.description))
    }

    return domainList()
  }

  @Throws(EmailDomainNotFoundException::class)
  fun removeDomain(id: String): List<EmailDomain> {
    val uuid: UUID = UUID.fromString(id)
    if (emailDomainRepository.existsById(uuid).not()) {
      throw EmailDomainNotFoundException("delete", id, "notfound")
    }

    emailDomainRepository.deleteById(uuid)
    return domainList()
  }
}

class EmailDomainExcludedException(val domain: String, val errorCode: String) :
  Exception("Unable to add email domain: $domain to allowed list with reason: $errorCode")

class EmailDomainNotFoundException(val action: String, val id: String, val errorCode: String) :
  Exception("Unable to $action email domain id: $id with reason: $errorCode")
