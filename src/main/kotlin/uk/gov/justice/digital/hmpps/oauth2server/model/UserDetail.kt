package uk.gov.justice.digital.hmpps.oauth2server.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.Companion.fromNullableString
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import java.util.UUID

@Suppress("DEPRECATION")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Details")
data class UserDetail(
  @Schema(
    required = true,
    description = "Username",
    example = "DEMO_USER1"
  ) val username: String
) {

  @Schema(
    required = true,
    description = "Active",
    example = "false"
  ) var active: Boolean? = null

  @Schema(
    required = true,
    description = "Name",
    example = "John Smith"
  ) var name: String? = null

  @Schema(
    required = true,
    title = "Authentication Source",
    description = "auth for auth users, nomis for nomis authenticated users",
    example = "nomis"
  ) var authSource: AuthSource? = null

  @Deprecated("")
  @Schema(
    title = "Staff Id",
    description = "Deprecated, use userId instead",
    example = "231232"
  ) var staffId: Long? = null

  @Deprecated("")
  @Schema(
    title = "Current Active Caseload",
    description = "Deprecated, retrieve from prison API rather than auth",
    example = "MDI"
  ) var activeCaseLoadId: String? = null

  @Schema(
    title = "User Id",
    description = "Unique identifier for user, will be UUID for auth users or staff ID for nomis users",
    example = "231232"
  ) var userId: String? = null

  @Schema(
    title = "Unique Id",
    description = "Universally unique identifier for user, generated and stored in auth database for all users",
    example = "5105a589-75b3-4ca0-9433-b96228c1c8f3",
  ) var uuid: UUID? = null

  constructor(
    username: String,
    active: Boolean?,
    name: String?,
    authSource: AuthSource?,
    staffId: Long?,
    activeCaseLoadId: String?,
    userId: String?,
    uuid: UUID?,
  ) : this(username) {
    this.active = active
    this.name = name
    this.authSource = authSource
    this.staffId = staffId
    this.activeCaseLoadId = activeCaseLoadId
    this.userId = userId
    this.uuid = uuid
  }

  companion object {
    fun fromPerson(upd: UserPersonDetails, u: User): UserDetail {

      val authSource = fromNullableString(upd.authSource)
      val staffId: Long?
      val activeCaseLoadId: String?
      if (authSource === AuthSource.nomis) {
        val staffUserAccount = upd as NomisUserPersonDetails
        staffId = staffUserAccount.userId.toLong()
        activeCaseLoadId = staffUserAccount.activeCaseLoadId
      } else {
        staffId = null
        activeCaseLoadId = null
      }
      return UserDetail(
        username = upd.username,
        active = upd.isEnabled,
        name = upd.name,
        authSource = authSource,
        userId = upd.userId,
        staffId = staffId,
        activeCaseLoadId = activeCaseLoadId,
        uuid = u.id,
      )
    }

    fun fromUsername(username: String): UserDetail {
      return UserDetail(username = username)
    }
  }
}
