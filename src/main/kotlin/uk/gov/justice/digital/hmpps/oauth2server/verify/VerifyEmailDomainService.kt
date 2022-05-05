package uk.gov.justice.digital.hmpps.oauth2server.verify

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailDomainCache
import java.util.concurrent.ExecutionException

@Service
@Transactional(readOnly = true)
class VerifyEmailDomainService(
  private val emailDomainCache: EmailDomainCache
) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun isValidEmailDomain(domain: String): Boolean {
    if (domain.isBlank()) return false

    val domainLower = domain.lowercase()
    return try {
      emailDomainCache.getEmailDomainCache()["EMAIL_DOMAIN"].any { domainLower.matches(it) }
    } catch (e: ExecutionException) {
      // nothing we can do here, so throw toys out of pram
      log.error("Caught exception retrieving email domains", e)
      throw RuntimeException(e)
    }
  }
}
