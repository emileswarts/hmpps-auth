package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import springfox.documentation.annotations.ApiIgnore
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
@Api(tags = ["/api/authuser"])
class AuthUserController(
  private val userService: UserService,
  private val authUserService: AuthUserService,
  private val authUserGroupService: AuthUserGroupService,
  private val authUserRoleService: AuthUserRoleService,
  @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/api/authuser/{username}")
  @ApiOperation(
    value = "User detail.",
    notes = "User detail.",
    nickname = "getUserDetails",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = AuthUser::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  fun user(
    @ApiParam(value = "The username of the user.", required = true) @PathVariable username: String,
  ): ResponseEntity<Any?> {
    val user = authUserService.getAuthUserByUsername(username)
    return user.map { AuthUser.fromUser(it) }
      .map { Any::class.java.cast(it) }
      .map { ResponseEntity.ok(it) }
      .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundBody(username)))
  }

  @GetMapping("/api/authuser/id/{userId}")
  @ApiOperation(
    value = "User detail.",
    notes = "User detail.",
    nickname = "getUserDetails",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = AuthUser::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  fun getUserById(
    @ApiParam(value = "The ID of the user.", required = true) @PathVariable userId: String,
    @ApiIgnore authentication: Authentication,
  ): AuthUser {

    return authUserService.getAuthUserByUserId(userId, authentication.name, authentication.authorities)
      ?.let { AuthUser.fromUser(it) }
      ?: throw UsernameNotFoundException("User $userId not found")
  }

  @GetMapping("/api/authuser")
  @ApiOperation(
    value = "Search for a user.",
    notes = "Search for a user.",
    nickname = "searchForUser",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = AuthUser::class, responseContainer = "List"),
      ApiResponse(code = 204, message = "No users found."),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)
    ]
  )
  fun searchForUser(
    @ApiParam(value = "The email address of the user.", required = true) @RequestParam email: String?,
  ): ResponseEntity<Any> {
    val users = authUserService.findAuthUsersByEmail(email).map { AuthUser.fromUser(it) }
    return if (users.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok(users)
  }

  @GetMapping("/api/authuser/search")
  @ApiOperation(
    value = "Search for a user.",
    nickname = "searchForUser",
    produces = "application/json"
  )
  @ApiResponses(value = [ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)])
  @ApiImplicitParams(
    ApiImplicitParam(
      name = "page",
      dataType = "java.lang.Integer",
      paramType = "query",
      value = "Results page you want to retrieve (0..N)",
      example = "0",
      defaultValue = "0"
    ),
    ApiImplicitParam(
      name = "size",
      dataType = "java.lang.Integer",
      paramType = "query",
      value = "Number of records per page.",
      example = "10",
      defaultValue = "10"
    ),
    ApiImplicitParam(
      name = "sort",
      dataType = "java.lang.String",
      paramType = "query",
      value = "Sort column and direction, eg sort=lastName,desc"
    )
  )
  @PreAuthorize(
    "hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')"
  )
  fun searchForUser(
    @ApiParam(
      value = "The username, email or name of the user.",
      example = "j smith"
    ) @RequestParam(required = false) name: String?,
    @ApiParam(value = "The role codes of the user.") @RequestParam(required = false) roles: List<String>?,
    @ApiParam(value = "The group codes of the user.") @RequestParam(required = false) groups: List<String>?,
    @ApiParam(value = "Limit to active / inactive / show all users.") @RequestParam(
      required = false,
      defaultValue = "ALL"
    ) status: Status,
    @PageableDefault(sort = ["Person.lastName", "Person.firstName"], direction = Sort.Direction.ASC) pageable: Pageable,
    @ApiIgnore authentication: Authentication,
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
  @ApiOperation(
    value = "Get list of assignable groups.",
    notes = "Get list of groups that can be assigned by the current user.",
    nickname = "assignableGroups",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)
    ]
  )
  fun assignableGroups(@ApiIgnore authentication: Authentication): List<AuthUserGroup> {
    val groups = authUserGroupService.getAssignableGroups(authentication.name, authentication.authorities)
    return groups.map { AuthUserGroup(it) }
  }

  @GetMapping("/api/authuser/me/searchable-roles")
  @ApiOperation(
    value = "Get list of searchable roles.",
    notes = "Get list of roles that can be search for by the current user.",
    nickname = "searchableRoles",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class)
    ]
  )
  fun searchableRoles(@ApiIgnore authentication: Authentication): List<AuthUserRole> {
    val roles = authUserRoleService.getAllAssignableRoles(authentication.name, authentication.authorities)
    return roles.map { AuthUserRole(it) }
  }

  @PostMapping("/api/authuser/create")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Create user.",
    notes = "Create user.",
    nickname = "createUser",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Validation failed.", response = ErrorDetail::class),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 409, message = "User or email already exists.", response = ErrorDetail::class),
      ApiResponse(code = 500, message = "Server exception e.g. failed to call notify.", response = ErrorDetail::class)
    ]
  )
  @Throws(NotificationClientException::class)
  fun createUserByEmail(
    @ApiParam(value = "Details of the user to be created.", required = true) @RequestBody createUser: CreateUser,
    @ApiIgnore request: HttpServletRequest,
    @ApiIgnore authentication: Authentication,
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

  private fun createInitialPasswordUrl(@ApiIgnore request: HttpServletRequest): String {
    val requestURL = request.requestURL
    return requestURL.toString().replaceFirst("/api/authuser/.*".toRegex(), "/initial-password?token=")
  }

  @PutMapping("/api/authuser/id/{userId}/enable")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(
    value = "Enable a user.",
    notes = "Enable a user.",
    nickname = "enableUser",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 204, message = "OK"),
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(
        code = 403,
        message = "Unable to enable user, the user is not within one of your groups",
        response = ErrorDetail::class
      ),
      ApiResponse(code = 404, message = "User not found.", response = ErrorDetail::class)
    ]
  )
  fun enableUserByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiIgnore authentication: Authentication,
    @ApiIgnore request: HttpServletRequest,
  ) = authUserService.enableUserByUserId(
    userId,
    authentication.name,
    request.requestURL.toString(),
    authentication.authorities
  )

  @PutMapping("/api/authuser/id/{userId}/disable")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Disable a user.",
    notes = "Disable a user.",
    nickname = "disableUser",
    consumes = "application/json",
    produces = "application/json"
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(
    value = [
      ApiResponse(code = 204, message = "OK"), ApiResponse(
        code = 401,
        message = "Unauthorized.",
        response = ErrorDetail::class
      ), ApiResponse(
        code = 403,
        message = "Unable to disable user, the user is not within one of your groups",
        response = ErrorDetail::class
      ), ApiResponse(
        code = 404,
        message = "User not found.",
        response = ErrorDetail::class
      )
    ]
  )
  fun disableUserByUserId(
    @ApiParam(value = "The userId of the user.", required = true) @PathVariable userId: String,
    @ApiParam(
      value = "The reason user made inactive.",
      required = true
    ) @RequestBody deactivateReason: DeactivateReason,
    @ApiIgnore authentication: Authentication,
  ) = authUserService.disableUserByUserId(
    userId,
    authentication.name,
    deactivateReason.reason,
    authentication.authorities
  )

  @PostMapping("/api/authuser/id/{userId}/email")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @ApiOperation(
    value = "Amend a user email address.",
    notes = "Amend a user email address.",
    nickname = "alterUserEmail",
    consumes = "application/json",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 204, message = "OK"), ApiResponse(
        code = 400,
        message = "Bad request e.g. if validation failed or if the email changes are disallowed",
        response = ErrorDetail::class
      ), ApiResponse(
        code = 401,
        message = "Unauthorized.",
        response = ErrorDetail::class
      ), ApiResponse(
        code = 403,
        message = "Unable to amend user, the user is not within one of your groups or you don't have ROLE_MAINTAIN_OAUTH_USERS or ROLE_AUTH_GROUP_MANAGER roles",
        response = ErrorDetail::class
      ), ApiResponse(
        code = 404,
        message = "User not found.",
        response = ErrorDetail::class
      )
    ]
  )
  @Throws(NotificationClientException::class)
  fun alterUserEmail(
    @ApiParam(value = "The ID of the user.", required = true) @PathVariable userId: String,
    @RequestBody amendUser: AmendUser,
    @ApiIgnore request: HttpServletRequest,
    @ApiIgnore authentication: Authentication,
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
  @ApiOperation(
    value = "Email address for users",
    notes = """Verified email address for users.  Post version that accepts multiple email addresses.
        Requires ROLE_MAINTAIN_ACCESS_ROLES or ROLE_MAINTAIN_ACCESS_ROLES_ADMIN.""",
    nickname = "getAuthUserEmails",
    consumes = "application/json",
    produces = "application/json"
  )
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  fun getAuthUserEmails(
    @ApiParam(value = "List of usernames.", required = true) @RequestBody usernames: List<String>,
  ): List<EmailAddress> = authUserService
    .findAuthUsersByUsernames(usernames)
    .filter { it.verified }
    .map { EmailAddress(it) }

  data class CreateUser(
    @ApiModelProperty(
      required = true,
      value = "Email address",
      example = "nomis.user@someagency.justice.gov.uk",
      position = 1
    )
    val email: String?,

    @ApiModelProperty(required = true, value = "First name", example = "Nomis", position = 2)
    val firstName: String?,

    @ApiModelProperty(required = true, value = "Last name", example = "User", position = 3)
    val lastName: String?,

    @ApiModelProperty(value = "Initial group, required for group managers", example = "SITE_1_GROUP_1", position = 4)
    val groupCode: String?,

    @ApiModelProperty(
      value = "Initial groups, can be used if multiple initial groups required",
      example = "[\"SITE_1_GROUP_1\", \"SITE_1_GROUP_2\"]",
      position = 5
    )
    val groupCodes: Set<String>?,
  )

  data class ErrorDetailUserId(
    @ApiModelProperty(required = true, value = "Error", example = "Not Found", position = 1)
    val error: String,

    @ApiModelProperty(required = true, value = "Error description", example = "User not found.", position = 2)
    val error_description: String,

    @ApiModelProperty(required = false, value = "Field in error", example = "userId", position = 3)
    val field: String? = null,

    @ApiModelProperty(required = false, value = "userId", example = "userId", position = 4)
    val userId: String? = null
  )

  data class AmendUser(
    @ApiModelProperty(required = true, value = "Email address", example = "nomis.user@someagency.justice.gov.uk")
    val email: String?,
  )

  data class AuthUser(
    @ApiModelProperty(
      required = true,
      value = "User ID",
      example = "91229A16-B5F4-4784-942E-A484A97AC865",
      position = 1
    )
    val userId: String? = null,

    @ApiModelProperty(required = true, value = "Username", example = "authuser", position = 2)
    val username: String? = null,

    @ApiModelProperty(
      required = true,
      value = "Email address",
      example = "auth.user@someagency.justice.gov.uk",
      position = 3
    )
    val email: String? = null,

    @ApiModelProperty(required = true, value = "First name", example = "Auth", position = 4)
    val firstName: String? = null,

    @ApiModelProperty(required = true, value = "Last name", example = "User", position = 5)
    val lastName: String? = null,

    @ApiModelProperty(
      required = true,
      value = "Account is locked due to incorrect password attempts",
      example = "true",
      position = 6
    )
    val locked: Boolean = false,

    @ApiModelProperty(required = true, value = "Account is enabled", example = "false", position = 7)
    val enabled: Boolean = false,

    @ApiModelProperty(required = true, value = "Email address has been verified", example = "false", position = 8)
    val verified: Boolean = false,

    @ApiModelProperty(required = true, value = "Last time user logged in", example = "01/01/2001", position = 9)
    val lastLoggedIn: LocalDateTime? = null,

    @ApiModelProperty(required = true, value = "Inactive reason", example = "Left department", position = 10)
    val inactiveReason: String? = null,
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

@ApiModel(description = "Deactivate Reason")
data class DeactivateReason(
  @ApiModelProperty(required = true, value = "Deactivate Reason", example = "User has left")
  @field:Size(max = 100, min = 4, message = "Reason must be between 4 and 100 characters") @NotBlank val reason: String,
)
