package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ChildGroup
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.ChildGroupExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupHasChildGroupException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserAssignableRole
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Validated
@RestController
@Tag(name = "/api/groups", description = "Groups Controller")
class GroupsController(
  private val groupsService: GroupsService
) {

  @GetMapping("/api/groups/{group}")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(
    summary = "Group detail.",
    description = "return Group Details"
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
        description = "Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun getGroupDetail(
    @Parameter(description = "The group code of the group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication
  ): GroupDetails {
    val returnedGroup: Group = groupsService.getGroupDetail(group, authentication.name, authentication.authorities)
    return GroupDetails(returnedGroup)
  }

  @GetMapping("/api/groups/child/{group}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Child Group detail.",
    description = "get Child Group Details"
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
        description = "Child Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun getChildGroupDetail(
    @Parameter(description = "The group code of the child group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication
  ): ChildGroupDetails {
    val returnedGroup: ChildGroup =
      groupsService.getChildGroupDetail(group, authentication.name, authentication.authorities)
    return ChildGroupDetails(returnedGroup)
  }

  @PutMapping("/api/groups/{group}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Amend group name.",
    description = "AmendGroupName"
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
        description = "Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun amendGroupName(
    @Parameter(description = "The group code of the group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(
      description = "Details of the group to be updated.",
      required = true
    ) @RequestBody
    groupAmendment: GroupAmendment

  ) {
    groupsService.updateGroup(authentication.name, group, groupAmendment)
  }

  @DeleteMapping("/api/groups/{group}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Delete group.",
    description = "Delete a Group"
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
        description = "Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @Throws(GroupNotFoundException::class, GroupHasChildGroupException::class)
  fun deleteGroup(
    @Parameter(description = "The group code of the group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication
  ) {
    groupsService.deleteGroup(authentication.name, group, authentication.authorities)
  }

  @PutMapping("/api/groups/child/{group}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Amend child group name.",
    description = "Amend a Child Group Name"
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
        description = "Child Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun amendChildGroupName(
    @Parameter(description = "The group code of the child group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(
      description = "Details of the child group to be updated.",
      required = true
    ) @RequestBody
    groupAmendment: GroupAmendment

  ) {
    groupsService.updateChildGroup(authentication.name, group, groupAmendment)
  }

  @PostMapping("/api/groups")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Create group.",
    description = "Create a Group"
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
        responseCode = "409",
        description = "Group already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @Throws(GroupExistsException::class, GroupNotFoundException::class)
  fun createGroup(
    @Parameter(hidden = true) authentication: Authentication,
    @Parameter(description = "Details of the group to be created.", required = true)
    @Valid @RequestBody
    createGroup: CreateGroup
  ) {
    groupsService.createGroup(authentication.name, createGroup)
  }

  @PostMapping("/api/groups/child")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Create child group.",
    description = "Create a Child Group"
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
        responseCode = "409",
        description = "Child Group already exists.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @Throws(ChildGroupExistsException::class, GroupNotFoundException::class)
  fun createChildGroup(
    @Parameter(description = "The group code of the child group.", required = true)
    @Parameter(hidden = true)
    authentication: Authentication,
    @Parameter(description = "Details of the child group to be created.", required = true)
    @Valid @RequestBody
    createChildGroup: CreateChildGroup
  ) {
    groupsService.createChildGroup(authentication.name, createChildGroup)
  }

  @DeleteMapping("/api/groups/child/{group}")
  @PreAuthorize("hasRole('ROLE_MAINTAIN_OAUTH_USERS')")
  @Operation(
    summary = "Delete child group.",
    description = "Delete a Child Group"
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
        description = "Child Group not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  fun deleteChildGroup(
    @Parameter(description = "The group code of the child group.", required = true)
    @PathVariable
    group: String,
    @Parameter(hidden = true) authentication: Authentication
  ) {
    groupsService.deleteChildGroup(authentication.name, group)
  }
}

@Schema(description = "Group Details")
data class GroupDetails(
  @Schema(required = true, description = "Group Code", example = "HDC_NPS_NE")
  val groupCode: String,

  @Schema(required = true, description = "Group Name", example = "HDC NPS North East")
  val groupName: String,

  @Schema(required = true, description = "Assignable Roles")
  val assignableRoles: List<AuthUserAssignableRole>,

  @Schema(required = true, description = "Child Groups")
  val children: List<AuthUserGroup>
) {
  constructor(g: Group) : this(
    g.groupCode,
    g.groupName,
    g.assignableRoles.map { AuthUserAssignableRole(it.role, it.automatic) }.sortedBy { it.roleName },
    g.children.map { AuthUserGroup(it) }.sortedBy { it.groupName }
  )
}

@Schema(description = "Group Name")
data class GroupAmendment(
  @Schema(required = true, description = "Group Name", example = "HDC NPS North East")
  @field:NotBlank(message = "parent group code must be supplied")
  val groupName: String
)

@Schema(description = "Group Details")
data class ChildGroupDetails(
  @Schema(required = true, description = "Group Code", example = "HDC_NPS_NE")
  val groupCode: String,

  @Schema(required = true, description = "Group Name", example = "HDC NPS North East")
  val groupName: String
) {
  constructor(g: ChildGroup) : this(g.groupCode, g.groupName)
}

data class CreateGroup(
  @Schema(required = true, description = "Group Code", example = "HDC_NPS_NE")
  @field:NotBlank(message = "group code must be supplied")
  @field:Size(min = 2, max = 30)
  @field:Pattern(regexp = "^[0-9A-Za-z_]*")
  val groupCode: String,

  @Schema(required = true, description = "groupName", example = "HDC NPS North East")
  @field:NotBlank(message = "group name must be supplied")
  @field:Size(min = 4, max = 100)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val groupName: String
)

data class CreateChildGroup(
  @Schema(required = true, description = "Parent Group Code", example = "HNC_NPS")
  @field:NotBlank(message = "parent group code must be supplied")
  @field:Size(min = 2, max = 30)
  @field:Pattern(regexp = "^[0-9A-Za-z_]*")
  val parentGroupCode: String,

  @Schema(required = true, description = "Group Code", example = "HDC_NPS_NE")
  @field:NotBlank(message = "group code must be supplied")
  @field:Size(min = 2, max = 30)
  @field:Pattern(regexp = "^[0-9A-Za-z_]*")
  val groupCode: String,

  @Schema(required = true, description = "groupName", example = "HDC NPS North East")
  @field:NotBlank(message = "group name must be supplied")
  @field:Size(min = 4, max = 100)
  @field:Pattern(regexp = "^[0-9A-Za-z- ,.()'&]*\$")
  val groupName: String
)
