package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.config.AuthDbConfig
import uk.gov.justice.digital.hmpps.oauth2server.config.FlywayConfig
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService.AuthUserRoleException
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
@Import(AuthDbConfig::class, FlywayConfig::class)
@AutoConfigureTestDatabase(replace = NONE)
@Transactional
class RoleRepositoryTest {
  @Autowired
  private lateinit var repository: RoleRepository

  @Test
  fun givenATransientEntityItCanBePersisted() {
    val transientEntity = transientEntity()
    val entity = Authority(roleCode = transientEntity.authority, roleName = transientEntity.roleName, adminType = transientEntity.adminType)
    val persistedEntity = repository.save(entity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    assertThat(persistedEntity.authority).isNotNull
    TestTransaction.start()
    val retrievedEntity = repository.findByRoleCode(entity.roleCode) ?: throw
    AuthUserRoleException("role", "role.notfound")

    // equals only compares the business key columns
    assertThat(retrievedEntity).isEqualTo(transientEntity)
    assertThat(retrievedEntity.authority).isEqualTo(transientEntity.authority)
    assertThat(retrievedEntity.roleName).isEqualTo(transientEntity.roleName)
    assertThat(retrievedEntity.adminType).isEqualTo(transientEntity.adminType)

    // clear up to prevent subsequent tests failing
    repository.delete(retrievedEntity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun givenAnExistingRoleTheyCanBeRetrieved() {
    val retrievedEntity = repository.findByRoleCode("PECS_POLICE") ?: throw
    AuthUserRoleException("role", "role.notfound")
    assertThat(retrievedEntity.authority).isEqualTo("ROLE_PECS_POLICE")
    assertThat(retrievedEntity.roleName).isEqualTo("PECS Police")
  }

  @Test
  fun `findAllByOrderByRoleNameLike EXT_ADM`() {
    assertThat(repository.findAllByOrderByRoleNameLike(AdminType.EXT_ADM.adminTypeCode)).extracting<String> { obj: Authority -> obj.authority }
      .contains("ROLE_GLOBAL_SEARCH", "ROLE_PECS_POLICE")
  }

  @Test
  fun `findAllByOrderByRoleNameLike DPS_ADM`() {
    assertThat(repository.findAllByOrderByRoleNameLike(AdminType.DPS_ADM.adminTypeCode)).extracting<String> { obj: Authority -> obj.authority }
      .contains("ROLE_GLOBAL_SEARCH", "ROLE_UNIT_TEST_DPS_ROLE")
  }

  @Test
  fun `findAllByOrderByRoleNameLike EXT_ADM does not contain DPS Roles`() {
    assertThat(repository.findAllByOrderByRoleNameLike(AdminType.EXT_ADM.adminTypeCode)).extracting<String> { obj: Authority -> obj.authority }
      .doesNotContain("ROLES_TEST_DPS")
  }

  @Test
  fun findByGroupAssignableRolesForUsername() {
    assertThat(repository.findByGroupAssignableRolesForUsername("AUTH_RO_VARY_USER")).extracting<String> { obj: Authority -> obj.roleCode }
      .containsExactly("GLOBAL_SEARCH", "LICENCE_RO", "LICENCE_VARY")
  }

  @Test
  fun findByGroupAssignableRolesForUserId() {
    assertThat(
      repository.findByGroupAssignableRolesForUserId(
        UUID.fromString("5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8")
      )
    ).extracting<String> { obj: Authority -> obj.roleCode }
      .containsExactly("GLOBAL_SEARCH", "LICENCE_RO", "LICENCE_VARY")
  }

  private fun transientEntity() = Authority(roleCode = "hdc", roleName = "Licences", adminType = listOf(AdminType.EXT_ADM))
}
