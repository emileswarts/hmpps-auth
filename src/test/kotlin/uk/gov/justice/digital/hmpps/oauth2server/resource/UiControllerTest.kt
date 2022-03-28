package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientType
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientFilter
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientSummary
import uk.gov.justice.digital.hmpps.oauth2server.service.SortBy.count

internal class UiControllerTest {
  private val clientService: ClientService = mock()
  private val controller = UiController(clientService)

  @Test
  internal fun test() {
    val clients = listOf(
      ClientSummary(
        baseClientId = "client-1",
        grantTypes = "bob",
        roles = "role",
        count = 5,
        clientType = ClientType.PERSONAL,
        service = "Some service",
        teamName = "name",
        lastAccessed = null,
        lastAccessedTime = null,
        secretUpdated = null,
        secretUpdatedTime = null,
      )
    )
    val filterBy = ClientFilter(role = "bob")
    whenever(clientService.listUniqueClients(any(), any())).thenReturn(clients)
    val modelAndView = controller.userIndex(count, role = "bob")
    assertThat(modelAndView.viewName).isEqualTo("ui/index")
    assertThat(modelAndView.model["clientDetails"]).isEqualTo(clients)

    verify(clientService).listUniqueClients(count, filterBy)
  }

  @Test
  internal fun `view only test`() {
    val clients = listOf(
      ClientSummary(
        baseClientId = "client-1",
        grantTypes = "bob",
        roles = "role",
        count = 5,
        clientType = ClientType.PERSONAL,
        service = "Some service",
        teamName = "name",
        lastAccessed = null,
        lastAccessedTime = null,
        secretUpdated = null,
        secretUpdatedTime = null,
      )
    )
    val filterBy = ClientFilter(role = "bob")
    whenever(clientService.listUniqueClients(any(), any())).thenReturn(clients)
    val modelAndView = controller.userIndexViewOnly(count, role = "bob")
    assertThat(modelAndView.viewName).isEqualTo("ui/viewOnlyIndex")
    assertThat(modelAndView.model["clientDetails"]).isEqualTo(clients)

    verify(clientService).listUniqueClients(count, filterBy)
  }
}
