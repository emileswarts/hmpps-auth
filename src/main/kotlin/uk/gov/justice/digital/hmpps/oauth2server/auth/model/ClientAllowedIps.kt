package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import org.apache.commons.lang3.StringUtils
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "OAUTH_CLIENT_ALLOWED_IPS")
data class ClientAllowedIps(

  @Id
  @Column(name = "base_client_id", nullable = false)
  val baseClientId: String,

  @Column(name = "allowed_ips")
  @Convert(converter = StringListConverter::class)
  var ips: List<String> = emptyList()
) {

  var allowedIpsWithNewlines: String?
    get() = ips?.joinToString("\n")
    set(allowedIpsWithNewlines) {
      ips = allowedIpsWithNewlines
        ?.replace("""\s+""".toRegex(), ",")
        ?.split(',')
        ?.mapNotNull { StringUtils.trimToNull(it) }
        ?.toList() as List<String>
    }
}
