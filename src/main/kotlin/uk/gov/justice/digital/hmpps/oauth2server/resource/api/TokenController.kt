package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Validated
@Tag(name = "/api/new-token", description = "Token Controller")
class TokenController(private val tokenService: TokenService) {

  @PreAuthorize("hasRole('ROLE_CREATE_EMAIL_TOKEN')")
  @PostMapping("/api/new-token")
  @Operation(
    summary = "Generates new token for DPS user",
    description = "Generates new token for DPS user.  Requires ROLE_CREATE_EMAIL_TOKEN."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun createNewTokenByUserName(
    @Valid @RequestBody
    createTokenRequest: CreateTokenRequest
  ): String {
    return tokenService.createTokenForNewUser(
      UserToken.TokenType.RESET,
      createTokenRequest
    )
  }

  @PreAuthorize("hasRole('ROLE_CREATE_EMAIL_TOKEN')")
  @PostMapping("/api/token/email-type")
  @Operation(
    summary = "Generates new token for existing user by email type",
    description = "Generates new token for existing user by email type.  Requires ROLE_CREATE_EMAIL_TOKEN."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun createTokenByEmailType(
    @Valid @RequestBody
    tokenByEmailTypeRequest: TokenByEmailTypeRequest
  ) = tokenService.createTokenByEmailType(tokenByEmailTypeRequest)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to create User Token by user name and email type")
data class TokenByEmailTypeRequest(
  @Schema(description = "Username", example = "DEMO_USER1", required = true) @field:Size(
    max = 60,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,

  @Schema(description = "Email type", example = "PRIMARY or SECONDARY", required = true)
  @NotBlank val emailType: EmailType,

)
