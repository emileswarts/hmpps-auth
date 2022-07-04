package uk.gov.justice.digital.hmpps.oauth2server.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Token creation request")
data class CreateTokenRequest(
  @NotBlank val username: String,
  @NotBlank val email: String,
  @NotBlank val source: AuthSource,
)
