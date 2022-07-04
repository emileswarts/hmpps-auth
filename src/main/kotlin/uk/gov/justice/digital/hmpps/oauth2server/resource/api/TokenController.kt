package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService

@RestController
@Validated
class TokenController(private val tokenService: TokenService) {

  @PreAuthorize("hasRole('ROLE_CREATE_EMAIL_TOKEN')")
  @PostMapping("/api/new-token")
  fun createNewTokenByUserName(
    @RequestBody createTokenRequest: CreateTokenRequest
  ): String {
    return tokenService.createTokenForNewUser(
      UserToken.TokenType.RESET,
      createTokenRequest.username, createTokenRequest.email, createTokenRequest.source
    )
  }
}
