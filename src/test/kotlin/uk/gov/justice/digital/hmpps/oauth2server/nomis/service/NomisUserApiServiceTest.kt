package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisApiMockServer
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension

@ExtendWith(NomisExtension::class)
class NomisUserApiServiceTest : IntegrationTest() {
  lateinit var nomisApi: NomisApiMockServer

  @Autowired
  @Qualifier("nomisWebClient")
  lateinit var webClient: WebClient

  private lateinit var nomisService: NomisUserApiService

  @BeforeEach
  fun setUp() {
    nomisService = NomisUserApiService(webClient)
  }

  @Nested
  inner class ChangePassword {
    @Test
    fun `changePassword enabled`() {
      nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
      nomisApi.verify(
        putRequestedFor(urlEqualTo("/NOMIS_PASSWORD_RESET/change-password"))
          .withRequestBody(equalTo("helloworld2"))
      )
    }
  }
}
