package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService

class AuthServicesControllerTest {
  private val authServicesService: AuthServicesService = mock()
  private val authentication: Authentication = mock()
  private val controller = AuthServicesController(authServicesService)

  private val services = listOf(
    Service("code1", "name1", "description1", "roles1", "url1", true, "email1"),
    Service("code2", "name2", "description2", "roles2", "url2", true, "email2")
  )

  @Test
  fun `test services returns all  services`() {
    whenever(authServicesService.listEnabled()).thenReturn(services)

    val response = controller.services()

    assertThat(response).containsExactly(AuthService(services[0]), AuthService(services[1]))
  }

  @Test
  fun `test services returns all my services`() {
    whenever(authServicesService.listEnabled(any())).thenReturn(services)
    val authorities = listOf(SimpleGrantedAuthority("role"))
    whenever(authentication.authorities).thenReturn(authorities)

    val response = controller.myServices(authentication)

    assertThat(response).containsExactly(AuthService(services[0]), AuthService(services[1]))
    verify(authServicesService).listEnabled(authorities)
  }

  @Nested
  inner class ServiceByCode {
    private val serviceCode = "test"

    @Test
    fun `should respond with service when found`() {
      whenever(authServicesService.findService(serviceCode)).thenReturn(services[0])

      val response = controller.serviceByCode(serviceCode)

      assertThat(response).isEqualTo(AuthService(services[0]))
    }

    @Test
    fun `should throw exception when service not found`() {
      whenever(authServicesService.findService(serviceCode)).thenReturn(null)

      assertThatThrownBy {
        controller.serviceByCode(serviceCode)
      }.isInstanceOf(ServiceNotFoundException::class.java)
    }
  }
}
