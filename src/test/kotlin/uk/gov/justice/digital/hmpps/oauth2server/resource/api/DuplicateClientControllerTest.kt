@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientDeployment
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientDuplicateIdsAndDeployment
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.DuplicateClientsException

class DuplicateClientControllerTest {

  private val clientService: ClientService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val authentication = TestingAuthenticationToken(
    "duplicate",
    "pass",
    "ROLE_OAUTH_ADMIN"
  )
  private val duplicateClientController = DuplicateClientController(clientService, telemetryClient)

  @Test
  fun `get client request`() {
    val clientDetails = ClientDuplicateIdsAndDeployment(
      "client", listOf("client-1"),
      ClientDeployment(baseClientId = "client")
    )
    whenever(clientService.loadClientAndDeployment(anyString())).thenReturn(clientDetails)
    val returnedClientDetails = duplicateClientController.getClientIdsAndDeployment(authentication, "client")

    assertThat(returnedClientDetails).isEqualTo(clientDetails)
    verify(clientService).loadClientAndDeployment("client")
  }

  @Test
  fun `get Client Request - get client throws NoSuchClientException`() {

    val exception = NoSuchClientException("No client found with id = ")
    doThrow(exception).whenever(clientService).loadClientAndDeployment(anyString())

    assertThatThrownBy { duplicateClientController.getClientIdsAndDeployment(authentication, "client") }.isEqualTo(
      exception
    )
  }

  @Test
  fun `Duplicate client`() {
    val client = BaseClientDetails(
      "client-1", null,
      "read,write", "client_credentials", "ROLE_"
    )
    client.clientSecret = "SOME-RANDOM-STRING"
    whenever(clientService.duplicateClient(anyString())).thenReturn(client)
    val newDuplicateClient = duplicateClientController.duplicateClient(authentication, "client")

    assertThat(newDuplicateClient).isEqualTo(DuplicateClientDetail(client))
    assertThat(newDuplicateClient.clientId).isEqualTo("client-1")
    assertThat(newDuplicateClient.clientSecret).isEqualTo("SOME-RANDOM-STRING")
    assertThat(newDuplicateClient.base64ClientId).isEqualTo("Y2xpZW50LTE=")
    assertThat(newDuplicateClient.base64ClientSecret).isEqualTo("U09NRS1SQU5ET00tU1RSSU5H")

    verify(telemetryClient).trackEvent(
      "AuthClientDetailsApiDuplicated",
      mapOf("username" to "duplicate", "clientId" to "client"),
      null
    )
  }

  @Test
  fun `Duplicate client fails - Max Duplicates reached`() {
    doThrow(DuplicateClientsException("custard", "MaxReached")).whenever(clientService).duplicateClient(anyString())

    assertThatThrownBy { duplicateClientController.duplicateClient(authentication, "max-client") }
      .isInstanceOf(DuplicateClientsException::class.java)
      .withFailMessage("Duplicate clientId failed for max-client with reason: MaxReached")
  }

  @Test
  fun `Duplicate client fails - client not found`() {
    doThrow(NoSuchClientException("No client with requested id: non-client")).whenever(clientService)
      .duplicateClient(anyString())

    assertThatThrownBy { duplicateClientController.duplicateClient(authentication, "non-client") }
      .isInstanceOf(NoSuchClientException::class.java)
      .withFailMessage("No client with requested id: non-client")
  }

  @Test
  fun `Delete client`() {
    duplicateClientController.deleteClient(authentication, "client")

    verify(clientService).removeClient("client")

    verify(telemetryClient).trackEvent(
      "AuthClientDetailsApiDeleted",
      mapOf("username" to "duplicate", "clientId" to "client"),
      null
    )
  }

  @Test
  fun `delete Client Request - delete client throws NoSuchClientException`() {

    val exception = NoSuchClientException("No client found with id = ")
    doThrow(exception).whenever(clientService).removeClient(anyString())

    assertThatThrownBy { duplicateClientController.deleteClient(authentication, "client") }.isEqualTo(exception)
  }
}
