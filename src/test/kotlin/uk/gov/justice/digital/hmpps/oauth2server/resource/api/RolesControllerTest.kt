@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl

class RolesControllerTest {
  private val rolesService: RolesService = mock()
  private val rolesController = RolesController(rolesService)
  private val authentication =
    TestingAuthenticationToken(
      UserDetailsImpl("user", "name", ROLES_ADMIN_USER, AuthSource.auth.name, "userid", "jwtId"),
      "pass",
      "ROLE_ROLES_ADMIN"
    )

  @Nested
  inner class CreateRole {
    @Test
    fun create() {
      val newRole = CreateRole("CG", "Role", "Desc", mutableSetOf(AdminType.EXT_ADM))
      rolesController.createRole(authentication, newRole)
      verify(rolesService).createRole("user", newRole)
    }

    @Test
    fun `create - role can be created when description not present `() {
      val newRole = CreateRole(roleCode = "CG", roleName = "Role", adminType = mutableSetOf(AdminType.EXT_ADM, AdminType.EXT_ADM))
      rolesController.createRole(authentication, newRole)
      verify(rolesService).createRole("user", newRole)
    }

    @Test
    fun `create - role already exist exception`() {
      doThrow(RolesService.RoleExistsException("_code", "role code already exists")).whenever(rolesService)
        .createRole(
          anyString(),
          any()
        )

      @Suppress("ClassName") val role = CreateRole("_code", " Role", "Description", mutableSetOf(AdminType.DPS_ADM))
      assertThatThrownBy { rolesController.createRole(authentication, role) }
        .isInstanceOf(RolesService.RoleExistsException::class.java)
        .withFailMessage("Unable to maintain role: code with reason: role code already exists")
    }
  }

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
      val role = Authority(
        roleCode = "RO1",
        roleName = "Role1",
        roleDescription = "First Role",
        adminType = listOf(AdminType.DPS_ADM)
      )

      whenever(rolesService.getRoleDetail(any())).thenReturn(role)

      val roleDetails = rolesController.getRoleDetail("RO1")
      assertThat(roleDetails).isEqualTo(
        RoleDetails(
          roleCode = "RO1",
          roleName = "Role1",
          roleDescription = "First Role",
          adminType = listOf(AdminType.DPS_ADM)
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

  @Nested
  inner class AmendRoleName {
    @Test
    fun `amend role name`() {
      val roleAmendment = RoleNameAmendment("role")
      rolesController.amendRoleName("role1", authentication, roleAmendment)
      verify(rolesService).updateRoleName("user", "role1", roleAmendment)
    }

    @Test
    fun `amend role name with no match throws exception`() {
      whenever(rolesService.updateRoleName(anyString(), anyString(), any())).thenThrow(RoleNotFoundException("find", "NoRole", "not found"))
      val roleAmendment = RoleNameAmendment("role")

      assertThatThrownBy { rolesController.amendRoleName("NoRole", authentication, roleAmendment) }
        .isInstanceOf(RoleNotFoundException::class.java)
        .withFailMessage("Unable to find role: NoRole with reason: not found")
    }
  }

  @Nested
  inner class AmendRoleDescription {
    @Test
    fun `amend role description`() {
      val roleAmendment = RoleDescriptionAmendment("roleDesc")
      rolesController.amendRoleDescription("role1", authentication, roleAmendment)
      verify(rolesService).updateRoleDescription("user", "role1", roleAmendment)
    }

    @Test
    fun `amend role description if no description set`() {
      val roleAmendment = RoleDescriptionAmendment(null)
      rolesController.amendRoleDescription("role1", authentication, roleAmendment)
      verify(rolesService).updateRoleDescription("user", "role1", roleAmendment)
    }

    @Test
    fun `amend role description with no match throws exception`() {
      whenever(rolesService.updateRoleDescription(anyString(), anyString(), any())).thenThrow(RoleNotFoundException("find", "NoRole", "not found"))
      val roleAmendment = RoleDescriptionAmendment("role description")

      assertThatThrownBy { rolesController.amendRoleDescription("NoRole", authentication, roleAmendment) }
        .isInstanceOf(RoleNotFoundException::class.java)
        .withFailMessage("Unable to find role: NoRole with reason: not found")
    }
  }

  @Nested
  inner class AmendRoleAdminType {
    @Test
    fun `amend role admin type`() {
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.DPS_ADM))
      rolesController.amendRoleAdminType("role1", authentication, roleAmendment)
      verify(rolesService).updateRoleAdminType("user", "role1", roleAmendment)
    }

    @Test
    fun `amend role admin type with no match throws exception`() {
      whenever(rolesService.updateRoleAdminType(anyString(), anyString(), any())).thenThrow(RoleNotFoundException("find", "NoRole", "not found"))
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.DPS_ADM))

      assertThatThrownBy { rolesController.amendRoleAdminType("NoRole", authentication, roleAmendment) }
        .isInstanceOf(RoleNotFoundException::class.java)
        .withFailMessage("Unable to find role: NoRole with reason: not found")
    }
  }

  companion object {
    private val ROLES_ADMIN_USER = listOf(SimpleGrantedAuthority("ROLE_ROLES_ADMIN"))
  }
}
