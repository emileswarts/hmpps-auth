package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail

@RestController
@Tag(name = "/api/authroles", description = "** IMPORTANT ** Calls to all /api/authroles endpoints are now deprecated. The endpoints have been moved to the mange-users-api service.")
@Deprecated(
  message = "Role endpoints now use the mange-users-api service",
  replaceWith = ReplaceWith("/{manage-users-api}/roles?adminTypes=EXT_ADM"),
  level = DeprecationLevel.WARNING
)
class AuthAllRolesController(private val authUserRoleService: AuthUserRoleService) {
  @GetMapping("/api/authroles")
  @Operation(
    summary = "Get all possible roles.",
    description = "Get all roles allowed for auth users."
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
  fun allRoles(): List<AuthUserRole> =
    authUserRoleService.allRoles.map { AuthUserRole(it) }
}
