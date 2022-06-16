package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import org.apache.commons.lang3.StringUtils
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "OAUTH_CLIENT_CONFIG")
data class ClientConfig(

  @Id
  @Column(name = "base_client_id", nullable = false)
  var baseClientId: String,

  @Column(name = "allowed_ips")
  @Convert(converter = StringListConverter::class)
  var ips: List<String> = emptyList(),

  @Column(name = "client_end_date")
  var clientEndDate: LocalDate? = null,

  @Transient
  var validDays: Long? = null,

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
