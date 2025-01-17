package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.CreateRole
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleAdminTypeAmendment
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleDescriptionAmendment
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleNameAmendment

class RolesServiceTest {
  private val roleRepository: RoleRepository = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val rolesService = RolesService(
    roleRepository,
    telemetryClient,
  )

  @Nested
  inner class CreateRoles {
    @Test
    fun `create role`() {
      val createRole = CreateRole(
        roleCode = "ROLE",
        roleName = "Role Name",
        roleDescription = "Role description",
        adminType = mutableSetOf(AdminType.EXT_ADM)
      )
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      rolesService.createRole("user", createRole)
      val authority = Authority("ROLE", " Role Name", "Role description", mutableListOf(AdminType.EXT_ADM))
      verify(roleRepository).findByRoleCode("ROLE")
      verify(roleRepository).save(authority)
      verify(telemetryClient).trackEvent(
        "RoleCreateSuccess",
        mapOf(
          "username" to "user",
          "roleCode" to "ROLE",
          "roleName" to "Role Name",
          "roleDescription" to "Role description",
          "adminType" to "[EXT_ADM]"
        ),
        null
      )
    }

    @Test
    fun `create role - having adminType DPS_LSA will auto add DPS_ADM`() {
      val createRole = CreateRole(
        roleCode = "ROLE",
        roleName = "Role Name",
        roleDescription = "Role description",
        adminType = mutableSetOf(AdminType.DPS_LSA)
      )
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      rolesService.createRole("user", createRole)
      val authority =
        Authority("ROLE", " Role Name", "Role description", mutableListOf(AdminType.DPS_LSA, AdminType.DPS_ADM))
      verify(roleRepository).findByRoleCode("ROLE")
      verify(roleRepository).save(authority)
      verify(telemetryClient).trackEvent(
        "RoleCreateSuccess",
        mapOf(
          "username" to "user",
          "roleCode" to "ROLE",
          "roleName" to "Role Name",
          "roleDescription" to "Role description",
          "adminType" to "[DPS_LSA, DPS_ADM]"
        ),
        null
      )
    }

    @Test
    fun `Create role exists`() {
      val createRole = CreateRole(
        roleCode = "NEW_ROLE",
        roleName = "Role Name",
        roleDescription = "Role description",
        adminType = mutableSetOf(AdminType.DPS_LSA)
      )
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(
        Authority(
          roleCode = "NEW_ROLE",
          roleName = "Role Name",
          roleDescription = "Role description",
          adminType = mutableListOf(AdminType.DPS_LSA)
        )
      )

      assertThatThrownBy {
        rolesService.createRole("user", createRole)
      }.isInstanceOf(RolesService.RoleExistsException::class.java)
        .hasMessage("Unable to create role: NEW_ROLE with reason: role code already exists")
    }
  }

  @Nested
  inner class GetRoles {
    @Test
    fun `get all roles`() {
      val role1 = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role")
      val role2 = Authority(roleCode = "RO2", roleName = "Role2", roleDescription = "Second Role")
      whenever(roleRepository.findAll(any(), any<Sort>())).thenReturn(listOf(role1, role2))

      val allRoles = rolesService.getRoles(null)
      assertThat(allRoles.size).isEqualTo(2)
    }

    @Test
    fun `get all roles returns no roles`() {
      whenever(roleRepository.findAll(any(), any<Sort>())).thenReturn(listOf())

      val allRoles = rolesService.getRoles(null)
      assertThat(allRoles.size).isEqualTo(0)
    }

    @Test
    fun `get All Roles check filter`() {
      val role1 = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role")
      val role2 = Authority(roleCode = "RO2", roleName = "Role2", roleDescription = "Second Role")
      whenever(roleRepository.findAll(any(), any<Sort>())).thenReturn(listOf(role1, role2))

      rolesService.getRoles(listOf(AdminType.DPS_ADM, AdminType.DPS_LSA))
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("adminTypes").isEqualTo(listOf(AdminType.DPS_ADM, AdminType.DPS_LSA))
        },
        eq(Sort.by(Sort.Direction.ASC, "roleName"))
      )
    }
  }

  @Nested
  inner class ManageRoles {
    @Test
    fun `get all roles`() {
      val role1 = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role")
      val role2 = Authority(roleCode = "RO2", roleName = "Role2", roleDescription = "Second Role")
      val roles = listOf(role1, role2)
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(PageImpl(roles))

      val allRoles = rolesService.getAllRoles(null, null, null, Pageable.unpaged())
      assertThat(allRoles.size).isEqualTo(2)
    }

    @Test
    fun `get all roles returns no roles`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())

      val allRoles = rolesService.getAllRoles(null, null, null, Pageable.unpaged())
      assertThat(allRoles.size).isEqualTo(0)
    }

    @Test
    fun `get All Roles check filter - multiple `() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())
      val unpaged = Pageable.unpaged()
      rolesService.getAllRoles(
        "Admin",
        "HWPV",
        listOf(AdminType.EXT_ADM, AdminType.DPS_LSA),
        unpaged,
      )
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("roleName").isEqualTo("Admin")
          assertThat(it).extracting("roleCode").isEqualTo("HWPV")
          assertThat(it).extracting("adminTypes").isEqualTo(listOf(AdminType.EXT_ADM, AdminType.DPS_LSA))
        },
        eq(unpaged)
      )
    }
    @Test
    fun `get All Roles check filter - roleName`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())
      val unpaged = Pageable.unpaged()
      rolesService.getAllRoles(
        "Admin",
        null,
        null,
        unpaged,
      )
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("roleName").isEqualTo("Admin")
        },
        eq(unpaged)
      )
    }

    @Test
    fun `get All Roles check filter - roleCode`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())
      val unpaged = Pageable.unpaged()
      rolesService.getAllRoles(
        null,
        "HWPV",
        null,
        unpaged,
      )
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("roleCode").isEqualTo("HWPV")
        },
        eq(unpaged)
      )
    }

    @Test
    fun `get All Roles check filter - adminType`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())
      val unpaged = Pageable.unpaged()
      rolesService.getAllRoles(
        null,
        null,
        listOf(AdminType.DPS_ADM, AdminType.DPS_LSA),
        unpaged,
      )
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("adminTypes").isEqualTo(listOf(AdminType.DPS_ADM, AdminType.DPS_LSA))
        },
        eq(unpaged)
      )
    }
  }

  @Nested
  inner class RoleDetail {

    @Test
    fun `get role details`() {
      val dbRole = Authority(roleCode = "RO1", roleName = "Role Name", roleDescription = "A Role")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      val role = rolesService.getRoleDetail("RO1")
      assertThat(role).isEqualTo(dbRole)
      verify(roleRepository).findByRoleCode("RO1")
    }

    @Test
    fun `get role details when no role matches`() {
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      assertThatThrownBy {
        rolesService.getRoleDetail("RO1")
      }.isInstanceOf(RoleNotFoundException::class.java)
    }
  }

  @Nested
  inner class AmendRoleName {
    @Test
    fun `update role name when no role matches`() {
      val roleAmendment = RoleNameAmendment("UpdatedName")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      assertThatThrownBy {
        rolesService.updateRoleName("user", "RO1", roleAmendment)
      }.isInstanceOf(RoleNotFoundException::class.java)
      verifyNoInteractions(telemetryClient)
    }

    @Test
    fun `update role name successfully`() {
      val dbRole = Authority(roleCode = "RO1", roleName = "Role Name", roleDescription = "A Role")
      val roleAmendment = RoleNameAmendment("UpdatedName")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleName("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleNameUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleName" to "UpdatedName"),
        null
      )
    }
  }

  @Test
  fun `create role - having adminType DPS_LSA will auto add DPS_ADM`() {
    val createRole = CreateRole(
      roleCode = "ROLE",
      roleName = "Role Name",
      roleDescription = "Role description",
      adminType = mutableSetOf(AdminType.DPS_LSA)
    )
    whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

    rolesService.createRole("user", createRole)
    val authority =
      Authority("ROLE", " Role Name", "Role description", mutableListOf(AdminType.DPS_LSA, AdminType.DPS_ADM))
    verify(roleRepository).findByRoleCode("ROLE")
    verify(roleRepository).save(authority)
    verify(telemetryClient).trackEvent(
      "RoleCreateSuccess",
      mapOf(
        "username" to "user",
        "roleCode" to "ROLE",
        "roleName" to "Role Name",
        "roleDescription" to "Role description",
        "adminType" to "[DPS_LSA, DPS_ADM]"
      ),
      null
    )
  }

  @Nested
  inner class AmendRoleDescription {
    @Test
    fun `update role description when no role matches`() {
      val roleAmendment = RoleDescriptionAmendment("UpdatedDescription")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      assertThatThrownBy {
        rolesService.updateRoleDescription("user", "RO1", roleAmendment)
      }.isInstanceOf(RoleNotFoundException::class.java)
      verifyNoInteractions(telemetryClient)
    }

    @Test
    fun `update role description successfully`() {
      val dbRole = Authority(roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc")
      val roleAmendment = RoleDescriptionAmendment("UpdatedDescription")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleDescription("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleDescriptionUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleDescription" to "UpdatedDescription"),
        null
      )
    }
  }

  @Nested
  inner class AmendRoleAdminType {
    @Test
    fun `update role admin type when no role matches`() {
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      assertThatThrownBy {
        rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      }.isInstanceOf(RoleNotFoundException::class.java)
      verifyNoInteractions(telemetryClient)
    }

    @Test
    fun `update role admin type successfully`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM, AdminType.DPS_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type with adminType DPS_LSA will auto add DPS_ADM`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM, AdminType.DPS_LSA))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_LSA, DPS_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type with adminType DPS_LSA will not add DPS_ADM if it already exists`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM, AdminType.DPS_LSA))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_LSA, DPS_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type without DPS_ADM will not remove immutable DPS_ADM if it already exists`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type without EXT_ADM will not remove immutable EXT_ADM if it already exists`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.DPS_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[DPS_ADM, EXT_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type will not duplicate existing immutable EXT_ADM`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM,)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM, AdminType.DPS_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_ADM]"),
        null
      )
    }

    @Test
    fun `update role admin type without DPS_LSA will remove DPS_LSA if it already exists`() {
      val dbRole = Authority(
        roleCode = "RO1", roleName = "Role Name", roleDescription = "Role Desc",
        adminType = listOf(AdminType.EXT_ADM, AdminType.DPS_ADM, AdminType.DPS_LSA)
      )
      val roleAmendment = RoleAdminTypeAmendment(mutableSetOf(AdminType.EXT_ADM))
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRoleAdminType("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleAdminTypeUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleAdminType" to "[EXT_ADM, DPS_ADM]"),
        null
      )
    }
  }
}
