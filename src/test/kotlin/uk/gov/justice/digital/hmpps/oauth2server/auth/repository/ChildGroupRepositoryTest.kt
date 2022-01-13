package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.config.AuthDbConfig
import uk.gov.justice.digital.hmpps.oauth2server.config.FlywayConfig

@DataJpaTest
@ActiveProfiles("test")
@Import(AuthDbConfig::class, FlywayConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ChildGroupRepositoryTest {

  @Autowired
  private lateinit var repository: ChildGroupRepository

  @Test
  fun givenAnExistingRoleTheyCanBeRetrieved() {
    val retrievedEntity = repository.findByGroupCode("CHILD_2")
    assertThat(retrievedEntity?.groupCode).isEqualTo("CHILD_2")
    assertThat(retrievedEntity?.groupName).isEqualTo("Child - Site 2 - Group 1")
  }
}
