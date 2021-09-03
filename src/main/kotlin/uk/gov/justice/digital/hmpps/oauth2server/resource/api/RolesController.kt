package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail

@Validated
@RestController
class RolesController(
  private val rolesService: RolesService
) {

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
  ): Page<RoleBasics> =
    rolesService.getAllRoles(
      pageable
    )
      .map { RoleBasics.fromAuthority(it) }
}

@ApiModel(description = "Basic Role")
data class RoleBasics(
  @ApiModelProperty(required = true, value = "Role Code", example = "HDC_NPS_NE")
  val roleCode: String,

  @ApiModelProperty(required = true, value = "Role Name", example = "HDC NPS North East")
  val roleName: String
) {
  companion object {
    fun fromAuthority(authority: Authority): RoleBasics {
      return RoleBasics(
        roleCode = authority.roleCode,
        roleName = authority.roleName,
      )
    }
  }
}
