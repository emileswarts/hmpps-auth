package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.commons.text.WordUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.NomisUserService
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@RestController
@Validated
@Tag(name = "/api/prisonuser", description = "Prison User Controller")
@RequestMapping("/api/prisonuser")
class PrisonUserController(
  private val userService: UserService,
  private val nomisUserService: NomisUserService,
  private val verifyEmailService: VerifyEmailService,
  @Value("\${application.smoketest.enabled}") private val smokeTestEnabled: Boolean,
) {
  @GetMapping
  @PreAuthorize("hasAnyRole('ROLE_USE_OF_FORCE', 'ROLE_STAFF_SEARCH')")
  @Operation(
    summary = "Find prison users by first and last names.",
    description = "Find prison users by first and last names."
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
      )
    ]
  )
  fun prisonUsersByFirstAndLastName(
    @Parameter(
      description = "The first name to match. Case insensitive.",
      required = true
    ) @RequestParam @NotEmpty firstName: String,
    @Parameter(
      description = "The last name to match. Case insensitive",
      required = true
    ) @RequestParam @NotEmpty lastName: String,
  ): List<PrisonUser> = userService.findPrisonUsersByFirstAndLastNames(firstName, lastName)
    .map {
      PrisonUser(
        username = it.username,
        staffId = it.userId.toLongOrNull(),
        email = it.email,
        verified = it.verified,
        firstName = WordUtils.capitalizeFully(it.firstName),
        lastName = WordUtils.capitalizeFully(it.lastName),
        name = WordUtils.capitalizeFully("${it.firstName} ${it.lastName}"),
        activeCaseLoadId = it.activeCaseLoadId
      )
    }

  @PostMapping("/{username}/email")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @Operation(
    summary = "Amend a prison user email address.",
    description = "Amend a prison user email address.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "400", description = "Bad request e.g. missing email address.",
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
      )
    ]
  )
  fun amendUserEmail(
    @Parameter(description = "The username of the user.", required = true) @PathVariable username: String,
    @Valid @RequestBody amendUser: AmendEmail,
    @Parameter(hidden = true) request: HttpServletRequest,
    @Parameter(hidden = true) authentication: Authentication,
  ): String? {
    val setPasswordUrl =
      request.requestURL.toString().replaceFirst("/api/prisonuser/.*".toRegex(), "/verify-email-confirm?token=")

    val userEmailAndUsername = nomisUserService.changeEmailAndRequestVerification(
      username = username,
      emailInput = amendUser.email,
      url = setPasswordUrl,
      emailType = User.EmailType.PRIMARY
    )

    log.info("Amend user succeeded for user {}", userEmailAndUsername.username)
    return if (smokeTestEnabled) userEmailAndUsername.link else ""
  }

  @PostMapping("/{username}/email/sync")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @Operation(
    summary = "Sync user email",
    description = "Run process to check for differences in email address between Auth and NOMIS and update Auth if required",
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
  fun syncUserEmail(
    @Parameter(description = "The username of the user.", required = true) @PathVariable username: String,
    @Parameter(hidden = true) authentication: Authentication,
  ) = verifyEmailService.syncEmailWithNOMIS(username)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

/**
 * copy of PrisonUserDto with added Swagger annotations.
 * Done to keep presentation layer detail out of the service layer.
 */
data class PrisonUser(
  @Schema(required = true, example = "RO_USER_TEST")
  val username: String,
  @Schema(required = true, example = "1234564789")
  val staffId: Long?,
  @Schema(required = false, example = "ryanorton@justice.gov.uk")
  val email: String?,
  @Schema(required = true, example = "true")
  val verified: Boolean,
  @Schema(required = true, example = "Ryan")
  val firstName: String,
  @Schema(required = true, example = "Orton")
  val lastName: String,
  @Schema(required = true, example = "Ryan Orton")
  val name: String,
  @Schema(required = false, example = "MDI")
  val activeCaseLoadId: String?
)

data class AmendEmail(
  @Schema(required = true, description = "Email address", example = "nomis.user@someagency.justice.gov.uk")
  @field:NotBlank(message = "Email must not be blank")
  val email: String?,
)
