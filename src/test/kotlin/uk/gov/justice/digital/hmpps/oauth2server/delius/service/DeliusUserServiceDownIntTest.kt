package uk.gov.justice.digital.hmpps.oauth2server.delius.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.oauth2server.config.DeliusRoleMappings
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.delius
import uk.gov.justice.digital.hmpps.oauth2server.utils.ServiceUnavailableThreadLocal

class DeliusUserServiceDownIntTest : IntegrationTest() {

  @Autowired
  @Qualifier("deliusWebClient")
  private lateinit var webClient: WebClient

  @Autowired
  private lateinit var deliusRoleMappings: DeliusRoleMappings

  private lateinit var deliusService: DeliusUserService

  @BeforeEach
  fun setUp() {
    deliusService = DeliusUserService(webClient, true, deliusRoleMappings)
  }

  @Test
  fun `Delius unavailable, service unavailable thread local contains AuthSource nomis`() {
    val user = deliusService.getDeliusUserByUsername("TEST_USER")

    assertThat(user).isEmpty

    assertThat(ServiceUnavailableThreadLocal.containsAuthSource(delius)).isTrue
  }
}
