package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class NomisUserDetails @JsonCreator constructor(
  @JsonProperty("staffId") val staffId: String,
  @JsonProperty("username") val username: String,
  @JsonProperty("lastName") val surname: String,
  @JsonProperty("firstName") val firstName: String,
  @JsonProperty("activeCaseloadId") val activeCaseloadId: String?,
  @JsonProperty("primaryEmail") val email: String?,
  @JsonProperty("dpsRoleCodes") val roles: List<String>,
  @JsonProperty("accountStatus") val accountStatus: AccountStatus,
  @JsonProperty("accountNonLocked") val accountNonLocked: Boolean,
  @JsonProperty("credentialsNonExpired") val credentialsNonExpired: Boolean,
  @JsonProperty("enabled") val enabled: Boolean,
  @JsonProperty("admin") val admin: Boolean,
  @JsonProperty("active") val active: Boolean,
  @JsonProperty("staffStatus") val staffStatus: String?,
)
