package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.azure.service.AzureUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.model.UserDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.util.UUID

@RestController
@Validated
@Tag(name = "/api/azureuser", description = "Azure User Controller")
class AzureUserController(
  private val userService: UserService,
  private val azureUserService: AzureUserService
) {
  @GetMapping("/api/azureuser/{username}")
  @Operation(
    summary = "User detail.",
    description = "User detail."
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
  fun user(
    @Parameter(description = "The username of the azure user.", required = true)
    @PathVariable
    username: UUID
  ): UserDetail {
    val upd = azureUserService.getAzureUserByUsername(username.toString().uppercase()).map { UserPersonDetails::class.java.cast(it) }
      .orElseThrow { UsernameNotFoundException("Account for username $username not found") }
    val user = userService.getOrCreateUser(username.toString()).orElseThrow()
    return UserDetail.fromPerson(upd, user)
  }
}
