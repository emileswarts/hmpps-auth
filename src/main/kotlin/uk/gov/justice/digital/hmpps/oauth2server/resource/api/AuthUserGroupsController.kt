package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail

@RestController
@Api(tags = ["/api/authuser/{username}/groups", "/api/authuser/id/{userId}/groups"])
class AuthUserGroupsController(
  private val authUserService: AuthUserService,
  private val authUserGroupService: AuthUserGroupService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/api/authuser/{username}/groups")
  @ApiOperation(
    value = "Get groups for user.",
    nickname = "groups",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  fun groups(
    @ApiParam(value = "The username of the user.", required = true)
    @PathVariable username: String,
    @ApiParam(value = "Whether groups are expanded into their children.", required = false)
    @RequestParam(defaultValue = "true") children: Boolean = true,
  ): List<AuthUserGroup> = authUserGroupService.getAuthUserGroups(username)
    ?.flatMap { g ->
      if (children && g.children.isNotEmpty()) g.children.map { AuthUserGroup(it) }
      else listOf(AuthUserGroup(g))
    }
    ?: throw UsernameNotFoundException("User $username not found")

  @GetMapping("/api/authuser/id/{userId}/groups")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Get groups for userId.",
    nickname = "groups",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  fun groupsByUserId(
    @ApiParam(value = "The userId of the user.", required = true)
    @PathVariable userId: String,
    @ApiParam(value = "Whether groups are expanded into their children.", required = false)
    @RequestParam(defaultValue = "true") children: Boolean = true,
    @ApiIgnore authentication: Authentication,
  ): List<AuthUserGroup> =
    authUserGroupService.getAuthUserGroupsByUserId(userId, authentication.name, authentication.authorities)
      ?.flatMap { g ->
        if (children && g.children.isNotEmpty()) g.children.map { AuthUserGroup(it) }
        else listOf(AuthUserGroup(g))
      }
      ?: throw UsernameNotFoundException("User $userId not found")

  @PutMapping("/api/authuser/id/{userId}/groups/{group}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Add group to user.",
    notes = "Add group to user.",
    nickname = "addGroup",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class),
      ApiResponse(code = 409, message = "Group for user already exists.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to insert row.", response = ErrorDetail::class)
    ]
  )
  fun addGroupByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiParam(value = "The group to be added to the user.", required = true) @PathVariable group: String,
    @ApiIgnore authentication: Authentication,
  ) {
    authUserGroupService.addGroupByUserId(userId, group, authentication.name, authentication.authorities)
    log.info("Add group succeeded for userId {} and group {}", userId, group)
  }

  @DeleteMapping("/api/authuser/id/{userId}/groups/{group}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Remove group from user.",
    notes = "Remove group from user.",
    nickname = "removeGroup",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to insert row.", response = ErrorDetail::class)
    ]
  )
  fun removeGroupByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiParam(value = "The group to be delete from the user.", required = true) @PathVariable group: String,
    @ApiIgnore authentication: Authentication,
  ) {
    authUserGroupService.removeGroupByUserId(userId, group, authentication.name, authentication.authorities)
    log.info("Remove group succeeded for userId {} and group {}", userId, group)
  }

  private fun notFoundResponse(username: String): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ErrorDetail("Not Found", "Account for username $username not found", "username"))
}
