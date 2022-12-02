package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter.Status
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService.CreateUserException
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole
import uk.gov.justice.digital.hmpps.oauth2server.model.EmailAddress
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.utils.EmailHelper
import uk.gov.justice.digital.hmpps.oauth2server.utils.removeAllCrLf
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.ValidEmailException
import uk.gov.service.notify.NotificationClientException
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Tag(
  name = "/api/authuser",
  description = "** IMPORTANT ** Calls to some endpoint in Auth User Controller are now deprecated. " +
    "The endpoints have been moved to the mange-users-api service."
)

class AuthUserController(
  private val userService: UserService,
  private val authUserService: AuthUserService,
  private val authUserGroupService: AuthUserGroupService,
  private val authUserRoleService: AuthUserRoleService,
  @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/api/authuser/{username}")
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
  ): ResponseEntity<Any?> {
    val user = authUserService.getAuthUserByUsername(username)
    return user.map { AuthUser.fromUser(it) }
      .map { Any::class.java.cast(it) }
      .map { ResponseEntity.ok(it) }
      .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundBody(username)))
  }

  @GetMapping("/api/authuser/id/{userId}")
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
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  fun getUserById(
    @Parameter(description = "The ID of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(hidden = true) authentication: Authentication
  ): AuthUser {
    return authUserService.getAuthUserByUserId(userId, authentication.name, authentication.authorities)
      ?.let { AuthUser.fromUser(it) }
      ?: throw UsernameNotFoundException("User $userId not found")
  }

  @GetMapping("/api/authuser")
  @Operation(
    summary = "Search for a user.",
    description = "Search for a user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AuthUser::class)
            // NOT SURE ABOUT THIS responseContainer = "List"
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
        description = "No users found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun searchForUser(
    @Parameter(description = "The email address of the user.", required = true) @RequestParam
    email: String?
  ): ResponseEntity<Any> {
    val users = authUserService.findAuthUsersByEmail(email).map { AuthUser.fromUser(it) }
    return if (users.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok(users)
  }

  @GetMapping("/api/authuser/search")
  @Operation(
    summary = "Search for a user."
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
    "hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')"
  )
  fun searchForUser(
    @Parameter(
      description = "The username, email or name of the user.",
      example = "j smith"
    ) @RequestParam(required = false)
    name: String?,
    @Parameter(description = "The role codes of the user.") @RequestParam(required = false)
    roles: List<String>?,
    @Parameter(description = "The group codes of the user.") @RequestParam(required = false)
    groups: List<String>?,
    @Parameter(description = "Limit to active / inactive / show all users.") @RequestParam(
      required = false,
      defaultValue = "ALL"
    )
    status: Status,
    @PageableDefault(sort = ["Person.lastName", "Person.firstName"], direction = Sort.Direction.ASC) pageable: Pageable,
    @Parameter(hidden = true) authentication: Authentication
  ): Page<AuthUser> =
    authUserService.findAuthUsers(
      name,
      roles,
      groups,
      pageable,
      authentication.name,
      authentication.authorities,
      status
    )
      .map { AuthUser.fromUser(it) }

  @GetMapping("/api/authuser/me/assignable-groups")
  @Operation(
    summary = "Get list of assignable groups.",
    description = "Get list of groups that can be assigned by the current user."
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
  fun assignableGroups(@Parameter(hidden = true) authentication: Authentication): List<AuthUserGroup> {
    val groups = authUserGroupService.getAssignableGroups(authentication.name, authentication.authorities)
    return groups.map { AuthUserGroup(it) }
  }

  @GetMapping("/api/authuser/me/searchable-roles")
  @Operation(
    summary = "Get list of searchable roles.",
    description = "Get list of roles that can be search for by the current user."
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
  fun searchableRoles(@Parameter(hidden = true) authentication: Authentication): List<AuthUserRole> {
    val roles = authUserRoleService.getAllAssignableRoles(authentication.name, authentication.authorities)
    return roles.map { AuthUserRole(it) }
  }

  @PostMapping("/api/authuser/create")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Create user.",
    description = "Create user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
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
        responseCode = "409",
        description = "User or email already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Server exception e.g. failed to call notify.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @Throws(NotificationClientException::class)
  fun createUserByEmail(
    @Parameter(description = "Details of the user to be created.", required = true) @RequestBody
    createUser: CreateUser,
    @Parameter(hidden = true) request: HttpServletRequest,
    @Parameter(hidden = true) authentication: Authentication
  ): ResponseEntity<Any> {
    val email = EmailHelper.format(createUser.email)

    val user = email?.let { authUserService.getAuthUserByUsername(email).orElse(null) }

    if (user != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorDetailUserId("username.exists", "User $email already exists", "userId", user.userId))
    }

    val userByEmail = authUserService.findAuthUsersByEmail(email)
    if (userByEmail.isNotEmpty()) {
      val userId = if (userByEmail.size == 1) userByEmail[0].userId else null
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorDetailUserId("email.exists", "User $email already exists", "email", userId))
    }

    val mergedGroups = mutableSetOf<String>()
    if (createUser.groupCodes != null) {
      mergedGroups.addAll(createUser.groupCodes)
    }
    if (!createUser.groupCode.isNullOrBlank()) {
      mergedGroups.add(createUser.groupCode)
    }

    return try {
      val setPasswordUrl = createInitialPasswordUrl(request)
      val userId = authUserService.createUserByEmail(
        email,
        createUser.firstName,
        createUser.lastName,
        mergedGroups,
        setPasswordUrl,
        authentication.name,
        authentication.authorities
      )
      log.info("Create user succeeded for user {}", createUser.email)
      ResponseEntity.ok(userId)
    } catch (e: CreateUserException) {
      log.info("Create user failed for user ${createUser.email} for field ${e.field} with reason ${e.errorCode}".removeAllCrLf())
      ResponseEntity.badRequest().body(
        ErrorDetail(
          "${e.field}.${e.errorCode}",
          "${e.field} failed validation",
          e.field
        )
      )
    } catch (e: ValidEmailException) {
      log.info("Create user failed for user $email for field email with reason ${e.reason}".removeAllCrLf())
      ResponseEntity.badRequest()
        .body(ErrorDetail("email.${e.reason}", "Email address failed validation", "email"))
    }
  }

  private fun createInitialPasswordUrl(@Parameter(hidden = true) request: HttpServletRequest): String {
    val requestURL = request.requestURL
    return requestURL.toString().replaceFirst("/api/authuser/.*".toRegex(), "/initial-password?token=")
  }

  @Deprecated(
    message = "Enable User by userId  now use the mange-users-api service",
    replaceWith = ReplaceWith("/{manage-users-api}/users/{userId}/enable"),
    level = DeprecationLevel.WARNING
  )
  @PutMapping("/api/authuser/id/{userId}/enable")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Enable a user.",
    description = "Enable a user."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "OK."
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
        responseCode = "403",
        description = "Unable to enable user, the user is not within one of your groups.",
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
  fun enableUserByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(hidden = true) request: HttpServletRequest
  ) = authUserService.enableUserByUserId(
    userId,
    authentication.name,
    request.requestURL.toString(),
    authentication.authorities
  )

  @PutMapping("/api/authuser/id/{userId}/disable")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Disable a user.",
    description = "Disable a user."
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "OK."
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
        responseCode = "403",
        description = "Unable to disable user, the user is not within one of your groups.",
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
  fun disableUserByUserId(
    @Parameter(description = "The userId of the user.", required = true) @PathVariable
    userId: String,
    @Parameter(
      description = "The reason user made inactive.",
      required = true
    ) @RequestBody
    deactivateReason: DeactivateReason,
    @Parameter(hidden = true) authentication: Authentication
  ) = authUserService.disableUserByUserId(
    userId,
    authentication.name,
    deactivateReason.reason,
    authentication.authorities
  )

  @PostMapping("/api/authuser/id/{userId}/email")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Amend a user email address.",
    description = "Amend a user email address."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "OK."
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request e.g. if validation failed or if the email changes are disallowed",
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
        responseCode = "403",
        description = "Unable to amend user, the user is not within one of your groups or you don't have ROLE_MAINTAIN_OAUTH_USERS or ROLE_AUTH_GROUP_MANAGER roles",
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
  @Throws(NotificationClientException::class)
  fun alterUserEmail(
    @Parameter(description = "The ID of the user.", required = true) @PathVariable
    userId: String,
    @RequestBody amendUser: AmendUser,
    @Parameter(hidden = true) request: HttpServletRequest,
    @Parameter(hidden = true) authentication: Authentication
  ): String? {
    val setPasswordUrl = createInitialPasswordUrl(request)
    val resetLink = authUserService.amendUserEmailByUserId(
      userId,
      amendUser.email,
      setPasswordUrl,
      authentication.name,
      authentication.authorities,
      EmailType.PRIMARY
    )
    return if (smokeTestEnabled) resetLink else null
  }

  @PostMapping("/api/authuser/email")
  @Operation(
    summary = "Email address for users",
    description =
    """Verified email address for users.  Post version that accepts multiple email addresses.
        Requires ROLE_MAINTAIN_ACCESS_ROLES or ROLE_MAINTAIN_ACCESS_ROLES_ADMIN.
    """
  )
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  fun getAuthUserEmails(
    @Parameter(description = "List of usernames.", required = true) @RequestBody
    usernames: List<String>
  ): List<EmailAddress> = authUserService
    .findAuthUsersByUsernames(usernames)
    .filter { it.verified }
    .map { EmailAddress(it) }

  data class CreateUser(
    @Schema(
      required = true,
      description = "Email address",
      example = "nomis.user@someagency.justice.gov.uk"
    )
    val email: String?,

    @Schema(required = true, description = "First name", example = "Nomis")
    val firstName: String?,

    @Schema(required = true, description = "Last name", example = "User")
    val lastName: String?,

    @Schema(description = "Initial group, required for group managers", example = "SITE_1_GROUP_1")
    val groupCode: String?,

    @Schema(
      description = "Initial groups, can be used if multiple initial groups required",
      example = "[\"SITE_1_GROUP_1\", \"SITE_1_GROUP_2\"]"
    )
    val groupCodes: Set<String>?
  )

  data class ErrorDetailUserId(
    @Schema(required = true, description = "Error", example = "Not Found")
    val error: String,

    @Schema(required = true, description = "Error description", example = "User not found.")
    val error_description: String,

    @Schema(description = "Field in error", example = "userId")
    val field: String? = null,

    @Schema(description = "userId", example = "userId")
    val userId: String? = null
  )

  data class AmendUser(
    @Schema(required = true, description = "Email address", example = "nomis.user@someagency.justice.gov.uk")
    val email: String?
  )

  data class AuthUser(
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
    val locked: Boolean = false,

    @Schema(required = true, description = "Account is enabled", example = "false")
    val enabled: Boolean = false,

    @Schema(required = true, description = "Email address has been verified", example = "false")
    val verified: Boolean = false,

    @Schema(required = true, description = "Last time user logged in", example = "01/01/2001")
    val lastLoggedIn: LocalDateTime? = null,

    @Schema(required = true, description = "Inactive reason", example = "Left department")
    val inactiveReason: String? = null
  ) {
    companion object {
      fun fromUser(user: User): AuthUser {
        return AuthUser(
          userId = user.id.toString(),
          username = user.username,
          email = user.email,
          firstName = user.firstName,
          lastName = user.person?.lastName,
          locked = user.locked,
          enabled = user.isEnabled,
          verified = user.verified,
          lastLoggedIn = user.lastLoggedIn,
          inactiveReason = user.inactiveReason
        )
      }
    }
  }

  private fun notFoundBody(username: String): Any =
    ErrorDetail("Not Found", "Account for username $username not found", "username")
}

@Schema(description = "Deactivate Reason")
data class DeactivateReason(
  @Schema(required = true, description = "Deactivate Reason", example = "User has left")
  @field:Size(max = 100, min = 4, message = "Reason must be between 4 and 100 characters") @NotBlank
  val reason: String
)
