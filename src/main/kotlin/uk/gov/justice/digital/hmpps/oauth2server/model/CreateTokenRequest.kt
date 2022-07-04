package uk.gov.justice.digital.hmpps.oauth2server.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Token creation  request")
@ApiModel(description = "Request to create Token")
data class CreateTokenRequest(
  @ApiModelProperty(required = true, value = "Username", example = "DEMO_USER1", position = 1)
  @Schema(description = "Username", example = "DEMO_USER1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,

  @ApiModelProperty(required = true, value = "Email", example = "john.smith@digital.justice.gov.uk", position = 2)
  @Schema(description = "DPS Email Address of user", example = "test@justice.gov.uk", required = true)
  @field:Email(
    message = "Not a valid email address"
  )
  @NotBlank val email: String,

  @ApiModelProperty(required = true, value = "AuthSource", example = "auth or azuread or delius or nomis or none", position = 3)
  @Schema(description = "AuthSource should be either nomis or delius", example = "nomis", required = true)
  @NotBlank val source: AuthSource,
)
