package uk.gov.justice.digital.hmpps.oauth2server.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.EmailDomainRepository
import uk.gov.justice.digital.hmpps.oauth2server.config.AuthDbConfig
import uk.gov.justice.digital.hmpps.oauth2server.config.FlywayConfig

@DataJpaTest
@ActiveProfiles("test")
@Import(AuthDbConfig::class, FlywayConfig::class)
@AutoConfigureTestDatabase(replace = NONE)
class EmailDomainRepositoryTest {
  @Autowired
  private lateinit var repository: EmailDomainRepository

  @Test
  fun givenATransientEntityItCanBePersisted() {
    val transientEntity = transientEntity()
    val entity = transientEntity.copy()
    val persistedEntity = repository.save(entity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    assertThat(persistedEntity.id).isNotNull
    TestTransaction.start()
    val retrievedEntity = repository.findById(entity.id!!).orElseThrow()

    assertThat(retrievedEntity.name).isEqualTo(transientEntity.name)
    assertThat(retrievedEntity.description).isEqualTo(transientEntity.description)
  }

  @Test
  fun givenAnExistingUserTheyCanBeRetrieved() {
    val retrievedEntity = repository.findAll().first()
    assertThat(retrievedEntity.name).isEqualTo("%advancecharity.org.uk")
  }

  @Test
  fun shouldRetrieveAllDomainsOrderedByName() {
    val allDomains = repository.findAllByOrderByName()

    assertTrue(allDomains.isNotEmpty())
    assertTrue(allDomains.asSequence().zipWithNext { a, b -> a.name.compareTo(b.name) }.all { true })
  }

  @Test
  fun shouldRetrieveDomainByName() {
    val retrievedEntity = repository.findByName("%advancecharity.org.uk")

    assertTrue(retrievedEntity != null)
    assertThat(retrievedEntity?.name).isEqualTo("%advancecharity.org.uk")
  }

  private fun transientEntity() = EmailDomain(id = null, name = "gov.uk", description = "some description")
}
