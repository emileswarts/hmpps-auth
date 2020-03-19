package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
data class Contact(@Enumerated(EnumType.STRING) var type: ContactType) {
  constructor(type: ContactType, value: String, verified: Boolean = false) : this(type) {
    this.value = value
    this.verified = verified
  }

  var value: String? = null
  var verified: Boolean = false
}

enum class ContactType {
  SECONDARY_EMAIL, MOBILE_PHONE
}