package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper
import uk.gov.justice.digital.hmpps.oauth2server.azure.AzureUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService
import uk.gov.justice.digital.hmpps.oauth2server.service.UserContextService

class HomeControllerTest {
  private val authServicesService: AuthServicesService = mock()
  private val userContextService: UserContextService = mock()
  private val homeController = HomeController(authServicesService, userContextService, false)
  private val linkAccountsEnabledHomeController = HomeController(authServicesService, userContextService, true)
  private val authentication: Authentication = mock()

  @Test
  fun `home view`() {
    setUpAuthentication()
    val modelAndView = homeController.home(authentication)
    assertThat(modelAndView.viewName).isEqualTo("landing")
  }

  @Test
  fun `terms view`() {
    setUpAuthentication()
    val modelAndView = homeController.terms()
    assertThat(modelAndView).isEqualTo("terms")
  }

  @Test
  fun `home model`() {
    whenever(authServicesService.listEnabled(any())).thenReturn(ALL_SERVICES)
    setUpAuthentication(AuthSource.nomis, "ROLE_LICENCE_DM")
    val modelAndView = homeController.home(authentication)
    assertThat(modelAndView.model["services"]).isSameAs(ALL_SERVICES)
  }

  @Test
  fun `home azuread user`() {
    whenever(authServicesService.listEnabled(any())).thenReturn(ALL_SERVICES)
    setUpAuthentication(AuthSource.azuread, "ROLE_BOB")
    val authorities = setOf(Authority("ROLE_OTHER", "Role Other"))
    whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(
      listOf(
        DeliusUserPersonDetails(
          username = "user",
          userId = "12345",
          firstName = "F",
          surname = "L",
          email = "a@b.com",
          roles = setOf(SimpleGrantedAuthority("ROLE_LICENCE_DM"))
        ),
        UserHelper.createSampleUser(authorities = authorities)
      )
    )
    val modelAndView = homeController.home(authentication)
    assertThat(modelAndView.model["services"]).isSameAs(ALL_SERVICES)
    verify(authServicesService).listEnabled(
      check { ga ->
        assertThat(ga.map { it.authority }).containsExactly("ROLE_LICENCE_DM", "ROLE_PROBATION", "ROLE_OTHER")
      }
    )
  }

  @Test
  fun `home link accounts enabled`() {
    whenever(authServicesService.listEnabled(any())).thenReturn(ALL_SERVICES)
    setUpAuthentication(AuthSource.nomis, "ROLE_BOB")
    val authorities = setOf(Authority("ROLE_OTHER", "Role Other"))
    whenever(userContextService.discoverUsers(any(), any(), any())).thenReturn(
      listOf(
        DeliusUserPersonDetails(
          username = "user",
          userId = "12345",
          firstName = "F",
          surname = "L",
          email = "a@b.com",
          roles = setOf(SimpleGrantedAuthority("ROLE_LICENCE_DM"))
        ),
        UserHelper.createSampleUser(authorities = authorities)
      )
    )
    val modelAndView = linkAccountsEnabledHomeController.home(authentication)
    assertThat(modelAndView.model["services"]).isSameAs(ALL_SERVICES)
    verify(authServicesService).listEnabled(
      check { ga ->
        assertThat(ga.map { it.authority }).containsExactly("ROLE_LICENCE_DM", "ROLE_PROBATION", "ROLE_OTHER")
      }
    )
  }

  private fun setUpAuthentication(authSource: AuthSource = AuthSource.auth, vararg roles: String = arrayOf()) {
    val authorities = roles.map { SimpleGrantedAuthority(it) }.toList()
    whenever(authentication.authorities).thenReturn(authorities)
    whenever(authentication.principal).thenReturn(
      UserDetailsImpl("username", "name", authorities, authSource.source, "userId", "jwtId")
    )
  }

  private fun getAzureUserPersonDetails() = AzureUserPersonDetails(
    ArrayList(),
    true,
    "D6165AD0-AED3-4146-9EF7-222876B57549",
    "Joe",
    "Bloggs",
    "joe.bloggs@justice.gov.uk",
    true,
    accountNonExpired = true,
    accountNonLocked = true
  )

  companion object {
    private val ALL_SERVICES = listOf(
      createService("DM", "ROLE_LICENCE_DM", "a@b.com"), // single role
      createService("LIC", "ROLE_LICENCE_CA,ROLE_LICENCE_DM,ROLE_LICENCE_RO", null), // multiple role
      createService("prison-staff-hub", null, "c@d.com"), // available to all roles
      createService("OTHER", "ROLE_OTHER", null), // not available
    )

    private fun createService(code: String, roles: String?, email: String?): Service =
      Service(code, "NAME", "Description", roles, "http://some.url", true, email)
  }
}
