@file:Suppress("ClassName", "DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationService
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Client
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientConfig
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientDeployment
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientType.PERSONAL
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientType.SERVICE
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Hosting
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientConfigRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientDeploymentRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthServiceRepository
import uk.gov.justice.digital.hmpps.oauth2server.resource.ClientsController.AuthClientDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.PasswordGenerator
import uk.gov.justice.digital.hmpps.oauth2server.service.SortBy.count
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional

internal class ClientServiceTest {
  private val clientRepository: ClientRepository = mock()
  private val clientDeploymentRepository: ClientDeploymentRepository = mock()
  private val oauthServiceRepository: OauthServiceRepository = mock()
  private val clientDetailsService: ClientDetailsService = mock()
  private val clientRegistrationService: ClientRegistrationService = mock()
  private val clientConfigRepository: ClientConfigRepository = mock()
  private val passwordGenerator: PasswordGenerator = mock()
  private val clientService = ClientService(
    clientDetailsService,
    clientRegistrationService,
    passwordGenerator,
    clientRepository,
    clientDeploymentRepository,
    clientConfigRepository,
    oauthServiceRepository,
  )

  @Nested
  inner class addClient {
    @Test
    internal fun `add client`() {
      whenever(passwordGenerator.generatePassword()).thenReturn("Some-Secret")
      val authClientDetails = createAuthClientDetails()
      clientService.addClient(authClientDetails)
      verify(clientRegistrationService).addClientDetails(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo((authClientDetails))
        }
      )
    }

    @Test
    internal fun `add client throws ClientAlreadyExistsException`() {
      val authClientDetails = createAuthClientDetails()

      val exception = ClientAlreadyExistsException("Client already exists: ")
      doThrow(exception).whenever(clientRegistrationService).addClientDetails(authClientDetails)

      assertThatThrownBy { clientService.addClient(authClientDetails) }.isEqualTo(exception)
    }
  }

  @Nested
  inner class findAndUpdateDuplicates {
    @Test
    internal fun `no replacement`() {
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(createAuthClientDetails())
      clientService.findAndUpdateDuplicates("some-client")
      verify(clientRepository).findByIdStartsWithOrderById("some-client")
      verify(clientRegistrationService, never()).updateClientDetails(any())
    }

    @Test
    internal fun duplicate() {
      val authClientDetails = createAuthClientDetails()
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(authClientDetails)
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("some-client-24"),
          Client("some-client-25")
        )
      )
      clientService.findAndUpdateDuplicates("some-client-24")
      verify(clientRepository).findByIdStartsWithOrderById("some-client")
      verify(clientRegistrationService).updateClientDetails(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo(createBaseClientDetails(authClientDetails))
        }
      )
    }

    @Test
    internal fun `other-client-with-numbers`() {
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(createAuthClientDetails())
      clientService.findAndUpdateDuplicates("some-client-24-id")
      verify(clientRepository).findByIdStartsWithOrderById("some-client-24-id")
    }
  }

  @Nested
  inner class loadClientWithCopies {
    @Test
    internal fun `returns all clients`() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("some-client-24"),
          Client("some-client-25")
        )
      )
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(BaseClientDetails())
      val client = clientService.loadClientWithCopies("some-client-24")
      assertThat(client.duplicates).extracting("id").containsOnly("some-client-24", "some-client-25")
      verify(clientRepository).findByIdStartsWithOrderById("some-client")
      verify(clientDetailsService).loadClientByClientId("some-client-24")
    }

    @Test
    internal fun `hides incorrect duplicates`() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("hub-24"),
          Client("hub-12345"),
          Client("hub-ui"),
          Client("hub-ui-2")
        )
      )
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(BaseClientDetails())
      val client = clientService.loadClientWithCopies("hub-1")
      assertThat(client.duplicates).extracting("id").containsOnly("hub-24", "hub-12345")
    }
  }

  @Nested
  inner class loadClientWithCopiesAndDeployment {
    @Test
    internal fun `returns all client ids and deployment details`() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("client"),
          Client("client-1"),
          Client("client-2")
        )
      )

      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(createAuthClientDetails())
      val clientDeploymentDetails = createClientDeploymentDetails()
      whenever(clientDeploymentRepository.findById(anyString())).thenReturn(Optional.of(clientDeploymentDetails))

      val client = clientService.loadClientAndDeployment("client")

      assertThat(client.requestedClientId).isEqualTo("client")
      assertThat(client.duplicates).containsOnly("client", "client-1", "client-2")
      assertThat(client.clientDeployment).isEqualTo(clientDeploymentDetails)
      verify(clientRepository).findByIdStartsWithOrderById("client")
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `returns all clients ids no deployment details held`() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("client"),
          Client("client-1"),
          Client("client-2")
        )
      )
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(createAuthClientDetails())

      val client = clientService.loadClientAndDeployment("client")

      assertThat(client.requestedClientId).isEqualTo("client")
      assertThat(client.duplicates).containsOnly("client", "client-1", "client-2")
      assertThat(client.clientDeployment).isNull()
      verify(clientRepository).findByIdStartsWithOrderById("client")
      verify(clientDeploymentRepository).findById("client")
    }
  }

  @Nested
  inner class duplicateClient {
    @Test
    internal fun `duplicate original client`() {
      val authClientDetails = createAuthClientDetails()
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(authClientDetails)
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(listOf(Client("some-client")))
      whenever(passwordGenerator.generatePassword()).thenReturn("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")

      clientService.duplicateClient("some-client")

      verify(clientRegistrationService).addClientDetails(
        check {
          assertThat(it).usingRecursiveComparison().ignoringFields("clientId", "clientSecret")
            .isEqualTo(createBaseClientDetails(authClientDetails))
          assertThat(it.clientId).isEqualTo("some-client-1")
          assertThat(it.clientSecret).isEqualTo("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")
        }
      )
    }

    @Test
    internal fun `duplicate client incrementing number correctly`() {
      val authClientDetails = createAuthClientDetails()
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(authClientDetails)
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("some-client"),
          Client("some-client-1")
        )
      )
      whenever(passwordGenerator.generatePassword()).thenReturn("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")

      clientService.duplicateClient("some-client-1")

      verify(clientRegistrationService).addClientDetails(
        check {
          assertThat(it).usingRecursiveComparison().ignoringFields("clientId", "clientSecret")
            .isEqualTo(createBaseClientDetails(authClientDetails))
          assertThat(it.clientId).isEqualTo("some-client-2")
          assertThat(it.clientSecret).isEqualTo("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")
        }
      )
    }

    @Test
    internal fun `duplicate client incrementing number correctly when original client duplicated`() {
      val authClientDetails = createAuthClientDetails()
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(authClientDetails)
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("some-client"),
          Client("some-client-4")
        )
      )
      whenever(passwordGenerator.generatePassword()).thenReturn("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")

      clientService.duplicateClient("some-client")

      verify(clientRegistrationService).addClientDetails(
        check {
          assertThat(it).usingRecursiveComparison().ignoringFields("clientId", "clientSecret")
            .isEqualTo(createBaseClientDetails(authClientDetails))
          assertThat(it.clientId).isEqualTo("some-client-5")
          assertThat(it.clientSecret).isEqualTo("O)Xbqg6F–Q7211cj&jUL)oC=E;s9^pFZ:3$")
        }
      )
    }

    @Test
    internal fun `will throw error if 3 clients already exist for base client id`() {
      val authClientDetails = createAuthClientDetails()
      whenever(clientDetailsService.loadClientByClientId(any())).thenReturn(authClientDetails)
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("some-client-3"),
          Client("some-client-4"),
          Client("some-client-5")
        )
      )

      assertThatThrownBy { clientService.duplicateClient("some-client") }
        .isInstanceOf(DuplicateClientsException::class.java)
        .hasMessage("Duplicate clientId failed for baseClientId: some-client with reason: MaxReached")
    }
  }

  @Nested
  inner class listUniqueClients {
    @Test
    internal fun `calculates roles and grants`() {
      val lastAccessed = LocalDateTime.parse("2020-02-03T11:23")
      val secretUpdated = LocalDateTime.parse("2020-03-04T12:24")
      val aClient = Client(
        "a-client",
        authorities = listOf("ROLE_fred", "bob"),
        authorizedGrantTypes = listOf("client_credentials", "password"),
        lastAccessed = lastAccessed,
        secretUpdated = secretUpdated,
      )
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient))
      val clients = clientService.listUniqueClients(count, ClientFilter())
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
      assertThat(clients).containsExactly(
        ClientSummary(
          baseClientId = "a-client",
          grantTypes = "client_credentials\npassword",
          roles = "bob\nfred",
          count = 1,
          clientType = null,
          service = null,
          teamName = null,
          lastAccessed = lastAccessed,
          lastAccessedTime = lastAccessed.toEpochSecond(ZoneOffset.UTC),
          secretUpdated = secretUpdated,
          secretUpdatedTime = secretUpdated.toEpochSecond(ZoneOffset.UTC),
        )
      )
    }

    @Test
    internal fun `calculates largest last accessed and secret updated`() {
      val lastAccessedLatest = LocalDateTime.parse("2020-02-03T11:23")
      val secretUpdatedLatest = LocalDateTime.parse("2020-03-04T12:24")
      val aClient = Client(
        "a-client",
        lastAccessed = lastAccessedLatest,
        secretUpdated = LocalDateTime.parse("2019-02-05T11:23"),
      )
      val aClient2 = Client(
        "a-client-2",
        lastAccessed = LocalDateTime.parse("2019-02-05T11:23"),
        secretUpdated = secretUpdatedLatest,
      )
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient, aClient2))
      val clients = clientService.listUniqueClients(count, ClientFilter())
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
      assertThat(clients).containsExactly(
        ClientSummary(
          baseClientId = "a-client",
          grantTypes = "",
          roles = "",
          count = 2,
          clientType = null,
          service = null,
          teamName = null,
          lastAccessed = lastAccessedLatest,
          lastAccessedTime = lastAccessedLatest.toEpochSecond(ZoneOffset.UTC),
          secretUpdated = secretUpdatedLatest,
          secretUpdatedTime = secretUpdatedLatest.toEpochSecond(ZoneOffset.UTC),
        )
      )
    }

    @Test
    internal fun `filters out duplicates of a client`() {
      val aClient = Client("a-client")
      val duplicateClient = Client("duplicate")
      whenever(clientRepository.findAll()).thenReturn(
        listOf(
          aClient, Client("duplicate-2"), duplicateClient, Client("duplicate-59")
        )
      )
      val clients = clientService.listUniqueClients(count, ClientFilter())
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client", "duplicate")
      assertThat(clients.filter { it.baseClientId == "duplicate" }.map { it.count }).containsOnly(3)
    }

    @Test
    internal fun `retrieves deployment information too`() {
      val lastAccessed = LocalDateTime.parse("2020-02-03T11:23")
      val secretUpdated = LocalDateTime.parse("2020-03-04T12:24")
      val aClient = Client(
        "a-client",
        lastAccessed = lastAccessed,
        secretUpdated = secretUpdated,
      )
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient))
      whenever(clientDeploymentRepository.findAll()).thenReturn(
        listOf(ClientDeployment("a-client", type = PERSONAL, team = "name"), ClientDeployment("other"))
      )
      val clients = clientService.listUniqueClients(count, ClientFilter())
      assertThat(clients).hasSize(1)
      assertThat(clients).containsExactly(
        ClientSummary(
          baseClientId = "a-client",
          grantTypes = "",
          roles = "",
          count = 1,
          clientType = PERSONAL,
          service = "Personal",
          teamName = "name",
          lastAccessed = lastAccessed,
          lastAccessedTime = lastAccessed.toEpochSecond(ZoneOffset.UTC),
          secretUpdated = secretUpdated,
          secretUpdatedTime = secretUpdated.toEpochSecond(ZoneOffset.UTC),
        )
      )
    }

    @Test
    internal fun `filter by role`() {
      val aClient = Client("a-client", authorities = listOf("BOB", "FRED"))
      val aClient2 = Client("a-second-client")
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient, aClient2))
      val clients = clientService.listUniqueClients(count, ClientFilter(role = "bob"))
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
    }

    @Test
    internal fun `filter by grant type`() {
      val aClient = Client("a-client", authorizedGrantTypes = listOf("client", "pass"))
      val aClient2 = Client("a-second-client")
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient, aClient2))
      val clients = clientService.listUniqueClients(count, ClientFilter(grantType = "pass"))
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
    }

    @Test
    internal fun `filter by client type`() {
      val aClient = Client("a-client")
      val aClient2 = Client("a-second-client")
      whenever(clientDeploymentRepository.findAll()).thenReturn(
        listOf(ClientDeployment("a-client", type = SERVICE, team = "name"), ClientDeployment("other"))
      )
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient, aClient2))
      val clients = clientService.listUniqueClients(count, ClientFilter(clientType = SERVICE))
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
    }

    @Test
    internal fun `filter by all`() {
      val aClient =
        Client("a-client", authorizedGrantTypes = listOf("client", "pass"), authorities = listOf("BOB", "FRED"))
      val aClient2 = Client("a-second-client")
      whenever(clientDeploymentRepository.findAll()).thenReturn(
        listOf(ClientDeployment("a-client", type = SERVICE, team = "name"), ClientDeployment("other"))
      )
      whenever(clientRepository.findAll()).thenReturn(listOf(aClient, aClient2))
      val clients =
        clientService.listUniqueClients(count, ClientFilter(clientType = SERVICE, role = "bob", grantType = "pass"))
      assertThat(clients.map { it.baseClientId }).containsOnly("a-client")
    }

    @Test
    internal fun `use service name if set`() {
      whenever(clientRepository.findAll()).thenReturn(listOf(Client("a-second-client")))
      whenever(oauthServiceRepository.findAll()).thenReturn(
        listOf(
          Service(
            code = "a-second-client",
            name = "Service Name",
            description = "",
            url = ""
          )
        )
      )
      val clients = clientService.listUniqueClients(count, null)
      assertThat(clients.map { it.service }).containsExactly("Service Name")
    }

    @Test
    internal fun `combine roles`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(
          Client(
            "a-second-client-2",
            authorities = listOf("ROLE_BOB")
          )
        )
      )
      whenever(oauthServiceRepository.findAll()).thenReturn(
        listOf(
          Service(
            code = "a-second-client",
            name = "Service Name",
            description = "",
            url = "",
            authorisedRoles = "ROLE_JOE"
          )
        )
      )
      val clients = clientService.listUniqueClients(count, null)
      assertThat(clients.map { it.roles }).containsExactly("BOB\nService roles:\nJOE")
    }

    @Test
    internal fun `combine roles no authorities`() {
      whenever(clientRepository.findAll()).thenReturn(listOf(Client("a-second-client-2")))
      whenever(oauthServiceRepository.findAll()).thenReturn(
        listOf(
          Service(
            code = "a-second-client",
            name = "Service Name",
            description = "",
            url = "",
            authorisedRoles = "ROLE_JOE"
          )
        )
      )
      val clients = clientService.listUniqueClients(count, null)
      assertThat(clients.map { it.roles }).containsExactly("Service roles:\nJOE")
    }
  }

  @Nested
  inner class clientDeployment {

    @Test
    internal fun `load client deployment details`() {
      val clientDeploymentDetails = createClientDeploymentDetails()
      whenever(clientDeploymentRepository.findById(anyString())).thenReturn(Optional.of(clientDeploymentDetails))
      val clientDeployment = clientService.loadClientDeploymentDetails("client-1")

      assertThat(clientDeployment).isEqualTo(clientDeploymentDetails)
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `load client deployment details - baseClientId`() {
      val clientDeploymentDetails = createClientDeploymentDetails()
      whenever(clientDeploymentRepository.findById(anyString())).thenReturn(Optional.of(clientDeploymentDetails))
      val clientDeployment = clientService.loadClientDeploymentDetails("client")

      assertThat(clientDeployment).isEqualTo(clientDeploymentDetails)
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `load client deployment details - no details held`() {
      val clientDeployment = clientService.loadClientDeploymentDetails("client")

      assertThat(clientDeployment).isNull()
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `get client deployment details and baseClientId`() {
      val clientDeploymentDetails = createClientDeploymentDetails()

      whenever(clientDeploymentRepository.findById(anyString())).thenReturn(Optional.of(clientDeploymentDetails))
      val (clientDeployment, baseClientId) = clientService.getClientDeploymentDetailsAndBaseClientId("client-1")

      assertThat(clientDeployment).isEqualTo(clientDeploymentDetails)
      assertThat(baseClientId).isEqualTo("client")
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `load client deployment details and baseClientId- no details held`() {
      val clientDeployment = clientService.getClientDeploymentDetailsAndBaseClientId("client")

      assertThat(clientDeployment).isEqualTo(Pair(null, "client"))
      verify(clientDeploymentRepository).findById("client")
    }

    @Test
    internal fun `save client deployment details`() {
      val clientDeploymentDetails = createClientDeploymentDetails()
      clientService.saveClientDeploymentDetails(clientDeploymentDetails)

      verify(clientDeploymentRepository).save(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo((clientDeploymentDetails))
        }
      )
    }
  }

  @Nested
  inner class clientConfig {

    @Test
    internal fun `load client config details`() {
      val clientConfig = createClientConfig()
      whenever(clientConfigRepository.findById(anyString())).thenReturn(Optional.of(clientConfig))
      val clientConfigFromDB = clientService.loadClientConfig("client-1")

      assertThat(clientConfigFromDB).isEqualTo(clientConfig)
      verify(clientConfigRepository).findById("client")
    }

    @Test
    internal fun `load client config details - baseClientId`() {
      val clientConfig = createClientConfig()
      whenever(clientConfigRepository.findById(anyString())).thenReturn(Optional.of(clientConfig))
      val clientConfigFromDB = clientService.loadClientConfig("client")

      assertThat(clientConfigFromDB).isEqualTo(clientConfig)
      verify(clientConfigRepository).findById("client")
    }

    @Test
    internal fun `load client config details - no details held`() {
      val clientConfigFromDB = clientService.loadClientConfig("client")

      assertThat(clientConfigFromDB).isNull()
      verify(clientConfigRepository).findById("client")
    }

    @Test
    internal fun `load client config with new line details - no details held`() {
      val clientConfigFromDB = clientService.loadClientConfig("client")

      assertThat(clientConfigFromDB?.allowedIpsWithNewlines).isNull()
      verify(clientConfigRepository).findById("client")
    }

    @Test
    internal fun `save client config details - add client`() {
      val client = createAuthClientDetails()
      val clientConfig = ClientConfig(
        baseClientId = "client",
        ips = listOf("127.0.0.1"),
        validDays = 7,
        allowExpire = true
      )
      clientService.addClientAndConfig(client, clientConfig)

      verify(clientConfigRepository).save(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo(
            ClientConfig(
              baseClientId = "client",
              ips = listOf("127.0.0.1"),
              clientEndDate = LocalDate.now().plusDays(6),
              allowExpire = true,
              validDays = 7,
            )
          )
        }
      )
    }

    @Test
    internal fun `save client config details - update client`() {
      val client = createAuthClientDetails()
      val clientConfig = ClientConfig(
        baseClientId = "client",
        ips = listOf("127.0.0.1"),
        validDays = 7,
        allowExpire = true
      )
      clientService.updateClientAndConfig(client, clientConfig)

      verify(clientConfigRepository).save(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo(
            ClientConfig(
              baseClientId = "client",
              ips = listOf("127.0.0.1"),
              clientEndDate = LocalDate.now().plusDays(6),
              allowExpire = true,
              validDays = 7,
            )
          )
        }
      )
    }

    @Test
    internal fun `save client config details - update client allowed to expire false removes validDays`() {
      val client = createAuthClientDetails()
      val clientConfig = ClientConfig(
        baseClientId = "client",
        ips = listOf("127.0.0.1"),
        validDays = 7,
        allowExpire = false
      )
      clientService.updateClientAndConfig(client, clientConfig)

      verify(clientConfigRepository).save(
        check {
          assertThat(it).usingRecursiveComparison().isEqualTo(
            ClientConfig(
              baseClientId = "client",
              ips = listOf("127.0.0.1"),
              clientEndDate = null,
              allowExpire = false,
              validDays = null,
            )
          )
        }
      )
    }
  }

  @Nested
  inner class removeClientDeploymentAndAllowedIps {

    @Test
    internal fun `remove client not duplicates and deployment`() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("client"),
        )
      )

      clientService.removeClient("client")

      verify(clientDeploymentRepository).deleteByBaseClientId("client")
      verify(clientConfigRepository).deleteByBaseClientId("client")
      verify(clientRegistrationService).removeClientDetails("client")
    }

    @Test
    internal fun `remove client duplicates exist no call to delete deployment `() {
      whenever(clientRepository.findByIdStartsWithOrderById(any())).thenReturn(
        listOf(
          Client("client"),
          Client("client-1"),
        )
      )

      clientService.removeClient("client")

      verifyNoInteractions(clientDeploymentRepository)
      verifyNoInteractions(clientConfigRepository)
      verify(clientRegistrationService).removeClientDetails("client")
    }
  }

  @Nested
  inner class baseClientId {

    @Test
    fun `baseClientId returns client when hyphen and digit are not present at end of clientId`() {
      val clientId = "client"
      assertThat(ClientService.baseClientId(clientId)).isEqualTo("client")
    }

    @Test
    fun `baseClientId does not remove hyphen and character from end of clientId`() {
      val clientId = "client-id"
      assertThat(ClientService.baseClientId(clientId)).isEqualTo("client-id")
    }

    @Test
    fun `baseClientId removes hyphen and digit from end of clientId`() {
      val clientId = "client-id-1"
      assertThat(ClientService.baseClientId(clientId)).isEqualTo("client-id")
    }

    @Test
    fun `baseClientId removes hyphen and digits from end of clientId`() {
      val clientId = "client-id-12345"
      assertThat(ClientService.baseClientId(clientId)).isEqualTo("client-id")
    }
  }

  @Nested
  inner class isValid {
    @Test
    fun `empty string`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(Client("one"), Client("two", webServerRedirectUri = listOf("one", "two")))
      )
      assertThat(clientService.isValid("")).isFalse
    }

    @Test
    fun `url not found`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(Client("one"), Client("two", webServerRedirectUri = listOf("one", "two")))
      )
      assertThat(clientService.isValid("some_url")).isFalse
    }

    @Test
    fun `url found`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(Client("one"), Client("two", webServerRedirectUri = listOf("one", "two")))
      )
      assertThat(clientService.isValid("two")).isTrue
    }

    @Test
    fun `url found with slash added`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(Client("one"), Client("two", webServerRedirectUri = listOf("one", "two")))
      )
      assertThat(clientService.isValid("two/")).isTrue
    }

    @Test
    fun `url found with slash added in repository`() {
      whenever(clientRepository.findAll()).thenReturn(
        listOf(Client("one"), Client("two", webServerRedirectUri = listOf("one", "two/", "three")))
      )
      assertThat(clientService.isValid("two")).isTrue
    }
  }

  private fun createAuthClientDetails(): AuthClientDetails {
    val authClientDetails = AuthClientDetails()
    authClientDetails.clientId = "client"
    authClientDetails.clientSecret = ""
    authClientDetails.setScope(listOf("read", "write"))
    authClientDetails.setResourceIds(listOf("resourceId"))
    authClientDetails.setAuthorizedGrantTypes(listOf("token", "client"))
    authClientDetails.registeredRedirectUri = setOf("some://url")
    authClientDetails.setAutoApproveScopes(listOf("read", "delius"))
    authClientDetails.authorities = listOf(SimpleGrantedAuthority("role1"), SimpleGrantedAuthority("role2"))
    authClientDetails.accessTokenValiditySeconds = 10
    authClientDetails.refreshTokenValiditySeconds = 20
    authClientDetails.additionalInformation = mapOf("additional" to "info")
    return authClientDetails
  }

  private fun createBaseClientDetails(authClientDetails: AuthClientDetails): BaseClientDetails {
    val baseClientDetails = BaseClientDetails()
    baseClientDetails.clientId = "some-client-25"
    with(authClientDetails) {
      baseClientDetails.clientSecret = clientSecret
      baseClientDetails.setScope(scope)
      baseClientDetails.setResourceIds(resourceIds)
      baseClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes)
      baseClientDetails.registeredRedirectUri = authClientDetails.registeredRedirectUri
      baseClientDetails.setAutoApproveScopes(autoApproveScopes)
      baseClientDetails.authorities = authorities
      baseClientDetails.accessTokenValiditySeconds = accessTokenValiditySeconds
      baseClientDetails.refreshTokenValiditySeconds = refreshTokenValiditySeconds
      baseClientDetails.additionalInformation = additionalInformation
    }
    return baseClientDetails
  }

  private fun createClientDeploymentDetails(): ClientDeployment = ClientDeployment(
    baseClientId = "client",
    type = SERVICE,
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

  private fun createClientConfig(): ClientConfig = ClientConfig(
    baseClientId = "client",
    ips = listOf("127.0.0.1"),
    clientEndDate = LocalDate.now().plusDays(1)
  )
}
