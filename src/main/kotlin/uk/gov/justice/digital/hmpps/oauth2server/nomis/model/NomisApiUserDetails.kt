package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class NomisApiUserDetails @JsonCreator constructor(
  @JsonProperty("staffId") val staffId: String,
  @JsonProperty("username") val username: String,
  @JsonProperty("lastName") val surname: String,
  @JsonProperty("firstName") val firstName: String,
  @JsonProperty("primaryEmail") val email: String?,
  @JsonProperty("active") val enabled: Boolean,
  @JsonProperty("dpsRoleCodes") val roles: List<String>,
)
