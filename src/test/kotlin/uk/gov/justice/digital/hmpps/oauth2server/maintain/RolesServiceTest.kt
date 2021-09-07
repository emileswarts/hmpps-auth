package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleAmendment

class RolesServiceTest {
  private val roleRepository: RoleRepository = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val rolesService = RolesService(
    roleRepository,
    telemetryClient
  )

  @Nested
  inner class ManageRoles {
    @Test
    fun `get all roles`() {
      val role1 = Authority(roleCode = "RO1", roleName = "Role1", roleDescription = "First Role")
      val role2 = Authority(roleCode = "RO2", roleName = "Role2", roleDescription = "Second Role")
      val roles = listOf(role1, role2)
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(PageImpl(roles))

      val allRoles = rolesService.getAllRoles(Pageable.unpaged())
      assertThat(allRoles.size).isEqualTo(2)
    }

    @Test
    fun `get all roles returns no roles`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())

      val allRoles = rolesService.getAllRoles(Pageable.unpaged())
      assertThat(allRoles.size).isEqualTo(0)
    }

    @Test
    fun `get All Roles check filter`() {
      whenever(roleRepository.findAll(any(), any<Pageable>())).thenReturn(Page.empty())
      val unpaged = Pageable.unpaged()
      rolesService.getAllRoles(
        unpaged,
      )
      verify(roleRepository).findAll(
        check {
          assertThat(it).extracting("roleCodes").isEqualTo(null)
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
      val roleAmendment = RoleAmendment("UpdatedName")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(null)

      assertThatThrownBy {
        rolesService.updateRole("user", "RO1", roleAmendment)
      }.isInstanceOf(RoleNotFoundException::class.java)
      verifyZeroInteractions(telemetryClient)
    }

    @Test
    fun `update role name successfully`() {
      val dbRole = Authority(roleCode = "RO1", roleName = "Role Name", roleDescription = "A Role")
      val roleAmendment = RoleAmendment("UpdatedName")
      whenever(roleRepository.findByRoleCode(anyString())).thenReturn(dbRole)

      rolesService.updateRole("user", "RO1", roleAmendment)
      verify(roleRepository).findByRoleCode("RO1")
      verify(roleRepository).save(dbRole)
      verify(telemetryClient).trackEvent(
        "RoleUpdateSuccess",
        mapOf("username" to "user", "roleCode" to "RO1", "newRoleName" to "UpdatedName"),
        null
      )
    }
  }
}
