package uk.gov.justice.digital.hmpps.oauth2server.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User Role")
data class UserRole(
  @Schema(required = true, description = "Role Code", example = "GLOBAL_SEARCH")
  val roleCode: String,
)
