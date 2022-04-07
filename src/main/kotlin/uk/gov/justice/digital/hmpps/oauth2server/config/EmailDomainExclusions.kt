package uk.gov.justice.digital.hmpps.oauth2server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application.email.domain.exclude")
data class EmailDomainExclusions(
  private val exclude: List<String>,
) {

  fun contains(domain: String): Boolean {
    return exclude.any { domain.contains(it, ignoreCase = true) }
  }
}
