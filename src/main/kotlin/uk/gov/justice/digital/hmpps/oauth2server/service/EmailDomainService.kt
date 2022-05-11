package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import uk.gov.justice.digital.hmpps.oauth2server.config.EmailDomainExclusions
import uk.gov.justice.digital.hmpps.oauth2server.resource.CreateEmailDomainDto
import uk.gov.justice.digital.hmpps.oauth2server.resource.EmailDomainDto
import java.util.UUID

@Service
@Transactional
class EmailDomainService(
  private val emailDomainRepository: EmailDomainRepository,
  private val emailDomainExclusions: EmailDomainExclusions,
) {
  private companion object {
    private const val PERCENT = "%"
  }

  @Transactional(readOnly = true)
  fun domainList(): List<EmailDomainDto> {
    val allEmailDomains = emailDomainRepository.findAll()
    val emailDomainDtoList = allEmailDomains.map { emailDomain ->
      EmailDomainDto(
        emailDomain.id.toString(),
        cleanDomainNameForDisplay(emailDomain.name),
        emailDomain.description.toString()
      )
    }

    return emailDomainDtoList.sortedWith(compareBy { it.domain })
  }

  @Throws(EmailDomainNotFoundException::class)
  fun domain(id: String): EmailDomainDto {
    val emailDomain = retrieveDomain(id, "retrieve")
    return EmailDomainDto(
      emailDomain.id.toString(),
      cleanDomainNameForDisplay(emailDomain.name),
      emailDomain.description.toString()
    )
  }

  @Throws(EmailDomainAdditionBarredException::class)
  fun addDomain(newDomain: CreateEmailDomainDto) {
    val domainNameInternal = if (newDomain.name.startsWith(PERCENT)) newDomain.name else PERCENT + newDomain.name
    val existingDomain = emailDomainRepository.findByName(domainNameInternal)

    if (existingDomain != null) {
      throw EmailDomainAdditionBarredException(newDomain.name, "domain already present in allowed list")
    }

    if (emailDomainExclusions.contains(newDomain.name)) {
      throw EmailDomainAdditionBarredException(newDomain.name, "domain present in excluded list")
    }
    emailDomainRepository.save(EmailDomain(name = domainNameInternal, description = newDomain.description))
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

  private fun cleanDomainNameForDisplay(persistedDomainName: String): String {
    return persistedDomainName.removePrefix(PERCENT).removePrefix(".")
  }
}

class EmailDomainAdditionBarredException(val domain: String, val errorCode: String) :
  Exception("Unable to add email domain: $domain to allowed list with reason: $errorCode")

class EmailDomainNotFoundException(val action: String, val id: String, val errorCode: String) :
  Exception("Unable to $action email domain id: $id with reason: $errorCode")
