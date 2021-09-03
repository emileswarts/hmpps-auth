package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository

class RolesServiceTest {
  private val roleRepository: RoleRepository = mock()
  private val rolesService = RolesService(
    roleRepository,
  )

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
      com.nhaarman.mockitokotlin2.check {
        assertThat(it).extracting("roleCodes").isEqualTo(null)
      },
      eq(unpaged)
    )
  }
}
