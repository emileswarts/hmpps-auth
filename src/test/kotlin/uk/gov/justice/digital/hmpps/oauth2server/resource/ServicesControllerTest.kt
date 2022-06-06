package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.validation.BindingResult
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService

class ServicesControllerTest {
  private val authServicesService: AuthServicesService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val controller = ServicesController(authServicesService, telemetryClient)
  private val authentication =
    TestingAuthenticationToken(UserDetailsImpl("user", "name", setOf(), auth.name, "userid", "jwtId"), "pass")

  @Nested
  inner class ListRequest {
    @Test
    fun `calls service list`() {
      val services = mutableListOf(Service(code = "", name = "", description = "", url = ""))
      whenever(authServicesService.list()).thenReturn(services)
      val userIndex = controller.userIndex()
      assertThat(userIndex.viewName).isEqualTo("ui/services")
      assertThat(userIndex.model).containsExactlyEntriesOf(mapOf("serviceDetails" to services))
      verify(authServicesService).list()
    }
  }

  @Nested
  inner class EditFormRequest {
    @Test
    fun `show edit form request view add service`() {
      val view = controller.showEditForm(null, true)

      assertThat(view.viewName).isEqualTo("ui/service")
      assertThat(view.model).containsExactlyEntriesOf(mapOf("service" to Service(code = "", name = "", description = "", url = ""), "newService" to "new"))

      verifyNoInteractions(authServicesService)
    }

    @Test
    fun `show edit form request view edit service`() {
      val service = Service(code = "somecode", name = "", description = "", url = "")
      whenever(authServicesService.getService(anyString())).thenReturn(service)
      val view = controller.showEditForm("code", false)

      assertThat(view.viewName).isEqualTo("ui/service")
      assertThat(view.model).containsExactlyEntriesOf(mapOf("service" to service, "newService" to "existing"))

      verify(authServicesService).getService("code")
    }
  }

  @Nested
  inner class EditService {

    private val bindingResult: BindingResult = mock()

    @Test
    fun `edit service - add service`() {
      val service = Service(code = "newcode", name = "", description = "", url = "")
      val url = controller.editService(authentication, service, bindingResult, true)
      assertThat(url.viewName).isEqualTo("redirect:/ui/services")
      assertThat(url.model).isEmpty()
      verify(authServicesService).addService(service)
      verify(telemetryClient).trackEvent(
        "AuthServiceDetailsAdd",
        mapOf("username" to "user", "code" to "newcode"),
        null
      )
    }

    @Test
    fun `edit service - add service validates form`() {
      whenever(bindingResult.hasErrors()).thenReturn(true)

      val service = Service(code = "", name = "Some Name", description = "Some Description", url = "Some URL")
      val url = controller.editService(authentication, service, bindingResult, true)
      assertThat(url.viewName).isEqualTo("ui/service")
      assertThat(url.model["service"]).isEqualTo(service)
    }

    @Test
    fun `edit service - edit service`() {
      val service = Service(code = "editcode", name = "", description = "", url = "")
      val url = controller.editService(authentication, service, bindingResult)
      assertThat(url.viewName).isEqualTo("redirect:/ui/services")
      assertThat(url.model).isEmpty()
      verify(authServicesService).updateService(service)
      verify(telemetryClient).trackEvent(
        "AuthServiceDetailsUpdate",
        mapOf("username" to "user", "code" to "editcode"),
        null
      )
    }
  }

  @Nested
  inner class DeleteService {
    @Test
    fun `delete service`() {
      val url = controller.deleteService(authentication, "code")
      assertThat(url).isEqualTo("redirect:/ui/services")
      verify(authServicesService).removeService("code")
      verify(telemetryClient).trackEvent(
        "AuthServiceDetailsDeleted",
        mapOf("username" to "user", "code" to "code"),
        null
      )
    }
  }
}
