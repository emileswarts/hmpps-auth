package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientConfig
import uk.gov.justice.digital.hmpps.oauth2server.config.AuthDbConfig
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@Import(AuthDbConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ClientConfigRepositoryTest {

  @Autowired
  private lateinit var repository: ClientConfigRepository

  @Test
  fun givenATransientEntityItCanBePersisted() {
    val transientEntity = transientEntity()
    val entity = ClientConfig(transientEntity.baseClientId, transientEntity.ips, transientEntity.clientEndDate)
    val persistedEntity = repository.save(entity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    Assertions.assertThat(persistedEntity.baseClientId).isNotNull
    TestTransaction.start()
    val retrievedEntity = repository.findByIdOrNull(entity.baseClientId)

    // equals only compares the business key columns
    Assertions.assertThat(retrievedEntity).isEqualTo(transientEntity)
    Assertions.assertThat(retrievedEntity?.clientEndDate).isEqualTo(transientEntity.clientEndDate)
    Assertions.assertThat(retrievedEntity?.ips).isEqualTo(transientEntity.ips)
    Assertions.assertThat(retrievedEntity?.baseClientId).isEqualTo(transientEntity.baseClientId)
    Assertions.assertThat(retrievedEntity?.allowedIpsWithNewlines).isEqualTo(
      transientEntity.allowedIpsWithNewlines
    )
  }

  private fun transientEntity(): ClientConfig = ClientConfig("hdc", listOf("127.0.0.1"), LocalDate.of(2022, 6, 1))
}
