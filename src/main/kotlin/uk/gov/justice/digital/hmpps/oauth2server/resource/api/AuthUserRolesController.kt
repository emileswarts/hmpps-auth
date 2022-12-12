package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import javax.validation.constraints.NotEmpty

@RestController
@Tag(
  name = "/api/authuser/id/{userId}/roles",
  description = "** IMPORTANT ** Calls to all /api/authuser/id/{userId}/roles endpoints are now deprecated. " +
    "The endpoints (excluding the PutMapping to add a single role, which is no longer supported) have been moved to the mange-users-api service."
)
@Deprecated(
  message = "User roles endpoints now use the mange-users-api service",
  replaceWith = ReplaceWith("/{manage-users-api}/users/{userId}/roles"),
  level = DeprecationLevel.WARNING
)
class AuthUserRolesController(
  private val authUserService: AuthUserService,
  private val authUserRoleService: AuthUserRoleService
) {

  @GetMapping("/api/authuser/id/{userId}/roles")
  @Operation(
    summary = "Get roles for user.",
    description = "Get roles for user."
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
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  fun rolesByUserId(
    @Parameter(
      description = "The userId of the user.",
      required = true
    ) @PathVariable
    userId: String,
    @Parameter(hidden = true) authentication: Authentication
  ): Set<AuthUserRole> =
    authUserService.getAuthUserByUserId(userId, authentication.name, authentication.authorities)
      ?.let { u: User -> u.authorities.map { AuthUserRole(it) }.toSet() }
      ?: throw UsernameNotFoundException("User $userId not found")

  @GetMapping("/api/authuser/id/{userId}/assignable-roles")
  @Operation(
    summary = "Get list of assignable roles.",
    description = "Get list of roles that can be assigned by the current user.  This is dependent on the group membership, although super users can assign any role"
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
  fun assignableRolesByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(hidden = true) authentication: Authentication
  ): List<AuthUserRole> {
    val roles = authUserRoleService.getAssignableRolesByUserId(userId, authentication.authorities)
    return roles.map { AuthUserRole(it) }
  }

  @PutMapping("/api/authuser/id/{userId}/roles/{role}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Add role to user.",
    description = "Add role to user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Added."
      ),
      ApiResponse(
        responseCode = "400",
        description = "Validation failed.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
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
      ),
      ApiResponse(
        responseCode = "409",
        description = "Role for user already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Server exception e.g. failed to insert row.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun addRoleByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(description = "The role to be added to the user.", required = true) @PathVariable
    role: String,
    @Parameter(hidden = true) authentication: Authentication
  ) {
    authUserRoleService.addRolesByUserId(userId, listOf(role), authentication.name, authentication.authorities)
    log.info("Add role succeeded for userId {} and role {}", userId, role)
  }

  @DeleteMapping("/api/authuser/id/{userId}/roles/{role}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Remove role from user.",
    description = "Remove role from user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Removed."
      ),
      ApiResponse(
        responseCode = "400",
        description = "Validation failed.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
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
      ),
      ApiResponse(
        responseCode = "500",
        description = "Server exception e.g. failed to insert row.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun removeRoleByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(description = "The role to be delete from the user.", required = true) @PathVariable
    role: String,
    @Parameter(hidden = true) authentication: Authentication
  ) {
    authUserRoleService.removeRoleByUserId(userId, role, authentication.name, authentication.authorities)
    log.info("Remove role succeeded for userId {} and role {}", userId, role)
  }

  @PostMapping("/api/authuser/id/{userId}/roles")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Add roles to user.",
    description = "Add role to user, post version taking multiple roles"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Added."
      ),
      ApiResponse(
        responseCode = "400",
        description = "Validation failed.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
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
      ),
      ApiResponse(
        responseCode = "409",
        description = "Role(s) for user already exists..",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Server exception e.g. failed to insert row.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun addRolesByUserId(
    @Parameter(description = "The username of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(
      description = "List of roles to be assigned.",
      required = true
    ) @RequestBody @NotEmpty
    roles: List<String>,
    @Parameter(hidden = true) authentication: Authentication
  ) {
    authUserRoleService.addRolesByUserId(userId, roles, authentication.name, authentication.authorities)
    log.info("Add role succeeded for userId {} and roles {}", userId, roles.toString())
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
