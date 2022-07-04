package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
class TokenController(private val tokenService: TokenService) {

  @PreAuthorize("hasRole('ROLE_CREATE_EMAIL_TOKEN')")
  @PostMapping("/api/new-token")
  @ApiOperation(
    value = "Generates new token for DPS user",
    notes =
    """
      Generates new token for DPS user.  Requires ROLE_CREATE_EMAIL_TOKEN.
    """,
    nickname = "createNewTokenByUserName",
    consumes = "application/json",
    produces = "text/plain;charset=UTF-8"
  )
  @ApiResponses(value = [ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)])
  fun createNewTokenByUserName(
    @Valid @RequestBody createTokenRequest: CreateTokenRequest
  ): String {
    return tokenService.createTokenForNewUser(
      UserToken.TokenType.RESET,
      createTokenRequest.username, createTokenRequest.email, createTokenRequest.source
    )
  }
}
