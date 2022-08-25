package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter
import uk.gov.justice.digital.hmpps.oauth2server.model.EmailAddress
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.model.UserDetail
import uk.gov.justice.digital.hmpps.oauth2server.model.UserRole
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.security.Principal
import java.time.LocalDateTime

@RestController
@Tag(name = "/api/user", description = "User Controller")
class UserController(private val userService: UserService) {

  @GetMapping("/api/user/me")
  @Operation(
    summary = "Current user detail.",
    description = "Current user detail."
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
  fun me(@Parameter(hidden = true) principal: Principal): UserDetail {
    val upd = userService.findMasterUserPersonDetails(principal.name)
    return upd.map {
      val user = userService.getOrCreateUser(principal.name).orElseThrow()
      UserDetail.fromPerson(it, user)
    }.orElse(UserDetail.fromUsername(principal.name))
  }

  @GetMapping("/api/user/me/roles")
  @Operation(
    summary = "List of roles for current user.",
    description = "List of roles for current user."
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
  fun myRoles(@Parameter(hidden = true) authentication: Authentication): Collection<UserRole> =
    authentication.authorities.map { UserRole(it!!.authority.substring(5)) } // remove ROLE_

  @GetMapping("/api/me/email")
  @Operation(
    summary = "Email address for current user",
    description = "Verified email address for current user"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "204",
        description = "No content.  No verified email address found for user.  Only if unverified not supplied or set to false"
      )
    ]
  )
  fun myEmail(
    @Parameter(description = "Return unverified email addresses.", required = false)
    @RequestParam
    unverified: Boolean = false,
    @Parameter(hidden = true) principal: Principal
  ): ResponseEntity<*> = getUserEmail(username = principal.name, unverified = unverified)

  @GetMapping("/api/user/me/mfa")
  @Operation(
    summary = "MFA options configured for current user",
    description = "MFA options configured for current user"
  )
  fun myMfa(@Parameter(hidden = true) principal: Principal): MfaOptions = userService
    .getOrCreateUser(principal.name)
    .map {
      MfaOptions(
        emailVerified = it.verified,
        mobileVerified = it.isMobileVerified,
        backupVerified = it.isSecondaryEmailVerified
      )
    }
    .orElseThrow { UsernameNotFoundException("Account for username ${principal.name} not found") }

  data class MfaOptions(
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val backupVerified: Boolean
  )

  @GetMapping("/api/user/{username}")
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
    @Parameter(description = "The username of the user.", required = true) @PathVariable
    username: String
  ): UserDetail {
    val upd = userService.findMasterUserPersonDetails(username)
      .orElseThrow { UsernameNotFoundException("Account for username $username not found") }
    val user = userService.getOrCreateUser(username).orElseThrow()
    return UserDetail.fromPerson(upd, user)
  }

  @GetMapping("/api/user/{username}/roles")
  @Operation(
    summary = "List of roles for user.",
    description = "List of roles for user. Currently restricted to service specific roles: ROLE_INTEL_ADMIN or ROLE_PCMS_USER_ADMIN."
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
  @PreAuthorize("hasAnyRole('ROLE_INTEL_ADMIN', 'ROLE_PCMS_USER_ADMIN')")
  fun userRoles(
    @Parameter(description = "The username of the user.", required = true) @PathVariable
    username: String
  ): Collection<UserRole> {
    val user = userService.findMasterUserPersonDetails(username)
      .orElseThrow { UsernameNotFoundException("Account for username $username not found") }
    return user.authorities.map { UserRole(it!!.authority.substring(5)) }.toSet() // remove ROLE_
  }

  @GetMapping("/api/user/{username}/email")
  @Operation(
    summary = "Email address for user",
    description = "Verified email address for user"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "204",
        description = "No content.  No verified email address found for user"
      ),
      ApiResponse(
        responseCode = "404",
        description = "User not found. The user doesn't exist in auth so could have never logged in",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun getUserEmail(
    @Parameter(description = "The username of the user.", required = true) @PathVariable
    username: String,
    @Parameter(description = "Return unverified email addresses.", required = false)
    @RequestParam
    unverified: Boolean = false
  ): ResponseEntity<*> = userService
    .getOrCreateUser(username)
    .map { user: User ->
      if (user.verified || unverified) ResponseEntity.ok(EmailAddress(user)) else ResponseEntity.noContent()
        .build<Any>()
    }
    .orElseGet { notFoundResponse(username) }

  @Deprecated("Please use /api/authuser/email instead")
  @PostMapping("/api/user/email")
  @Operation(
    summary = "Email address for users",
    description =
    """Verified email address for users.  Post version that accepts multiple email addresses.
        Requires ROLE_MAINTAIN_ACCESS_ROLES or ROLE_MAINTAIN_ACCESS_ROLES_ADMIN.
    """
  )
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  fun getUserEmails(
    @Parameter(description = "List of usernames.", required = true) @RequestBody
    usernames: List<String>
  ): List<EmailAddress> = userService
    .getOrCreateUsers(usernames)
    .filter { it.verified }
    .map { EmailAddress(it) }

  private fun notFoundResponse(username: String): ResponseEntity<Any?> = ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ErrorDetail("Not Found", "Account for username $username not found", "username"))

  @GetMapping("/api/users/email")
  @Operation(
    summary = "Email address for all users",
    description = "Return primary email address for all users."
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
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS')")
  fun getAllUserEmails(
    @Parameter(description = "A single auth source to search [nomis|delius|auth|azuread]. Defaults to auth if omitted.")
    @RequestParam(
      required = false
    )
    authSource: AuthSource?
  ): List<EmailAddress> = userService
    .findUsersBySource(authSource ?: AuthSource.auth)
    .filter { it.verified }
    .map { EmailAddress(it) }

  @GetMapping("/api/user/search")
  @Operation(
    summary = "Search for users ",
    description = """
      Search for users in the Auth DB who match on partial first name, surname, username or email and return a pageable result set. 
      Optionally choose the authentication sources from any combination of auth, delius, nomis and azuread sources.
      It will default to AuthSource.auth if the authSources parameter is omitted.
      Provide the authSources as a list of values with the same name. e.g. ?authSources=nomis&authSources=delius&authSources=auth
      It will return users with the requested auth sources where they have authenticated against the auth service at least once.
      Note: User information held in the auth service may be out of date with the user information held in the source systems as
      their details will be as they were the last time that they authenticated.
    """
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
  @PreAuthorize(
    "hasAnyRole('ROLE_INTEL_ADMIN', 'ROLE_PCMS_USER_ADMIN')"
  )
  fun searchForUsersInMultipleSourceSystems(
    @Parameter(
      description = "The username, email or name of the user.",
      example = "j smith"
    ) @RequestParam(required = false)
    name: String?,
    @Parameter(description = "User status to find ACTIVE, INACTIVE or ALL. Defaults to ALL if omitted.") @RequestParam(
      required = false,
      defaultValue = "ALL"
    )
    status: UserFilter.Status,
    @Parameter(description = "List of auth sources to search [nomis|delius|auth|azuread]. Defaults to auth if omitted.") @RequestParam(
      required = false
    )
    authSources: List<AuthSource>?,
    @PageableDefault(sort = ["Person.lastName", "Person.firstName"], direction = Sort.Direction.ASC) pageable: Pageable,
    @Parameter(hidden = true) authentication: Authentication
  ): Page<AuthUserWithSource> =
    userService.searchUsersInMultipleSourceSystems(
      name,
      pageable,
      authentication.name,
      authentication.authorities,
      status,
      authSources
    )
      .map { AuthUserWithSource.fromUser(it) }
}

data class AuthUserWithSource(
  @Schema(
    required = true,
    description = "User ID",
    example = "91229A16-B5F4-4784-942E-A484A97AC865"
  )
  val userId: String? = null,

  @Schema(required = true, description = "Username", example = "authuser")
  val username: String? = null,

  @Schema(
    required = true,
    description = "Email address",
    example = "auth.user@someagency.justice.gov.uk"
  )
  val email: String? = null,

  @Schema(required = true, description = "First name", example = "Auth")
  val firstName: String? = null,

  @Schema(required = true, description = "Last name", example = "User")
  val lastName: String? = null,

  @Schema(
    required = true,
    description = "Account is locked due to incorrect password attempts",
    example = "true"
  )
  @Order(6)
  val locked: Boolean = false,

  @Schema(required = true, description = "Account is enabled", example = "false")
  val enabled: Boolean = false,

  @Schema(required = true, description = "Email address has been verified", example = "false")
  val verified: Boolean = false,

  @Schema(required = true, description = "Last time user logged in", example = "01/01/2001")
  val lastLoggedIn: LocalDateTime? = null,

  @Schema(required = true, description = "Authentication source", example = "delius")
  val source: AuthSource = AuthSource.auth

) {
  companion object {
    fun fromUser(user: User): AuthUserWithSource {
      return AuthUserWithSource(
        userId = user.id.toString(),
        username = user.username,
        email = user.email,
        firstName = user.firstName,
        lastName = user.person?.lastName,
        locked = user.locked,
        enabled = user.isEnabled,
        verified = user.verified,
        lastLoggedIn = user.lastLoggedIn,
        source = user.source
      )
    }
  }
}
