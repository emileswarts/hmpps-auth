package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService

@RestController
@Validated
@Tag(name = "/api/validate", description = "Validation Controller")
class ValidationController(
  private val verifyEmailService: VerifyEmailService
) {
  @GetMapping("/api/validate/email-domain")
  @Deprecated(
    message = "Validate email domains now uses the mange-users-api service",
    replaceWith = ReplaceWith("/{manage-users-api}/validate/email-domain"),
    level = DeprecationLevel.WARNING
  )
  @Operation(
    summary = "Validates Email domain",
    description = "Validates Email domain."
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
  fun isValidEmailDomain(@RequestParam(value = "emailDomain", required = true) emailDomain: String) =
    verifyEmailService.validateEmailDomainExcludingGsi(emailDomain)
}
