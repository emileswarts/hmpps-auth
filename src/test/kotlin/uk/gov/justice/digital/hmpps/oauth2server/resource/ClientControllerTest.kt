@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Client
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientConfig
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientDeployment
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Hosting
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.resource.ClientsController.AuthClientDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientDetailsWithCopies
import uk.gov.justice.digital.hmpps.oauth2server.service.ClientService
import uk.gov.justice.digital.hmpps.oauth2server.service.DuplicateClientsException

class ClientControllerTest {
  private val authServiceServices: AuthServicesService = mock()
  private val clientService: ClientService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val controller =
    ClientsController(authServiceServices, clientService, telemetryClient)
  private val authentication = TestingAuthenticationToken(
    UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId"),
    "pass"
  )

  @Nested
  inner class EditFormRequest {
    @Test
    fun `show edit form new client`() {
      val modelAndView = controller.showEditForm(null)

      assertThat(modelAndView.viewName).isEqualTo("ui/form")
      assertThat(modelAndView.model["clients"] as List<*>).isEmpty()
      assertThat(modelAndView.model["clientDetails"] as ClientDetails).isNotNull
    }

    @Test
    fun `show edit form existing client`() {
      whenever(clientService.loadClientWithCopies(anyString())).thenReturn(
        ClientDetailsWithCopies(BaseClientDetails(), listOf(Client("client-1")))
      )
      whenever(clientService.loadClientDeploymentDetails(anyString())).thenReturn(
        ClientDeployment(baseClientId = "client-id")
      )
      whenever(clientService.loadClientConfig(anyString())).thenReturn(
        ClientConfig(baseClientId = "client-id", ips = listOf("127.0.0.1"))
      )
      val modelAndView = controller.showEditForm("client-id")

      assertThat(modelAndView.viewName).isEqualTo("ui/form")
      assertThat(modelAndView.model["clients"] as List<*>).extracting("id").containsOnly("client-1")
      val client = (modelAndView.model["clients"] as List<*>)[0] as Client
      assertThat(client.baseClientId).isEqualTo("client")
      assertThat(modelAndView.model["clientDetails"] as ClientDetails).isNotNull
      assertThat(modelAndView.model["deployment"] as ClientDeployment).isNotNull
      assertThat(modelAndView.model["clientConfig"] as ClientConfig).isNotNull
      assertThat(modelAndView.model["service"] as Service).isNotNull
    }
  }

  @Nested
  inner class ViewOnlyClient {
    @Test
    fun `show view only version of client`() {
      whenever(clientService.loadClientWithCopies(anyString())).thenReturn(
        ClientDetailsWithCopies(BaseClientDetails(), listOf(Client("client-1")))
      )
      whenever(clientService.loadClientDeploymentDetails(anyString())).thenReturn(
        ClientDeployment(baseClientId = "client-id")
      )
      whenever(clientService.loadClientConfig(anyString())).thenReturn(
        ClientConfig(baseClientId = "client-id", ips = listOf("127.0.0.1"))
      )
      val modelAndView = controller.showViewOnlyForm("client-id")

      assertThat(modelAndView.viewName).isEqualTo("ui/viewOnlyForm")
      assertThat(modelAndView.model["clients"] as List<*>).extracting("id").containsOnly("client-1")
      val client = (modelAndView.model["clients"] as List<*>)[0] as Client
      assertThat(client.baseClientId).isEqualTo("client")
      assertThat(modelAndView.model["clientDetails"] as ClientDetails).isNotNull
      assertThat(modelAndView.model["deployment"] as ClientDeployment).isNotNull
      assertThat(modelAndView.model["clientConfig"] as ClientConfig).isNotNull
      assertThat(modelAndView.model["service"] as Service).isNotNull
    }
  }

  @Nested
  inner class AddClient {
    @Test
    fun `add client request - add client`() {
      val authClientDetails: AuthClientDetails = createAuthClientDetails()
      val clientConfig = ClientConfig(
        "client",
        listOf("127.0.0.1"),
        validDays = 7
      )
      whenever(clientService.addClientAndConfig(authClientDetails, clientConfig)).thenReturn("bob")
      val modelAndView = controller.addClient(authentication, authClientDetails, clientConfig, "true")
      verify(clientService).addClientAndConfig(authClientDetails, clientConfig)
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsAdd",
        mapOf("username" to "user", "clientId" to "client"),
        null
      )

      assertThat(modelAndView.viewName).isEqualTo("redirect:/ui/clients/client-success")
      assertThat(modelAndView.model).containsOnly(
        entry("newClient", "true"),
        entry("clientId", "client"),
        entry("clientSecret", "bob"),
        entry("base64ClientId", "Y2xpZW50"),
        entry("base64ClientSecret", "Ym9i"),
      )
    }

    @Test
    fun `add client request - add client trailing white spare removed`() {
      val authClientDetails: AuthClientDetails = createAuthClientDetails()
      val clientConfig = ClientConfig(
        "client",
        listOf("127.0.0.1"),
        validDays = 7
      )
      authClientDetails.clientId = "client "
      whenever(clientService.addClientAndConfig(authClientDetails, clientConfig)).thenReturn("bob")
      val modelAndView = controller.addClient(authentication, authClientDetails, clientConfig, "true")
      verify(clientService).addClientAndConfig(authClientDetails, clientConfig)
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsAdd",
        mapOf("username" to "user", "clientId" to "client"),
        null
      )

      assertThat(modelAndView.viewName).isEqualTo("redirect:/ui/clients/client-success")
      assertThat(modelAndView.model).containsOnly(
        entry("newClient", "true"),
        entry("clientId", "client"),
        entry("clientSecret", "bob"),
        entry("base64ClientId", "Y2xpZW50"),
        entry("base64ClientSecret", "Ym9i"),
      )
    }

    private fun createAuthClientDetails(): AuthClientDetails {
      val authClientDetails = AuthClientDetails()
      authClientDetails.clientId = "client"
      authClientDetails.setAuthorizedGrantTypes(listOf("client_credentials"))
      authClientDetails.authorities = mutableListOf(GrantedAuthority { "ROLE_CLIENT" })
      authClientDetails.clientSecret = ""
      return authClientDetails
    }
  }

  @Nested
  inner class EditClient {

    @Test
    fun `edit client request - update existing client`() {
      val authClientDetails: AuthClientDetails = createAuthClientDetails()
      val clientConfig = ClientConfig(
        "client", listOf("127.0.0.1"),
        validDays = 7
      )
      val modelAndView = controller.editClient(authentication, authClientDetails, clientConfig, null)
      verify(clientService).updateClientAndConfig(authClientDetails, clientConfig)
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsUpdate",
        mapOf("username" to "user", "clientId" to "client"),
        null
      )
      assertThat(modelAndView.viewName).isEqualTo("redirect:/ui")
    }

    @Test
    fun `edit client request - update existing client remove end date`() {
      val authClientDetails: AuthClientDetails = createAuthClientDetails()
      val clientConfig = ClientConfig(
        "client", listOf("127.0.0.1"),
        validDays = 7
      )
      val modelAndView = controller.editClient(authentication, authClientDetails, clientConfig, null)
      verify(clientService).updateClientAndConfig(authClientDetails, clientConfig)
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsUpdate",
        mapOf("username" to "user", "clientId" to "client"),
        null
      )
      assertThat(modelAndView.viewName).isEqualTo("redirect:/ui")
    }

    @Test
    fun `edit client request - update client throws NoSuchClientException`() {
      val authClientDetails: AuthClientDetails = createAuthClientDetails()
      val clientConfig = ClientConfig(
        "client", listOf("127.0.0.1"),
        validDays = 7
      )
      val exception = NoSuchClientException("No client found with id = ")
      doThrow(exception).whenever(clientService).updateClientAndConfig(authClientDetails, clientConfig)

      assertThatThrownBy { controller.editClient(authentication, authClientDetails, clientConfig, null) }.isEqualTo(
        exception
      )

      verifyNoInteractions(telemetryClient)
    }

    private fun createAuthClientDetails(): AuthClientDetails {
      val authClientDetails = AuthClientDetails()
      authClientDetails.clientId = "client"
      authClientDetails.setAuthorizedGrantTypes(listOf("client_credentials"))
      authClientDetails.authorities = mutableListOf(GrantedAuthority { "ROLE_CLIENT" })
      authClientDetails.clientSecret = ""
      return authClientDetails
    }
  }

  @Nested
  inner class DuplicateClientRequest {

    @Test
    fun `Duplicate client`() {
      whenever(clientService.duplicateClient(anyString())).thenReturn(createAuthClientDetails())
      val mandv = controller.duplicateClient(authentication, "client")
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsDuplicated",
        mapOf("username" to "user", "clientId" to "client-1"),
        null
      )
      assertThat(mandv.viewName).isEqualTo("redirect:/ui/clients/duplicate-client-success")
      assertThat(mandv.model).containsOnly(
        entry("clientId", "client-1"),
        entry("clientSecret", "Some-Secret"),
        entry("base64ClientId", "Y2xpZW50LTE="),
        entry("base64ClientSecret", "U29tZS1TZWNyZXQ="),
      )
    }

    @Test
    fun `Duplicate client throw exception max duplicated`() {
      doThrow(
        DuplicateClientsException(
          "client",
          "Duplicate clientId failed for some-client with reason: MaxReached"
        )
      ).whenever(clientService).duplicateClient(anyString())

      val mandv = controller.duplicateClient(authentication, "client")

      verifyNoInteractions(telemetryClient)
      assertThat(mandv.viewName).isEqualTo("redirect:/ui/clients/form")
      assertThat(mandv.model).containsOnly(
        entry("client", "client"),
        entry("error", "maxDuplicates"),
      )
    }

    private fun createAuthClientDetails(): AuthClientDetails {
      val authClientDetails = AuthClientDetails()
      authClientDetails.clientId = "client-1"
      authClientDetails.setAuthorizedGrantTypes(listOf("client_credentials"))
      authClientDetails.authorities = mutableListOf(GrantedAuthority { "ROLE_CLIENT" })
      authClientDetails.clientSecret = "Some-Secret"
      return authClientDetails
    }
  }

  @Nested
  inner class DeleteClientRequest {

    @Test
    fun `delete Client Request view`() {
      val view = controller.deleteClient(authentication, "client")
      verify(clientService).removeClient("client")
      verify(telemetryClient).trackEvent(
        "AuthClientDetailsDeleted",
        mapOf("username" to "user", "clientId" to "client"),
        null
      )
      assertThat(view).isEqualTo("redirect:/ui")
    }

    @Test
    fun `delete Client Request - delete client throws NoSuchClientException`() {

      val exception = NoSuchClientException("No client found with id = ")
      doThrow(exception).whenever(clientService).removeClient(anyString())

      assertThatThrownBy { controller.deleteClient(authentication, "client") }.isEqualTo(exception)

      verifyNoInteractions(telemetryClient)
    }
  }

  @Nested
  inner class AuthClientDetailsTest {
    @Test
    fun `set mfa`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.mfa = MfaAccess.all
      assertThat(authClientDetails.additionalInformation).containsExactlyEntriesOf(mapOf("mfa" to MfaAccess.all))
      assertThat(authClientDetails.mfa).isEqualTo(MfaAccess.all)
    }

    @Test
    fun `get mfa`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.addAdditionalInformation("mfa", MfaAccess.untrusted)
      assertThat(authClientDetails.mfa).isEqualTo(MfaAccess.untrusted)
    }

    @Test
    fun `get mfa not set`() {
      val authClientDetails = AuthClientDetails()
      assertThat(authClientDetails.mfa).isNull()
    }

    @Test
    fun `set mfa remember me`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.mfaRememberMe = true
      assertThat(authClientDetails.additionalInformation).containsExactlyEntriesOf(mapOf("mfaRememberMe" to true))
      assertThat(authClientDetails.mfaRememberMe).isEqualTo(true)
    }

    @Test
    fun `get mfa remember me`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.addAdditionalInformation("mfaRememberMe", true)
      assertThat(authClientDetails.mfaRememberMe).isEqualTo(true)
    }

    @Test
    fun `get mfa remember me not set`() {
      val authClientDetails = AuthClientDetails()
      assertThat(authClientDetails.mfaRememberMe).isNull()
    }

    @Test
    fun `set Jira`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.jiraNo = "DT-2264"
      assertThat(authClientDetails.additionalInformation).containsExactlyEntriesOf(mapOf("jiraNo" to "DT-2264"))
      assertThat(authClientDetails.jiraNo).isEqualTo("DT-2264")
    }

    @Test
    fun `get Jira`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.addAdditionalInformation("jiraNo", "DT-2264")
      assertThat(authClientDetails.jiraNo).isEqualTo("DT-2264")
    }

    @Test
    fun `get Jira not set`() {
      val authClientDetails = AuthClientDetails()
      assertThat(authClientDetails.jiraNo).isNull()
    }

    @Test
    fun `setAuthorities adds ROLE_ prefix`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.authorities = listOf("joe", "ROLE_fred", "  harry")
        .map { SimpleGrantedAuthority(it) }
      assertThat(authClientDetails.authorities.map { it.authority })
        .containsExactlyInAnyOrder("ROLE_JOE", "ROLE_FRED", "ROLE_HARRY")
    }

    @Test
    fun `getAuthoritiesWithNewlines removes ROLE_ prefix and converts to newlines`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.authorities = listOf("joe", "ROLE_fred", "  harry")
        .map { SimpleGrantedAuthority(it) }
      assertThat(authClientDetails.authoritiesWithNewlines).isEqualTo("JOE\nFRED\nHARRY")
    }

    @Test
    fun `setAuthoritiesWithNewlines adds ROLE_ prefix, removes newlines and separates roles`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.authoritiesWithNewlines = "joe ROLE_fred\n\n  harry "
      assertThat(authClientDetails.authoritiesWithNewlines).isEqualTo("JOE\nFRED\nHARRY")
      assertThat(authClientDetails.authorities).isEqualTo(
        listOf(
          SimpleGrantedAuthority("ROLE_JOE"),
          SimpleGrantedAuthority("ROLE_FRED"),
          SimpleGrantedAuthority("ROLE_HARRY")
        )
      )
    }

    @Test
    fun `registeredRedirectUriWithNewlines converts to Strings for null`() {
      assertThat(AuthClientDetails().registeredRedirectUriWithNewlines).isNull()
    }

    @Test
    fun `registeredRedirectUriWithNewlines converts from String for null`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.registeredRedirectUriWithNewlines = null
      assertThat(authClientDetails.registeredRedirectUri).isNull()
    }

    @Test
    fun `registeredRedirectUriWithNewlines converts to and from String`() {
      val authClientDetails = AuthClientDetails()
      authClientDetails.registeredRedirectUriWithNewlines = " http://some_url \n http://and_naother http://third"
      assertThat(authClientDetails.registeredRedirectUri).containsExactlyInAnyOrder(
        "http://some_url", "http://and_naother", "http://third"
      )
    }
  }

  @Nested
  inner class ClientDeploymentFormRequest {
    @Test
    fun `show add client deployment details form`() {
      whenever(clientService.getClientDeploymentDetailsAndBaseClientId(anyString())).thenReturn(
        Pair(null, "client-id")
      )
      val modelAndView = controller.showDeploymentForm("client-id-1")

      assertThat(modelAndView.viewName).isEqualTo("ui/deploymentForm")
      assertThat(modelAndView.model["baseClientId"]).isEqualTo("client-id")
      assertThat(modelAndView.model["clientDeployment"] as ClientDeployment).isNotNull
    }

    @Test
    fun `show edit client deployment details form`() {
      whenever(clientService.getClientDeploymentDetailsAndBaseClientId(anyString())).thenReturn(
        Pair(ClientDeployment(baseClientId = "client-id"), "client-id")
      )
      val modelAndView = controller.showDeploymentForm("client-id-1")

      assertThat(modelAndView.viewName).isEqualTo("ui/deploymentForm")
      assertThat(modelAndView.model["baseClientId"]).isEqualTo("client-id")
      assertThat(modelAndView.model["clientDeployment"] as ClientDeployment).isNotNull
    }
  }

  @Nested
  inner class EditClientDeployment {
    @Test
    fun `edit client deployment request - update existing client`() {
      val clientDeployment: ClientDeployment = createClientDeploymentDetails()
      val modelAndView = controller.addClientDeploymentDetails(authentication, clientDeployment)
      verify(clientService).saveClientDeploymentDetails(clientDeployment)
      verify(telemetryClient).trackEvent(
        "AuthClientDeploymentDetailsUpdated",
        mapOf("username" to "user", "baseClientId" to "client"),
        null
      )
      assertThat(modelAndView.viewName).isEqualTo("redirect:/ui/clients/form")
      assertThat(modelAndView.model["client"]).isEqualTo("client")
    }

    private fun createClientDeploymentDetails(): ClientDeployment = ClientDeployment(
      baseClientId = "client",
      type = ClientType.SERVICE,
      team = "A-Team",
      teamContact = "bob@Ateam",
      teamSlack = "slack",
      hosting = Hosting.CLOUDPLATFORM,
      namespace = "namespace",
      deployment = "deployment",
      secretName = "secret-name",
      clientIdKey = "client-id-key",
      secretKey = "secret-key",
    )
  }
}
