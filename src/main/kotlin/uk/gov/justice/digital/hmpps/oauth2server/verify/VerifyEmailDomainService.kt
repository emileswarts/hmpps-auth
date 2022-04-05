package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class VerifyEmailDomainService(emailDomainRepository: EmailDomainRepository) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val emailDomainsCache: LoadingCache<String, List<Regex>>

  fun isValidEmailDomain(domain: String): Boolean {
    if (domain.isBlank()) return false

    val domainLower = domain.lowercase()
    return try {
      emailDomainsCache["EMAIL_DOMAIN"].any { domainLower.matches(it) }
    } catch (e: ExecutionException) {
      // nothing we can do here, so throw toys out of pram
      log.error("Caught exception retrieving email domains", e)
      throw RuntimeException(e)
    }
  }

  init {
    emailDomainsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(
      CacheLoader.from { _: String ->
        emailDomainRepository.findAll()
          .map { it.name.replace("%".toRegex(), ".*").lowercase().toRegex() }
      }
    )
  }
}
