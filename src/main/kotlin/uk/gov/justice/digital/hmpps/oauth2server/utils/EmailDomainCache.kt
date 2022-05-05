package uk.gov.justice.digital.hmpps.oauth2server.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import java.util.concurrent.TimeUnit

@Component
class EmailDomainCache(emailDomainRepository: EmailDomainRepository) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val emailDomainsCache: LoadingCache<String, List<Regex>>

  fun refreshEmailDomainCache() {
    emailDomainsCache.refresh("EMAIL_DOMAIN")
    log.debug("EmailDomainCache refreshed")
  }

  fun getEmailDomainCache(): LoadingCache<String, List<Regex>> {
    return emailDomainsCache
  }

  init {

    emailDomainsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(
      CacheLoader.from { _: String ->
        emailDomainRepository.findAll()
          .map {
            it.name.replace("%".toRegex(), ".*").lowercase().toRegex()
          }
      }

    )
  }
}
