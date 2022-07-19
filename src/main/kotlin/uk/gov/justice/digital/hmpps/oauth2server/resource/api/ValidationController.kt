package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService

@RestController
@Validated
class ValidationController(
  private val verifyEmailService: VerifyEmailService
) {
  @GetMapping("/api/validate/email-domain")
  @ApiOperation(
    value = "Validates Email domain",
    notes =
    """
      Validates Email domain.
    """,
    nickname = "isValidEmailDomain",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = Boolean::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)
    ]
  )
  fun isValidEmailDomain(@RequestParam(value = "emailDomain", required = true) emailDomain: String): Boolean =
    verifyEmailService.validateEmailDomainExcludingGsi(emailDomain)
}
