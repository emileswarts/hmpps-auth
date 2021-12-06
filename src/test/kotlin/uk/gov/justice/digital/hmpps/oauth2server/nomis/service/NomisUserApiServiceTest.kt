@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.nomis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class NomisUserApiServiceTest {
  private var webClient: WebClient = mock()
  private var nomisUserWebClient: WebClient = mock()
  private var objectMapper: ObjectMapper = mock()
  private var nomisDisabledService = NomisUserApiService(webClient, nomisUserWebClient, false, objectMapper)

  @Nested
  inner class changePassword {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
      verifyZeroInteractions(webClient)
    }
  }

  @Nested
  inner class lockAccount {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.lockAccount("NOMIS_PASSWORD_RESET")
      verifyZeroInteractions(webClient)
    }
  }

  @Nested
  inner class unlockAccount {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.unlockAccount("NOMIS_PASSWORD_RESET")
      verifyZeroInteractions(webClient)
    }
  }

  @Nested
  inner class findUsersByEmailAddressAndUsernames {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUsersByEmailAddressAndUsernames("missing@justice.gov.uk", setOf("bob"))
      verifyZeroInteractions(webClient)
    }
  }

  @Nested
  inner class findUsers {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUsers("Joe", "Bloggs")
      verifyZeroInteractions(webClient)
    }
  }

  @Nested
  inner class findUserByUsername {
    @Test
    fun `it will do nothing when disabled`() {
      nomisDisabledService.findUserByUsername("Joe")
      verifyZeroInteractions(webClient)
    }
  }
}
