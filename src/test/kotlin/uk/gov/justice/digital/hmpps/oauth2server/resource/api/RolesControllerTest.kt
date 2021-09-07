@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException

class RolesControllerTest {
  private val rolesService: RolesService = mock()

  private val rolesController = RolesController(rolesService)

  @Nested
  inner class ManageRoles {
    @Test
    fun `get roles`() {
      val role1 = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role")
      val role2 = Authority(roleCode = "RO2", roleName = "Role2", roleDescription = "Second Role")
      val roles = listOf(role1, role2)
      whenever(rolesService.getAllRoles(any())).thenReturn(PageImpl(roles))

      val allRoles = rolesController.getAllRoles(Pageable.unpaged())
      verify(rolesService).getAllRoles(
        Pageable.unpaged(),
      )
      assertThat(allRoles.size).isEqualTo(2)
    }

    @Test
    fun `No Roles Found`() {
      whenever(rolesService.getAllRoles(any())).thenReturn(PageImpl(listOf()))

      val noRoles = rolesController.getAllRoles(Pageable.unpaged())
      verify(rolesService).getAllRoles(
        Pageable.unpaged(),
      )
      assertThat(noRoles.size).isEqualTo(0)
    }
  }

  @Nested
  inner class RoleDetail {
    @Test
    fun `Get role details`() {
      val role = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role", adminType = listOf("DPS_ADM"))

      whenever(rolesService.getRoleDetail(any())).thenReturn(role)

      val roleDetails = rolesController.getRoleDetail("RO1")
      assertThat(roleDetails).isEqualTo(
        RoleDetails(
          roleCode = "RO1",
          roleName = "Role1",
          roleDescription = "First Role",
          adminType = listOf(RoleAdminType(AdminType.DPS_ADM, "DPS Central Administrator"))
        )
      )
    }

    @Test
    fun `Get role details with no match throws exception`() {
      whenever(rolesService.getRoleDetail(any())).thenThrow(RoleNotFoundException("find", "NoRole", "not found"))

      assertThatThrownBy { rolesController.getRoleDetail("ROLE_DOES_NOT_EXIST") }
        .isInstanceOf(RoleNotFoundException::class.java)
        .withFailMessage("Unable to find role: NoRole with reason: not found")
    }
  }
}
