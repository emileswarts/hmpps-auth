package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "OAUTH_CODE")
data class OauthCode(
  @Id
  @Column(nullable = false)
  val code: String,
) {

  @Lob
  @Type(type = "org.hibernate.type.BinaryType")
  @Column(nullable = false)
  var authentication: ByteArray? = null

  @Column(name = "created_date", nullable = false)
  var createdDate: LocalDateTime? = null
}
