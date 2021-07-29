package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import springfox.documentation.annotations.ApiIgnore
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import javax.validation.constraints.NotEmpty

@RestController
@Api(tags = ["/api/authuser/{username}/roles"])
class AuthUserRolesController(
  private val authUserService: AuthUserService,
  private val authUserRoleService: AuthUserRoleService,
) {

  @GetMapping("/api/authuser/id/{userId}/roles")
  @ApiOperation(
    value = "Get roles for user.",
    notes = "Get roles for user.",
    nickname = "roles",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  fun rolesByUserId(
    @ApiParam(
      value = "The userId of the user.",
      required = true
    ) @PathVariable userId: String,
    @ApiIgnore authentication: Authentication,
  ): Set<AuthUserRole> =
    authUserService.getAuthUserByUserId(userId, authentication.name, authentication.authorities)
      ?.let { u: User -> u.authorities.map { AuthUserRole(it) }.toSet() }
      ?: throw UsernameNotFoundException("User $userId not found")

  @GetMapping("/api/authuser/id/{userId}/assignable-roles")
  @ApiOperation(
    value = "Get list of assignable roles.",
    notes = "Get list of roles that can be assigned by the current user.  This is dependent on the group membership, although super users can assign any role",
    nickname = "assignableRoles",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  fun assignableRolesByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiIgnore authentication: Authentication,
  ): List<AuthUserRole> {
    val roles = authUserRoleService.getAssignableRolesByUserId(userId, authentication.authorities)
    return roles.map { AuthUserRole(it) }
  }

  @PutMapping("/api/authuser/id/{userId}/roles/{role}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Add role to user.",
    notes = "Add role to user.",
    nickname = "addRole",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class),
      ApiResponse(code = 409, message = "Role for user already exists.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to insert row.", response = ErrorDetail::class)
    ]
  )
  fun addRoleByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiParam(value = "The role to be added to the user.", required = true) @PathVariable role: String,
    @ApiIgnore authentication: Authentication,
  ) {
    authUserRoleService.addRolesByUserId(userId, listOf(role), authentication.name, authentication.authorities)
    log.info("Add role succeeded for userId {} and role {}", userId, role)
  }

  @DeleteMapping("/api/authuser/id/{userId}/roles/{role}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Remove role from user.",
    notes = "Remove role from user.",
    nickname = "removeRole",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 204, message = "Removed"),
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to insert row.", response = ErrorDetail::class)
    ]
  )
  fun removeRoleByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiParam(value = "The role to be delete from the user.", required = true) @PathVariable role: String,
    @ApiIgnore authentication: Authentication,
  ) {
    authUserRoleService.removeRoleByUserId(userId, role, authentication.name, authentication.authorities)
    log.info("Remove role succeeded for userId {} and role {}", userId, role)
  }

  @PostMapping("/api/authuser/id/{userId}/roles")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Add roles to user.",
    notes = "Add role to user, post version taking multiple roles",
    nickname = "addRole",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class),
      ApiResponse(code = 409, message = "Role(s) for user already exists.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to insert row.", response = ErrorDetail::class)
    ]
  )
  fun addRolesByUserId(
    @ApiParam(value = "The username of the user.", required = true) @PathVariable userId: String,
    @ApiParam(value = "List of roles to be assigned.", required = true) @RequestBody @NotEmpty roles: List<String>,
    @ApiIgnore authentication: Authentication,
  ) {
    authUserRoleService.addRolesByUserId(userId, roles, authentication.name, authentication.authorities)
    log.info("Add role succeeded for userId {} and roles {}", userId, roles.toString())
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
