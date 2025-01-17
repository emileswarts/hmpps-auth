package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthServiceRepository
import java.util.Optional
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

@Suppress("ClassName")
class AuthServicesServiceTest {
  private val oauthServiceRepository: OauthServiceRepository = mock()
  private val authServicesService = AuthServicesService(oauthServiceRepository)

  @Nested
  inner class list {
    @Test
    fun `list calls repository find all`() {
      val services = mutableListOf(Service(code = "", name = "", description = "", url = ""))
      whenever(oauthServiceRepository.findAllByOrderByName()).thenReturn(services)
      assertThat(authServicesService.list()).isSameAs(services)
      verify(oauthServiceRepository).findAllByOrderByName()
    }
  }

  @Nested
  inner class listEnabled {
    @Test
    fun `listEnabled calls repository find all by enabled`() {
      val services = mutableListOf(Service(code = "", name = "", description = "", url = ""))
      whenever(oauthServiceRepository.findAllByEnabledTrueOrderByName()).thenReturn(services)
      assertThat(authServicesService.listEnabled()).isSameAs(services)
      verify(oauthServiceRepository).findAllByEnabledTrueOrderByName()
    }
  }

  @Nested
  inner class `listEnabled with authorities` {
    @Test
    fun `listEnabled with authorities limits to correct roles`() {
      val authorities = listOf(SimpleGrantedAuthority("ROLE_LICENCE_DM"))
      whenever(oauthServiceRepository.findAllByEnabledTrueOrderByName()).thenReturn(ALL_SERVICES)
      assertThat(authServicesService.listEnabled(authorities)).extracting<String> { (it as Service).code }
        .containsExactly("DM", "LIC", "prison-staff-hub")
      verify(oauthServiceRepository).findAllByEnabledTrueOrderByName()
    }
  }

  @Nested
  inner class getService {
    @Test
    fun `get service finds service`() {
      val service = Service(code = "", name = "", description = "", url = "")
      whenever(oauthServiceRepository.findById(anyString())).thenReturn(Optional.of(service))
      assertThat(authServicesService.getService("code")).isSameAs(service)
      verify(oauthServiceRepository).findById("code")
    }

    @Test
    fun `get service throws exception if not found`() {
      assertThatThrownBy { authServicesService.getService("code") }
        .isInstanceOf(EntityNotFoundException::class.java).hasMessage("Entity code not found")
      verify(oauthServiceRepository).findById("code")
    }
  }
  @Nested
  inner class loadServiceDetails {
    @Test
    internal fun `load client service details`() {
      val clientServiceDetails = createClientServiceDetails()
      whenever(oauthServiceRepository.findById(anyString())).thenReturn(Optional.of(clientServiceDetails))
      val clientService = authServicesService.loadServiceDetails("client")
      val service = ServiceSummary(
        code = "client",
        name = "client service",
        description = "A service",
        authorisedRoles = "A Role",
        url = "http://localhost:3001/",
        enabled = true,
        email = "bob@Ateam",
      )
      assertThat(clientService).isEqualTo(service)
      verify(oauthServiceRepository).findById("client")
    }

    @Test
    internal fun `load client service details - no details held`() {
      val clientServiceDetails = authServicesService.loadServiceDetails("client")

      val service = ServiceSummary(code = "", name = "", description = "", authorisedRoles = "", url = "", enabled = null, email = "")
      assertThat(clientServiceDetails).isEqualTo(service)
      verify(oauthServiceRepository).findById("client")
    }
    private fun createClientServiceDetails(): Service = Service(
      code = "client",
      name = "client service",
      description = "A service",
      authorisedRoles = "A Role",
      url = "http://localhost:3001/",
      enabled = true,
      email = "bob@Ateam",
    )
  }

  @Nested
  inner class updateService {
    @Test
    fun `update calls save`() {
      val service = Service(code = "", name = "", description = "", url = "")
      authServicesService.updateService(service)
      verify(oauthServiceRepository).save<Service>(
        check {
          assertThat(it).isSameAs(service)
        }
      )
    }
  }

  @Nested
  inner class addService {
    @Test
    fun `add calls save`() {
      val service = Service(code = "", name = "", description = "", url = "")
      authServicesService.addService(service)
      verify(oauthServiceRepository).save<Service>(
        check {
          assertThat(it).isSameAs(service)
        }
      )
    }

    @Test
    fun `add checks service doesn't already exist`() {
      val service = Service(code = "newcode", name = "", description = "", url = "")
      whenever(oauthServiceRepository.findById(anyString())).thenReturn(Optional.of(Service(code = "", name = "", description = "", url = "")))
      assertThatThrownBy { authServicesService.addService(service) }
        .isInstanceOf(EntityExistsException::class.java).hasMessage("Entity newcode already exists")

      verify(oauthServiceRepository).findById("newcode")
      verifyNoMoreInteractions(oauthServiceRepository)
    }
  }

  @Nested
  inner class removeService {
    @Test
    fun `remove calls delete`() {
      authServicesService.removeService("code")
      verify(oauthServiceRepository).deleteById("code")
    }
  }

  companion object {
    private val ALL_SERVICES = mutableListOf(
      createService("DM", "ROLE_LICENCE_DM", "a@b.com"), // single role
      createService("LIC", "ROLE_LICENCE_CA,ROLE_LICENCE_DM,ROLE_LICENCE_RO", null), // multiple role
      createService("prison-staff-hub", null, "c@d.com"), // available to all roles
      createService("OTHER", "ROLE_OTHER", null), // not available
    )

    private fun createService(code: String, roles: String?, email: String?): Service =
      Service(code, "NAME", "Description", roles, "http://some.url", true, email)
  }
}
