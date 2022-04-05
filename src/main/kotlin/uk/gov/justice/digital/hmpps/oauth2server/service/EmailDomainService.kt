package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository

@Service
@Transactional(readOnly = true)
class EmailDomainService(private val emailDomainRepository: EmailDomainRepository) {

  fun domainList(): List<EmailDomain> = emailDomainRepository.findAllByOrderByName()
}
