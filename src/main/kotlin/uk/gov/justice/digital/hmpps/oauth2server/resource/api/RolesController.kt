package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Validated
@RestController
@Tag(name = "/api/roles", description = "Roles Controller")
class RolesController(
  private val rolesService: RolesService
) {
  @PostMapping("/api/roles")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "Create role.",
    description = "Create a Role"
  )
  @ApiResponses(
    value = [
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
        description = "Role already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Throws(RoleExistsException::class, RoleNotFoundException::class)
  fun createRole(
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(description = "Details of the role to be created.", required = true)
    @Valid @RequestBody
    createRole: CreateRole
  ) {
    rolesService.createRole(authentication.name, createRole)
  }

  @GetMapping("/api/roles")
  @Deprecated(
    message = "Role endpoints now use the mange-users-api service",
    replaceWith = ReplaceWith("/{manage-users-api}/roles"),
    level = DeprecationLevel.WARNING
  )
  @PreAuthorize("hasAnyRole('ROLE_ROLES_ADMIN', 'ROLE_MAINTAIN_ACCESS_ROLES_ADMIN','ROLE_MAINTAIN_ACCESS_ROLES')")
  @Operation(
    summary = "Get all Roles",
    description = "Get all Roles"
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
  fun getRoles(
    @Parameter(description = "Role admin type to find EXT_ADM, DPS_ADM, DPS_LSA.")
    @RequestParam(
      required = false
    )
    adminTypes: List<AdminType>?
  ): List<RoleDetails> = rolesService.getRoles(adminTypes)
    .map {
      RoleDetails(it)
    }

  @GetMapping("/api/roles/paged")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "get all paged Roles.",
    description = "getAllPagedRoles"
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
        description = "Roles not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun getAllRoles(
    @Parameter(description = "Role name or partial of a role name") @RequestParam(
      required = false
    )
    roleName: String?,
    @Parameter(description = "Role code or partial of a role code") @RequestParam(
      required = false
    )
    roleCode: String?,
    @Parameter(description = "Role admin type to find EXT_ADM, DPS_ADM, DPS_LSA.") @RequestParam(
      required = false
    )
    adminTypes: List<AdminType>?,
    @PageableDefault(sort = ["roleName"], direction = Sort.Direction.ASC) pageable: Pageable
  ): Page<RoleDetails> =
    rolesService.getAllRoles(
      roleName,
      roleCode,
      adminTypes,
      pageable
    )
      .map { RoleDetails(it) }

  @GetMapping("/api/roles/{role}")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "Role detail.",
    description = "Get Role Details"
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
        description = "Role not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun getRoleDetail(
    @Parameter(description = "The Role code of the role.", required = true)
    @PathVariable
    role: String
  ): RoleDetails {
    val returnedRole: Authority = rolesService.getRoleDetail(role)
    return RoleDetails(returnedRole)
  }

  @PutMapping("/api/roles/{role}")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "Amend role name.",
    description = "AmendRoleName"
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
        description = "Role not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun amendRoleName(
    @Parameter(description = "The role code of the role.", required = true)
    @PathVariable
    role: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(
      description = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody
    roleAmendment: RoleNameAmendment
  ) {
    rolesService.updateRoleName(authentication.name, role, roleAmendment)
  }

  @PutMapping("/api/roles/{role}/description")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "Amend role description.",
    description = "Amend role description."
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
        description = "Role not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun amendRoleDescription(
    @Parameter(description = "The role code of the role.", required = true)
    @PathVariable
    role: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(
      description = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody
    roleAmendment: RoleDescriptionAmendment
  ) {
    rolesService.updateRoleDescription(authentication.name, role, roleAmendment)
  }

  @PutMapping("/api/roles/{role}/admintype")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @Operation(
    summary = "Amend role admin type.",
    description = "Amend role admin type."
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
        description = "Role not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun amendRoleAdminType(
    @Parameter(description = "The role code of the role.", required = true)
    @PathVariable
    role: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(
      description = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody
    roleAmendment: RoleAdminTypeAmendment
  ) {
    rolesService.updateRoleAdminType(authentication.name, role, roleAmendment)
  }
}

data class CreateRole(
  @Schema(required = true, description = "Role Code", example = "AUTH_GROUP_MANAGER")
  @field:NotBlank(message = "role code must be supplied")
  @field:Size(min = 2, max = 30)
  @field:Pattern(regexp = "^[0-9A-Za-z_]*")
  val roleCode: String,

  @Schema(required = true, description = "roleName", example = "Auth Group Manager")
  @field:NotBlank(message = "role name must be supplied")
  @field:Size(min = 4, max = 128)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val roleName: String,

  @Schema(
    required = false,
    description = "roleDescription",
    example = "Allow Group Manager to administer the account within their groups"
  )
  @field:Size(max = 1024)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&\r\n]*\$")
  val roleDescription: String? = null,

  @Schema(
    required = true,
    description = "adminType, can be used if multiple admin types required",
    example = "[\"EXT_ADM\", \"DPS_ADM\"]"
  )
  @field:NotEmpty(message = "Admin type cannot be empty")
  val adminType: Set<AdminType>
)

@Schema(description = "Role Details")
data class RoleDetails(
  @Schema(required = true, description = "Role Code", example = "AUTH_GROUP_MANAGER")
  val roleCode: String,

  @Schema(required = true, description = "Role Name", example = "Auth Group Manager")
  val roleName: String,

  @Schema(
    required = true,
    description = "Role Description",
    example = "Allow Group Manager to administer the account within their groups"
  )
  val roleDescription: String?,

  @Schema(
    required = true,
    description = "Administration Type",
    example = "{\"adminTypeCode\": \"EXT_ADM\",\"adminTypeName\": \"External Administrator\"}"
  )
  val adminType: List<AdminType>
) {
  constructor(r: Authority) : this(
    r.roleCode,
    r.roleName,
    r.roleDescription,
    r.adminType
  )
}

@Schema(description = "Role Name")
data class RoleNameAmendment(
  @Schema(required = true, description = "Role Name", example = "Central admin")
  @field:NotBlank(message = "Role name must be supplied")
  @field:Size(min = 4, max = 100)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val roleName: String
)

@Schema(description = "Role Description")
data class RoleDescriptionAmendment(
  @Schema(required = true, description = "Role Description", example = "Maintaining admin users")
  @field:Size(max = 1024)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&\r\n]*\$")
  val roleDescription: String?
)

@Schema(description = "Role Administration Types")
data class RoleAdminTypeAmendment(
  @Schema(required = true, description = "Role Administration Types", example = "[\"DPS_ADM\"]")
  @field:NotEmpty(message = "Admin type cannot be empty")
  val adminType: Set<AdminType>
)
