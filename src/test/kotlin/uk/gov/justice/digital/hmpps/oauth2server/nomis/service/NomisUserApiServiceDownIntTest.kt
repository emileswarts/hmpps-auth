package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.utils.ServiceUnavailableThreadLocal

class NomisUserApiServiceDownIntTest : IntegrationTest() {

  @Autowired
  @Qualifier("nomisWebClient")
  private lateinit var webClient: WebClient

  @Autowired
  @Qualifier("nomisUserWebClient")
  private lateinit var nomisUserWebClient: WebClient

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  private lateinit var nomisService: NomisUserApiService

  @BeforeEach
  fun setUp() {
    nomisService = NomisUserApiService(webClient, nomisUserWebClient, true, objectMapper)
  }

  @Test
  fun `Noms unavailable, service unavailable thread local contains AuthSource nomis`() {
    val user = nomisService.findUserByUsername("TEST_USER")

    assertThat(user).isNull()

    assertThat(ServiceUnavailableThreadLocal.service!!.contains(AuthSource.nomis))
  }
}
