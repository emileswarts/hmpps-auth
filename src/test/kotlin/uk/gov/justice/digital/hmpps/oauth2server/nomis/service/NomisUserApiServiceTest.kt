@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.web.reactive.function.client.WebClient

class NomisUserApiServiceTest {
  private var webClient: WebClient = mock()
  private var nomisUserWebClient: WebClient = mock()
  private var objectMapper: ObjectMapper = mock()
  private var nomisDisabledService = NomisUserApiService(webClient, nomisUserWebClient, false, objectMapper)
  private var nomisService = NomisUserApiService(webClient, nomisUserWebClient, true, objectMapper)

  @Nested
  inner class changePassword {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class changeEmail {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.changeEmail("NOMIS_CHANGE_EMAIL", "changing@justice.gov.uk")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class lockAccount {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.lockAccount("NOMIS_PASSWORD_RESET")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class unlockAccount {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.unlockAccount("NOMIS_PASSWORD_RESET")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class findUsersByEmailAddressAndUsernames {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUsersByEmailAddressAndUsernames("missing@justice.gov.uk", setOf("bob"))
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class findUsers {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUsers("Joe", "Bloggs")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class findUserByUsername {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUserByUsername("Joe")
      verifyNoInteractions(webClient)
    }

    @Test
    fun `it will do nothing when username contains @`() {
      nomisService.findUserByUsername("Joe@justice.gov.uk")
      verifyNoInteractions(webClient)
    }
  }

  @Nested
  inner class authenticateUser {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.authenticateUser("Joe", "password")
      verifyNoInteractions(webClient)
    }
  }
}
