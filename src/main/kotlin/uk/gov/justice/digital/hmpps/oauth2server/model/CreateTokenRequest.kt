package uk.gov.justice.digital.hmpps.oauth2server.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to create Token")
data class CreateTokenRequest(
  @Schema(description = "Username", example = "DEMO_USER1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,

  @Schema(description = "Email Address of user", example = "test@justice.gov.uk", required = true)
  @field:Email(
    message = "Not a valid email address"
  )
  @NotBlank val email: String,

  @Schema(description = "AuthSource should be auth or azuread or delius or nomis or none", example = "nomis", required = true)
  @NotBlank val source: AuthSource,

  @Schema(description = "First name of the user", example = "John", required = true)
  @NotBlank val firstName: String,

  @Schema(description = "Last name of the user", example = "Smith", required = true)
  @NotBlank val lastName: String,
)
