package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
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
class RolesController(
  private val rolesService: RolesService
) {
  @PostMapping("/api/roles")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiOperation(
    value = "Create role.",
    nickname = "CreateRole",
    consumes = "application/json",
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 409, message = "Role already exists.", response = ErrorDetail::class)
    ]
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Throws(RoleExistsException::class, RoleNotFoundException::class)
  fun createRole(
    @ApiIgnore authentication: Authentication,
    @ApiParam(value = "Details of the role to be created.", required = true)
    @Valid @RequestBody createRole: CreateRole,
  ) {
    rolesService.createRole(authentication.name, createRole)
  }

  @GetMapping("/api/roles")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiImplicitParams(
    ApiImplicitParam(
      name = "page",
      dataType = "int",
      paramType = "query",
      value = "Results page you want to retrieve (0..N)",
      example = "0",
      defaultValue = "0"
    ),
    ApiImplicitParam(
      name = "size",
      dataType = "int",
      paramType = "query",
      value = "Number of records per page.",
      example = "10",
      defaultValue = "10"
    ),
    ApiImplicitParam(
      name = "sort",
      dataType = "string",
      paramType = "query",
      value = "Sort column and direction, eg sort=roleName,desc"
    )
  )
  @ApiOperation(
    value = "get all Roles.",
    nickname = "getAllRoles",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "Roles not found.", response = ErrorDetail::class)
    ]
  )
  fun getAllRoles(
    @PageableDefault(sort = ["roleName"], direction = Sort.Direction.ASC) pageable: Pageable,
  ): Page<RoleDetails> =
    rolesService.getAllRoles(
      pageable
    )
      .map { RoleDetails(it) }

  @GetMapping("/api/roles/{role}")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiOperation(
    value = "Role detail.",
    nickname = "getRoleDetails",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "Role not found.", response = ErrorDetail::class)
    ]
  )
  fun getRoleDetail(
    @ApiParam(value = "The Role code of the role.", required = true)
    @PathVariable role: String,
  ): RoleDetails {
    val returnedRole: Authority = rolesService.getRoleDetail(role)
    return RoleDetails(returnedRole)
  }

  @PutMapping("/api/roles/{role}")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiOperation(
    value = "Amend role name.",
    nickname = "AmendRoleName",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "Role not found.", response = ErrorDetail::class)
    ]
  )
  fun amendRoleName(
    @ApiParam(value = "The role code of the role.", required = true)
    @PathVariable role: String,
    @ApiIgnore authentication: Authentication,
    @ApiParam(
      value = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody roleAmendment: RoleNameAmendment
  ) {
    rolesService.updateRoleName(authentication.name, role, roleAmendment)
  }

  @PutMapping("/api/roles/{role}/description")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiOperation(
    value = "Amend role description.",
    nickname = "AmendRoleDescription",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "Role not found.", response = ErrorDetail::class)
    ]
  )
  fun amendRoleDescription(
    @ApiParam(value = "The role code of the role.", required = true)
    @PathVariable role: String,
    @ApiIgnore authentication: Authentication,
    @ApiParam(
      value = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody roleAmendment: RoleDescriptionAmendment
  ) {
    rolesService.updateRoleDescription(authentication.name, role, roleAmendment)
  }

  @PutMapping("/api/roles/{role}/admintype")
  @PreAuthorize("hasRole('ROLE_ROLES_ADMIN')")
  @ApiOperation(
    value = "Amend role admin type.",
    nickname = "AmendRoleAdminType",
    produces = "application/json"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 401, message = "Unauthorized.", response = ErrorDetail::class),
      ApiResponse(code = 404, message = "Role not found.", response = ErrorDetail::class)
    ]
  )
  fun amendRoleAdminType(
    @ApiParam(value = "The role code of the role.", required = true)
    @PathVariable role: String,
    @ApiIgnore authentication: Authentication,
    @ApiParam(
      value = "Details of the role to be updated.",
      required = true
    ) @Valid @RequestBody roleAmendment: RoleAdminTypeAmendment
  ) {
    rolesService.updateRoleAdminType(authentication.name, role, roleAmendment)
  }
}

data class CreateRole(
  @ApiModelProperty(required = true, value = "Role Code", example = "AUTH_GROUP_MANAGER", position = 1)
  @field:NotBlank(message = "role code must be supplied")
  @field:Size(min = 2, max = 30)
  @field:Pattern(regexp = "^[0-9A-Za-z_]*")
  val roleCode: String,

  @ApiModelProperty(required = true, value = "roleName", example = "Auth Group Manager", position = 2)
  @field:NotBlank(message = "role name must be supplied")
  @field:Size(min = 4, max = 128)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val roleName: String,

  @ApiModelProperty(
    required = false,
    value = "roleDescription",
    example = "Allow Group Manager to administer the account within their groups",
    position = 3
  )
  @field:Size(max = 1024)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&\r\n]*\$")
  val roleDescription: String? = null,

  @ApiModelProperty(
    required = true,
    value = "adminType, can be used if multiple admin types required",
    example = "[\"EXT_ADM\", \"DPS_ADM\"]",
    position = 4
  )
  @field:NotEmpty(message = "Admin type cannot be empty")
  val adminType: Set<AdminType>,
)

@ApiModel(description = "Role Details")
data class RoleDetails(
  @ApiModelProperty(required = true, value = "Role Code", example = "AUTH_GROUP_MANAGER")
  val roleCode: String,

  @ApiModelProperty(required = true, value = "Role Name", example = "Auth Group Manager")
  val roleName: String,

  @ApiModelProperty(required = true, value = "Role Description", example = "Allow Group Manager to administer the account within their groups")
  val roleDescription: String?,

  @ApiModelProperty(required = true, value = "Administration Type")
  val adminType: List<AdminType>,
) {
  constructor(r: Authority) : this(
    r.roleCode,
    r.roleName,
    r.roleDescription,
    r.adminType
  )
}

@ApiModel(description = "Role Name")
data class RoleNameAmendment(
  @ApiModelProperty(required = true, value = "Role Name", example = "Central admin")
  @field:NotBlank(message = "Role name must be supplied")
  @field:Size(min = 4, max = 100)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val roleName: String
)

@ApiModel(description = "Role Description")
data class RoleDescriptionAmendment(
  @ApiModelProperty(required = true, value = "Role Description", example = "Maintaining admin users")
  @field:Size(max = 1024)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&\r\n]*\$")
  val roleDescription: String?
)

@ApiModel(description = "Role Administration Types")
data class RoleAdminTypeAmendment(
  @ApiModelProperty(required = true, value = "Role Administration Types", example = "[\"DPS_ADM\"]")
  @field:NotEmpty(message = "Admin type cannot be empty")
  val adminType: Set<AdminType>
)
