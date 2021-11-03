package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisApiMockServer
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException
import java.net.HttpURLConnection

@ExtendWith(NomisExtension::class)
class NomisUserApiServiceTest : IntegrationTest() {
  lateinit var nomisApi: NomisApiMockServer

  @Autowired
  @Qualifier("nomisWebClient")
  lateinit var webClient: WebClient

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private lateinit var nomisService: NomisUserApiService

  @BeforeEach
  fun setUp() {
    nomisService = NomisUserApiService(webClient, objectMapper)
  }

  @DisplayName("changePassword")
  @Nested
  inner class ChangePassword {
    @Test
    fun `it will succeed when API is OK`() {
      nomisApi.stubFor(
        WireMock.put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpURLConnection.HTTP_OK)
        )
      )

      nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
      nomisApi.verify(
        putRequestedFor(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password"))
          .withRequestBody(equalTo("helloworld2"))
      )
    }

    @Test
    fun `it will throw an exception for unexpected errors`() {
      nomisApi.stubFor(
        WireMock.put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "status": 500,
                  "userMessage": "NullPointerException",
                  "developerMessage": "NullPointerException"
                }
              """.trimIndent()
            )
            .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )
      )

      assertThatThrownBy { nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(InternalServerError::class.java)
    }

    @Test
    fun `it will throw a PasswordValidationFailureException for simple validation failures`() {
      nomisApi.stubFor(
        WireMock.put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                    "status": 400,
                    "errorCode": 1000,
                    "userMessage": "Validation failure: changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars",
                    "developerMessage": "changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars"
                }
              """.trimIndent()
            )
            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
        )
      )

      assertThatThrownBy { nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(PasswordValidationFailureException::class.java)
    }

    @Test
    fun `it will throw a PasswordValidationFailureException for NOMIS validation failures`() {
      nomisApi.stubFor(
        WireMock.put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                    "status": 400,
                    "errorCode": 1002,
                    "userMessage": "Password is not valid and has been rejected by NOMIS due to ORA-20001:  Password must contain at least 1 numeric character. Please re-enter password.\nORA-06512: at \"OMS_OWNER.TAG_ERROR\", line 169\nORA-06512: at \"OMS_OWNER.TAG_ERROR\", line 85\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 838\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 867\n",
                    "developerMessage": "Password is not valid and has been rejected by NOMIS due to ORA-20001:  Password must contain at least 1 numeric character. Please re-enter password.\nORA-06512: at \"OMS_OWNER.TAG_ERROR\", line 169\nORA-06512: at \"OMS_OWNER.TAG_ERROR\", line 85\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 838\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 867\n"
                }              
              """.trimIndent()
            )
            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
        )
      )

      assertThatThrownBy { nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(PasswordValidationFailureException::class.java)
    }

    @Test
    fun `it will throw a ReusedPasswordException when NOMIS detects password has been used before`() {
      nomisApi.stubFor(
        WireMock.put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                    "status": 400,
                    "errorCode": 1001,
                    "userMessage": "Password has been used before and was rejected by NOMIS due to ORA-20087: Password cannot be reused\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 870\n",
                    "developerMessage": "Password has been used before and was rejected by NOMIS due to ORA-20087: Password cannot be reused\nORA-06512: at \"OMS_OWNER.OMS_UTILS\", line 870\n"
                }                
              """.trimIndent()
            )
            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
        )
      )

      assertThatThrownBy { nomisService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(ReusedPasswordException::class.java)
    }
  }

  @Test
  fun lockAccount() {
    nomisService.lockAccount("NOMIS_PASSWORD_RESET")
    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/NOMIS_PASSWORD_RESET/lock-user"))
    )
  }

  @Test
  fun unlockAccount() {
    nomisService.unlockAccount("NOMIS_PASSWORD_RESET")
    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/NOMIS_PASSWORD_RESET/unlock-user"))
    )
  }
}
