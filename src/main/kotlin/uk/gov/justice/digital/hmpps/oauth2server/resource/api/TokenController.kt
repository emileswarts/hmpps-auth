package uk.gov.justice.digital.hmpps.oauth2server.resource.api

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
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import javax.validation.Valid

@RestController
@Validated
@Tag(name = "token-controller", description = "Token Controller")
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
}
