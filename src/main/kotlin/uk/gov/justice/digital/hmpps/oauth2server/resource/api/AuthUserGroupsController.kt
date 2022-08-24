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
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail

@RestController
@Tag(name = "/api/authuser/id/{userId}/groups", description = "Auth User Groups Controller")
class AuthUserGroupsController(
  private val authUserService: AuthUserService,
  private val authUserGroupService: AuthUserGroupService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/api/authuser/{username}/groups")
  @Operation(
    summary = "Get groups for user.",
    description = "Get groups for user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "401", description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404", description = "User not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun groups(
    @Parameter(description = "The username of the user.", required = true)
    @PathVariable username: String,
    @Parameter(description = "Whether groups are expanded into their children.", required = false)
    @RequestParam(defaultValue = "true") children: Boolean = true,
  ): List<AuthUserGroup> = authUserGroupService.getAuthUserGroups(username)
    ?.flatMap { g ->
      if (children && g.children.isNotEmpty()) g.children.map { AuthUserGroup(it) }
      else listOf(AuthUserGroup(g))
    }
    ?: throw UsernameNotFoundException("User $username not found")

  @GetMapping("/api/authuser/id/{userId}/groups")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Get groups for userId.",
    description = "Get groups for userId."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "401", description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404", description = "User not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun groupsByUserId(
    @Parameter(description = "The userId of the user.", required = true)
    @PathVariable userId: String,
    @Parameter(description = "Whether groups are expanded into their children.", required = false)
    @RequestParam(defaultValue = "true") children: Boolean = true,
    @Parameter(hidden = true) authentication: Authentication,
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
  @Operation(
    summary = "Add group to user.",
    description = "Add group to user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Added"
      ),
      ApiResponse(
        responseCode = "400", description = "Validation failed.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401", description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404", description = "User not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "409", description = "Group for user already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500", description = "Server exception e.g. failed to insert row.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun addGroupByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable userId: String,
    @Parameter(description = "The group to be added to the user.", required = true) @PathVariable group: String,
    @Parameter(hidden = true) authentication: Authentication,
  ) {
    authUserGroupService.addGroupByUserId(userId, group, authentication.name, authentication.authorities)
    log.info("Add group succeeded for userId {} and group {}", userId, group)
  }

  @DeleteMapping("/api/authuser/id/{userId}/groups/{group}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Remove group from user.",
    description = "Remove group from user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Deleted"
      ),
      ApiResponse(
        responseCode = "400", description = "Validation failed.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401", description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404", description = "User not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500", description = "Server exception e.g. failed to insert row.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun removeGroupByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable userId: String,
    @Parameter(description = "The group to be delete from the user.", required = true) @PathVariable group: String,
    @Parameter(hidden = true) authentication: Authentication,
  ) {
    authUserGroupService.removeGroupByUserId(userId, group, authentication.name, authentication.authorities)
    log.info("Remove group succeeded for userId {} and group {}", userId, group)
  }

  private fun notFoundResponse(username: String): ResponseEntity<Any> = ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ErrorDetail("Not Found", "Account for username $username not found", "username"))
}
