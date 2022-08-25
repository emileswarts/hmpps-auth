package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService

@RestController
@Tag(name = "/api/user/{username}/contacts")
class UserContactsController(private val userService: UserService) {
  @GetMapping("/api/user/{username}/contacts")
  @PreAuthorize("hasRole('ROLE_RETRIEVE_OAUTH_CONTACTS')")
  @Operation(
    summary = "Get contacts for user.",
    description = "Get verified contacts for user."
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
  fun contacts(
    @Parameter(
      description = "The username of the user.",
      required = true
    ) @PathVariable
    username: String
  ): List<ContactDto> {
    val user = userService.getUserWithContacts(username)
    return user.contacts.filter { it.verified }.map { ContactDto(it.value!!, it.type.name, it.type.description) }
  }
}

data class ContactDto(
  @Schema(required = true, example = "01234 23451234")
  val value: String,
  @Schema(required = true, example = "SECONDARY_EMAIL")
  val type: String,
  @Schema(required = true, example = "Mobile Phone")
  val typeDescription: String
)
