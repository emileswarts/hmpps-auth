package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import uk.gov.justice.digital.hmpps.oauth2server.config.EmailDomainExclusions
import uk.gov.justice.digital.hmpps.oauth2server.resource.CreateEmailDomainDto

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
}

class EmailDomainExcludedException(val domain: String, val errorCode: String) :
  Exception("Unable to add email domain: $domain to allowed list with reason: $errorCode")
