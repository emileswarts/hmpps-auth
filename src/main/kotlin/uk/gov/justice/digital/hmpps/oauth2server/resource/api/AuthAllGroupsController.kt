package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail

@RestController
@Tag(name = "/api/authgroups", description = "** IMPORTANT ** Calls to the /api/authgroups endpoint are now deprecated. The endpoint has been moved to the mange-users-api service.")
@Deprecated(
  message = "Groups endpoints now use the mange-users-api service",
  replaceWith = ReplaceWith("/{manage-users-api}/groups"),
  level = DeprecationLevel.WARNING
)
class AuthAllGroupsController(private val authUserGroupService: AuthUserGroupService) {
  @GetMapping("/api/authgroups")
  @Operation(
    summary = "Get all possible groups.",
    description = "Get all groups allowed for auth users."
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
  fun allGroups(): List<AuthUserGroup> =
    authUserGroupService.allGroups.map { AuthUserGroup(it) }
}
