package uk.gov.justice.digital.hmpps.oauth2server.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ChildGroup
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group

@Schema(description = "User Group")
data class AuthUserGroup(
  @Schema(required = true, description = "Group Code", example = "HDC_NPS_NE")
  val groupCode: String,

  @Schema(required = true, description = "Group Name", example = "HDC NPS North East")
  val groupName: String,
) {

  constructor(g: Group) : this(g.groupCode, g.groupName)
  constructor(g: ChildGroup) : this(g.groupCode, g.groupName)
}
