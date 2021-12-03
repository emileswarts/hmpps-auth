@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.oauth2server.resource.IntegrationTest
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension.Companion.nomisApi
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordValidationFailureException
import uk.gov.justice.digital.hmpps.oauth2server.security.ReusedPasswordException
import java.net.HttpURLConnection

@ExtendWith(NomisExtension::class)
class NomisUserApiServiceIntTest : IntegrationTest() {
  @Autowired
  @Qualifier("nomisWebClient")
  private lateinit var webClient: WebClient

  @Autowired
  @Qualifier("nomisUserWebClient")
  private lateinit var nomisUserWebClient: WebClient

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  private lateinit var nomisService: NomisUserApiService
  private lateinit var nomisDisabledService: NomisUserApiService

  @BeforeEach
  fun setUp() {
    nomisService = NomisUserApiService(webClient, nomisUserWebClient, true, objectMapper)
    nomisDisabledService = NomisUserApiService(webClient, nomisUserWebClient, false, objectMapper)
  }

  @Nested
  inner class changePassword {
    @Test
    fun `it will succeed when API is OK`() {
      nomisApi.stubFor(
        put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          aResponse()
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
        put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          aResponse()
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
        put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          aResponse()
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
        put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          aResponse()
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
        put(urlEqualTo("/users/NOMIS_PASSWORD_RESET/change-password")).willReturn(
          aResponse()
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

  @Test
  fun `findUsers returns a matching user`() {
    nomisApi.stubFor(
      get(
        urlEqualTo("/users/staff?firstName=First&lastName=Last")
      ).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(HttpURLConnection.HTTP_OK)
          .withBody(
            """
            [ {
        "username": "ITAG_USER",
        "staffId": 100,
        "email": "itag_user@digital.justice.gov.uk",
        "verified": true,
        "firstName": "Api",
        "lastName": "User",
        "name": "Api User",
        "activeCaseLoadId": "MDI"
    }]
            """.trimIndent()
          )
      )
    )

    val users = nomisService.findUsers("First", "Last")
    nomisApi.verify(
      getRequestedFor(
        urlEqualTo("/users/staff?firstName=First&lastName=Last")
      )
    )
    assertThat(users).isNotEmpty
  }

  @Test
  fun `findUsers returns no matching users`() {
    nomisApi.stubFor(
      get(urlEqualTo("/users/staff?firstName=First&lastName=Last")).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(HttpURLConnection.HTTP_OK)
          .withBody(
            "[]"
          )
      )
    )

    val users = nomisService.findUsers("First", "Last")
    nomisApi.verify(
      getRequestedFor(
        urlEqualTo("/users/staff?firstName=First&lastName=Last")
      )
    )
    assertThat(users).isEmpty()
  }

  @Nested
  inner class findUsersByEmailAddressAndUsernames {
    @Test
    fun `findUsers returns no matching users`() {
      nomisApi.stubFor(
        post(urlEqualTo("/users/user?email=missing@justice.gov.uk"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(HttpURLConnection.HTTP_OK)
              .withBody(
                "[]"
              )
          )
      )

      val users = nomisService.findUsersByEmailAddressAndUsernames("missing@justice.gov.uk", setOf("bob"))
      nomisApi.verify(
        postRequestedFor(
          urlEqualTo("/users/user?email=missing@justice.gov.uk")
        )
      )
      assertThat(users).isEmpty()
    }

    @Test
    fun `findUsers returns a matching user`() {
      nomisApi.stubFor(
        post(urlEqualTo("/users/user?email=itag_user@digital.justice.gov.uk"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(HttpURLConnection.HTTP_OK)
              .withBody(
                """
            [ {
        "username": "ITAG_USER",
        "staffId": 100,
        "firstName": "Api",
        "lastName": "User",
        "activeCaseloadId": "MDI",
        "active": true,
        "accountStatus": "EXPIRED",
        "accountType": "GENERAL",
        "primaryEmail": "itag_user@digital.justice.gov.uk",
        "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"]
    }]
                """.trimIndent()
              )
          )
      )

      val users = nomisService.findUsersByEmailAddressAndUsernames("itag_user@digital.justice.gov.uk", setOf())
      nomisApi.verify(
        postRequestedFor(
          urlEqualTo("/users/user?email=itag_user@digital.justice.gov.uk")
        )
      )
      assertThat(users).isNotEmpty
    }

    @Test
    fun `findUsers hydrates users that don't have an email address in NOMIS`() {
      nomisApi.stubFor(
        post(urlEqualTo("/users/user?email=itag_user@digital.justice.gov.uk"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(HttpURLConnection.HTTP_OK)
              .withBody(
                """
            [ {
        "username": "ITAG_USER",
        "staffId": 100,
        "firstName": "Api",
        "lastName": "User",
        "activeCaseloadId": "MDI",
        "active": true,
        "accountStatus": "EXPIRED",
        "accountType": "GENERAL",
        "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"]
    }]
                """.trimIndent()
              )
          )
      )

      val users = nomisService.findUsersByEmailAddressAndUsernames("itag_user@digital.justice.gov.uk", setOf())
      assertThat(users.map { it.username }).containsExactly("ITAG_USER")
    }
  }

  @Nested
  inner class FindAllActiveUsers {

    @Test
    fun `findUsers returns a page list of active users`() {
      nomisApi.stubFor(
        get(urlPathEqualTo("/users"))
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(HttpURLConnection.HTTP_OK)
              .withBody(
                """
{
    "content": [
        {
            "username": "TQQ74V",
            "staffId": 19232,
            "firstName": "Admdasa",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "WEI",
                "name": "Wealstun (HMP)"
            },
            "dpsRoleCount": 54
        },
        {
            "username": "MQO36O",
            "staffId": 14382,
            "firstName": "Ascimrd",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "WEI",
                "name": "Wealstun (HMP)"
            },
            "dpsRoleCount": 1
        },
        {
            "username": "GQB63T",
            "staffId": 66003,
            "firstName": "Auannsek",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "EEI",
                "name": "Erlestoke (HMP)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "AQT09U",
            "staffId": 50901,
            "firstName": "Doptlicrt",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "GTI",
                "name": "Gartree (HMP)"
            },
            "dpsRoleCount": 2
        },
        {
            "username": "SQY06K",
            "staffId": 422954,
            "firstName": "Eldvenwaju",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "MRI",
                "name": "Manchester (HMP)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "TQX74G",
            "staffId": 480573,
            "firstName": "O'inipeter",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "FKI",
                "name": "Frankland (HMP)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "NQT71E",
            "staffId": 127668,
            "firstName": "Olnhewjan",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "MRI",
                "name": "Manchester (HMP)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "AQX47P",
            "staffId": 61708,
            "firstName": "Uforesnie",
            "lastName": "Aalasha",
            "active": true,
            "activeCaseload": {
                "id": "LWI",
                "name": "Lewes (HMP)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "PQK16I",
            "staffId": 482249,
            "firstName": "Urksiearie",
            "lastName": "Aalonica",
            "active": true,
            "activeCaseload": {
                "id": "PDI",
                "name": "Portland (HMPYOI)"
            },
            "dpsRoleCount": 0
        },
        {
            "username": "JQS63G",
            "staffId": 466856,
            "firstName": "Admdache",
            "lastName": "Aalyle",
            "active": true,
            "activeCaseload": {
                "id": "RNI",
                "name": "Ranby (HMP)"
            },
            "dpsRoleCount": 0
        }
    ],
    "pageable": {
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "pageSize": 10,
        "pageNumber": 0,
        "paged": true,
        "unpaged": false
    },
    "last": false,
    "totalPages": 4190,
    "totalElements": 41900,
    "size": 10,
    "number": 1,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 10,
    "empty": false
}                
                """.trimIndent()
              )
          )
      )

      val users = nomisService.findAllActiveUsers(PageRequest.of(1, 20))
      nomisApi.verify(
        getRequestedFor(
          urlEqualTo("/users?status=ACTIVE&page=1&size=20")
        )
      )
      assertThat(users.content).hasSize(10)
      assertThat(users.totalPages).isEqualTo(4190)
      assertThat(users.pageable.pageNumber).isEqualTo(1)
      assertThat(users.totalElements).isEqualTo(41900)
    }
  }
}