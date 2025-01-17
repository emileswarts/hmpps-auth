package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentList
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy

class ClientConfigSpecification : AbstractNomisAuthSpecification() {
  @Page
  private lateinit var clientSummaryPage: ClientSummaryPage

  @Page
  private lateinit var clientMaintenancePage: ClientMaintenancePage

  @Page
  private lateinit var clientMaintenanceAddPage: ClientMaintenanceAddPage

  @Page
  private lateinit var clientCreatedSuccessPage: ClientCreatedSuccessPage

  @Page
  private lateinit var duplicateClientSuccessPage: DuplicateClientSuccessPage

  @Page
  private lateinit var clientMaintenancePageWithError: ClientMaintenancePageWithError

  @Test
  fun `View Client Dashboard once logged in`() {
    goTo("/ui")
    loginPage.isAtPage().submitLogin("ITAG_USER_ADM", "password123456")

    clientSummaryPage.isAtPage()
      .checkClientSummary()
  }

  @Test
  fun `I can edit a client credential`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient()
    clientMaintenancePage.isAtPage().checkDetails().save()
    clientSummaryPage.isAtPage()
  }

  @Test
  fun `I can edit a client credential with allowed Ips`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("another-test-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#ips").value()).isEqualTo("127.0.0.1")
  }

  @Test
  fun `I can edit a client credential with expiry`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("end-date-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#allowExpire").selected()).isTrue
    assertThat(el("#validDays").value()).isEqualTo("7")
  }

  @Test
  fun `I can edit a client credential with extra jwt field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("elite2apiclient")
    clientMaintenancePage.isAtPage()
    assertThat(el("#jwtFields").value()).isEqualTo("-name")
  }

  @Test
  fun `I can edit a client credential with legacy username field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("prison-to-probation-update-api-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#databaseUsernameField").value()).isEqualTo("DSS_USER")
  }

  @Test
  fun `I can edit a client credential with Jira Ticket Number`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("prison-to-probation-update-api-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#jiraNo").value()).isEqualTo("DT-2264")
  }

  @Test
  fun `when Jira Tick number entered url to jira is displayed next to input box`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("prison-to-probation-update-api-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#jiraNo").value()).isEqualTo("DT-2264")
    assertThat(el("#jiraNoLink").text()).isEqualTo("https://dsdmoj.atlassian.net/browse/DT-2264")
  }

  @Test
  fun `I can edit a client credential with an mfa field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("deliusnewtech")
    clientMaintenancePage.isAtPage()
    assertThat(el("#mfa-2").selected()).isTrue
  }

  @Test
  fun `I can edit a client credential and set an mfa field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("v1-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#mfa-3").selected()).isFalse
    clientMaintenancePage.selectCheckboxOption("mfa-3").save()
    clientSummaryPage.isAtPage().editClient("v1-client")
    assertThat(el("#mfa-3").selected()).isTrue
    clientMaintenancePage.selectCheckboxOption("mfa-1").save()
  }

  @Test
  fun `I can edit a client credential with an mfa remember me field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-mfa-remember-test-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#mfaRememberMe").selected()).isTrue
  }

  @Test
  fun `I can edit a client credential and set an mfa remember me field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient("v1-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#mfaRememberMe").selected()).isFalse
    clientMaintenancePage.selectCheckboxOption("mfaRememberMe").save()
    clientSummaryPage.isAtPage().editClient("v1-client")
    assertThat(el("#mfaRememberMe").selected()).isTrue
    clientMaintenancePage.selectCheckboxOption("mfaRememberMe").save()
  }

  @Test
  fun `I can edit a client credential and set the allowed ips field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient("v1-client")
    clientMaintenancePage.isAtPage()
      .edit("ips", "127.0.0.1")
      .save()
    clientSummaryPage.isAtPage().editClient("v1-client")
    assertThat(el("#ips").value()).isEqualTo("127.0.0.1")
  }

  @Test
  fun `I can edit a client credential and set the client end field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient("end-date-client")
    clientMaintenancePage.isAtPage()
      .edit("validDays", "1")
      .save()
    clientSummaryPage.isAtPage().editClient("end-date-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#allowExpire").selected()).isTrue
    assertThat(el("#validDays").value()).isEqualTo("1")

    // restore client end date
    clientMaintenancePage.isAtPage()
      .edit("validDays", "7")
      .save()
  }

  @Test
  fun `I can edit a client credential and remove the client end field`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient("end-date-client")
    clientMaintenancePage.isAtPage()
      .edit("validDays", "")
      .save()
    clientSummaryPage.isAtPage().editClient("end-date-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#allowExpire").selected()).isFalse
    clientMaintenancePage.isAtPage()
      .selectCheckboxOption("allowExpire")
    assertThat(el("#validDays").value()).isEqualTo("")

    // restore client end date
    clientMaintenancePage.isAtPage()
      .edit("validDays", "7")
      .save()
  }

  @Test
  fun `I can edit a client credential and remove the client end field by unchecking the allow expire check box`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient("end-date-client")
    clientMaintenancePage.isAtPage()
      .selectCheckboxOption("allowExpire")
      .save()
    clientSummaryPage.isAtPage().editClient("end-date-client")
    clientMaintenancePage.isAtPage()
    assertThat(el("#allowExpire").selected()).isFalse
    clientMaintenancePage.isAtPage()
      .selectCheckboxOption("allowExpire")
    assertThat(el("#validDays").value()).isEqualTo("")

    // restore client end date
    clientMaintenancePage.isAtPage()
      .edit("validDays", "7")
      .save()
  }

  @Test
  fun `I can edit a client credential as an auth user`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient()
    clientMaintenancePage.isAtPage().checkDetails().save()
    clientSummaryPage.isAtPage()
  }

  @Test
  fun `I can edit a client and new details are copied over to the duplicate`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("rotation-test-client-1")
    clientMaintenancePage.isAtPage()
      .selectCheckboxOption("allowExpire")
      .edit("validDays", "7")
      .edit("registeredRedirectUri", "http://a_url:3003")
      .edit("accessTokenValiditySeconds", "1234")
      .edit("scopes", "read,bob")
      .edit("ips", "127.0.0.1")
      .save()
    clientSummaryPage.isAtPage()
    goTo(clientSummaryPage).editClient("rotation-test-client-1")
    with(clientMaintenancePage) {
      isAtPage()
      assertThat(el("#registeredRedirectUri").value()).isEqualTo("http://a_url:3003")
      assertThat(el("#accessTokenValiditySeconds").value()).isEqualTo("1234")
      assertThat(el("#scopes").value()).isEqualTo("read,bob")
      assertThat(el("#ips").value()).isEqualTo("127.0.0.1")
      assertThat(el("#allowExpire").selected()).isTrue
      assertThat(el("#validDays").value()).isEqualTo("7")
    }
    goTo("/ui/clients/form?client=rotation-test-client-2")
    with(clientMaintenancePage) {
      assertThat(el("#registeredRedirectUri").value()).isEqualTo("http://a_url:3003")
      assertThat(el("#accessTokenValiditySeconds").value()).isEqualTo("1234")
      assertThat(el("#scopes").value()).isEqualTo("read,bob")
      assertThat(el("#ips").value()).isEqualTo("127.0.0.1")
      assertThat(el("#allowExpire").selected()).isTrue
      assertThat(el("#validDays").value()).isEqualTo("7")
    }
  }

  @Test
  fun `I can edit a client duplicate and new details are copied over to the original`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo("/ui/clients/form?client=rotation-test-client-2")
    clientMaintenancePage.isAtPage()
      .edit("resourceIds", "some_resource")
      .edit("refreshTokenValiditySeconds", "2345")
      .edit("authorities", "  BOB\n\n, role_joe \n")
      .save()
    clientSummaryPage.isAtPage()
    goTo(clientSummaryPage).editClient("rotation-test-client-1")
    with(clientMaintenancePage) {
      isAtPage()
      assertThat(el("#resourceIds").value()).isEqualTo("some_resource")
      assertThat(el("#refreshTokenValiditySeconds").value()).isEqualTo("2345")
      assertThat(el("#authorities").value()).isEqualTo("BOB\nJOE")
    }
    goTo("/ui/clients/form?client=rotation-test-client-2")
    with(clientMaintenancePage) {
      assertThat(el("#resourceIds").value()).isEqualTo("some_resource")
      assertThat(el("#refreshTokenValiditySeconds").value()).isEqualTo("2345")
      assertThat(el("#authorities").value()).isEqualTo("BOB\nJOE")
    }
  }

  @Test
  fun `I can create and remove client credential`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient(client = "client")
    clientMaintenanceAddPage.isAtPage()
      .edit("clientId", "new-client")
      .edit("registeredRedirectUri", "http://a_url:3003")
      .edit("accessTokenValiditySeconds", "1200")
      .edit("scopes", "read")
      .edit("authorities", "  BOB\n\n, role_fred \n")
      .selectCheckboxOption("client_credentials")
      .edit("jwtFields", "-name")
      .edit("jiraNo", "DT-2264")
      .edit("ips", "127.0.0.1")
      .selectCheckboxOption("mfa-3")
      .save()
    clientCreatedSuccessPage.isAtPage()
      .checkClientSuccessDetails()
      .continueToClientPage()
    clientMaintenancePage.isAtPage()
      .cancelBackToUI()
    clientSummaryPage.isAtPage()
      .checkClientSummary(
        client = "new-client",
        text =
        """
          new-client 
          client_credentials 
          BOB FRED 
        """
      )

    // now remove so test is re-runnable
    goTo("/ui/clients/new-client/delete")
    clientSummaryPage.isAtPage()
      .checkClientDoesntExist("new-client")
  }

  @Test
  fun `I can create and remove client credential - create clientId ends with whitespace`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientSummaryPage).editClient(client = "client")
    clientMaintenanceAddPage.isAtPage()
      .edit("jiraNo", "DT-2264")
      .edit("clientId", "new-client  ")
      .edit("registeredRedirectUri", "http://a_url:3003")
      .edit("accessTokenValiditySeconds", "1200")
      .edit("scopes", "read")
      .edit("authorities", "  BOB\n\n, role_fred \n")
      .selectCheckboxOption("client_credentials")
      .edit("ips", "127.0.0.1")
      .edit("jwtFields", "-name")
      .selectCheckboxOption("mfa-3")
      .save()
    clientCreatedSuccessPage.isAtPage()
      .checkClientSuccessDetails()
      .continueToClientPage()
    clientMaintenancePage.isAtPage()
      .cancelBackToUI()
    clientSummaryPage.isAtPage()
      .checkClientSummary(
        client = "new-client",
        text =
        """
          new-client 
          client_credentials 
          BOB FRED 
        """
      )

    // now remove so test is re-runnable
    goTo("/ui/clients/new-client/delete")
    clientSummaryPage.isAtPage()
      .checkClientDoesntExist("new-client")
  }

  @Test
  fun `I can duplicate a client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("rotation-test-client-1")
    clientMaintenancePage.isAtPage()
      .duplicate()

    duplicateClientSuccessPage.isAtPage()
      .checkClientSuccessDetails()
      .continueToClientUiPage()

    // now remove so test is re-runnable
    goTo("/ui/clients/rotation-test-client-3/delete")
    clientSummaryPage.isAtPage()
      .checkClientDoesntExist("rotation-test-client-3")
  }

  @Test
  fun `Display last accessed, created and secret updated`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("rotation-test-client-1")
    with(clientMaintenancePage) {
      isAtPage()
      assertThat(el("#rotation-test-client-1-last-accessed").text()).isEqualTo("28-01-2013 13:23")
      assertThat(el("#rotation-test-client-1-secret-updated").text()).isEqualTo("27-01-2013 13:23")
      assertThat(el("#rotation-test-client-1-created").text()).isEqualTo("26-01-2013 13:23")
      assertThat(el("#rotation-test-client-2-last-accessed").text()).isEqualTo("25-12-2018 01:03")
      assertThat(el("#rotation-test-client-1-secret-updated").text()).isEqualTo("27-01-2013 13:23")
      assertThat(el("#rotation-test-client-2-created").text()).isEqualTo("25-12-2018 01:03")
    }
  }

  @Test
  fun `I receive error if I try to have more than 3 of a client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("rotation-test-client")
    clientMaintenancePage.isAtPage()
      .duplicate()

    duplicateClientSuccessPage.isAtPage()
      .continueToClientUiPage()

    clientMaintenancePage.isAtPage()
      .duplicate()

    clientMaintenancePageWithError
      .checkError("You are only allowed 3 versions of this client at one time. You will need to delete one to be able to duplicate it again.")

    // now remove so test is re-runnable
    goTo("/ui/clients/rotation-test-client-3/delete")
    clientSummaryPage.isAtPage()
      .checkClientDoesntExist("rotation-test-client-3")
  }

  @Test
  fun `Client deployment details are displayed for hosting - cloud platform`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
  }

  @Test
  fun `Client deployment details are displayed for hosting - other`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("individual-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsOther()
  }

  @Test
  fun `Client deployment detail and allowed ips are not deleted when duplicate client is delete but duplicated exist`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
      .checkAllowedIps()
      .duplicate()

    goTo("/ui/clients/service-client-1/delete")

    goTo(clientSummaryPage).editClient("service-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
      .checkAllowedIps()
  }

  @Test
  fun `Client deployment detail and allowed ips  are not deleted when original client is delete but duplicated exist`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
      .checkAllowedIps()
      .duplicate()

    goTo("/ui/clients/service-client/delete")

    goTo(clientSummaryPage).editClient("service-client")
    clientMaintenancePage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
      .checkAllowedIps()
  }

  @Test
  fun `Service details are displayed for a authorisation grant client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("manage-user-accounts-ui")
    clientMaintenancePage.isAtPage()
      .checkServiceDetails(
        name = "Manage user accounts",
        description = "",
        authorisedRoles = "AUTH_GROUP_MANAGER CREATE_USER KW_MIGRATION MAINTAIN_ACCESS_ROLES MAINTAIN_ACCESS_ROLES_ADMIN MAINTAIN_OAUTH_USERS",
        url = "http://localhost:3001/",
        email = "",
        enabled = "Yes"
      )
  }

  @Test
  fun `Service details are not displayed for a client-credential clients`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("v1-client")
    clientMaintenancePage.isAtPage()
      .checkServiceDetailsNotShown()
  }

  @Test
  fun `I can filter by role`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).filterBy(role = "report")
    clientSummaryPage.checkClientSummary(rowsMin = 2, rowsMax = 2)
    clientSummaryPage.checkClientDoesntExist("azure-login-client")

    val roleColumns = find("table tbody td[data-test='roles']").texts()
    assertThat(roleColumns).hasSizeGreaterThanOrEqualTo(2).containsOnly("REPORTING")
  }

  @Test
  fun `I can filter by grant type`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).filterBy(grantType = "client_credentials")
    clientSummaryPage.checkClientSummary(rowsMin = 30)
    clientSummaryPage.checkClientDoesntExist("azure-login-client")

    val grantTypeColumns = find("table tbody td[data-test='grantTypes']").texts()
    assertThat(grantTypeColumns).hasSizeGreaterThanOrEqualTo(2)
    grantTypeColumns.forEach { assertThat(it).contains("client_credentials") }
  }

  @Test
  fun `I can filter by client type`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).filterBy(clientType = "SERVICE")
    clientSummaryPage.checkClientSummary(
      rowsMin = 3, rowsMax = 20, client = "service-client", text = "service-client Service A Team client_credentials"
    )
    clientSummaryPage.checkClientDoesntExist("apireporting")

    val clientTypeColumns = find("table tbody td[data-test='service']").texts()
    assertThat(clientTypeColumns).hasSizeGreaterThanOrEqualTo(2).containsOnly("Service")
  }

  @Test
  fun `I can filter by multiple criteria`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).filterBy(grantType = "client_credentials", role = "community_events")
    clientSummaryPage.checkClientSummary(
      rowsMin = 2,
      rowsMax = 5,
      client = "probation-offender-events-client",
      text = "probation-offender-events-client client_credentials COMMUNITY COMMUNITY_EVENTS"
    )
    clientSummaryPage.checkClientDoesntExist("apireporting")
  }

  @Test
  fun `I can clear search criteria`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).filterBy(grantType = "client_credentials", role = "community_events")
    clientSummaryPage.checkClientSummary(
      rowsMin = 2,
      rowsMax = 5,
      client = "probation-offender-events-client",
      text = "probation-offender-events-client client_credentials COMMUNITY COMMUNITY_EVENTS"
    )
    clientSummaryPage.checkClientDoesntExist("apireporting")
    clientSummaryPage.clearFilter()
    // clientSummaryPage.checkClientSummary(client = "apireporting")
    newInstance(ClientSummaryPage::class.java).isAtPage().checkClientSummary("apireporting")
  }
}

@PageUrl("/ui")
class ClientSummaryPage : AuthPage<ClientSummaryPage>(
  "HMPPS Digital Services - Administration Dashboard",
  "OAuth client details"
) {
  @FindBy(css = "input[name='role']")
  private lateinit var role: FluentWebElement

  @FindBy(css = "select[name='grantType']")
  private lateinit var grantType: FluentWebElement

  @FindBy(css = "select[name='clientType']")
  private lateinit var clientType: FluentWebElement

  @FindBy(css = "button[type='submit']")
  private lateinit var searchButton: FluentWebElement

  @FindBy(css = "a[data-test='clear-link']")
  private lateinit var clearLink: FluentWebElement

  fun checkClientSummary(
    client: String = "apireporting",
    text: String =
      """
      apireporting 
      client_credentials 
      REPORTING
      """,
    rowsMin: Int = 10,
    rowsMax: Int = 200,
  ): ClientSummaryPage {
    find("table tbody tr")
    this.role.text()
    assertThat(rows).hasSizeGreaterThanOrEqualTo(rowsMin)
    assertThat(rows).hasSizeLessThanOrEqualTo(rowsMax)
    assertThat(el("tr[data-qa='$client']").text().replace("\n", " ")).startsWith(text.replaceIndent().replace("\n", ""))
    return this
  }

  private val rows: FluentList<FluentWebElement>?
    get() = find("table tbody tr")

  fun checkClientDoesntExist(client: String) {
    assertThat(el("tr[data-qa='$client']").displayed()).isFalse
  }

  fun editClient(client: String = "apireporting") {
    val baseClient = client.replace(regex = "-[0-9]*$".toRegex(), replacement = "")
    el("#edit-$baseClient").click()
  }

  fun filterBy(role: String? = null, grantType: String? = null, clientType: String? = null) {
    role?.let { this.role.fill().withText(it) }
    grantType?.let { this.grantType.fillSelect().withValue(it) }
    clientType?.let { this.clientType.fillSelect().withValue(it) }
    searchButton.submit()

    // check that filter options are then saved
    role?.let { assertThat(this.role.value()).isEqualTo(it) }
    grantType?.let { assertThat(this.grantType.value()).isEqualTo(it) }
    clientType?.let { assertThat(this.clientType.value()).isEqualTo(it) }
  }

  fun clearFilter() {
    clearLink.click()

    assertThat(this.role.value()).isEmpty()
    assertThat(this.grantType.value()).isEqualTo("")
    assertThat(this.clientType.value()).isEqualTo("")
  }
}

@PageUrl("/ui/clients/form")
open class ClientMaintenancePage(heading: String = "Edit client", headingStartsWith: Boolean = true) :
  AuthPage<ClientMaintenancePage>(
    "HMPPS Digital Services - Maintain Client Configuration",
    heading,
    headingStartsWith
  ) {
  @FindBy(name = "client-submit")
  private lateinit var save: FluentWebElement

  @FindBy(name = "duplicate-client")
  private lateinit var duplicate: FluentWebElement

  @FindBy(css = "#cancel")
  private lateinit var cancelButton: FluentWebElement

  fun checkDetails(): ClientMaintenancePage {
    assertThat(el("#clientId").value()).isEqualTo("apireporting")
    assertThat(el("#clientSecret").value()).isBlank
    assertThat(el("#accessTokenValiditySeconds").value()).isEqualTo("3600")
    assertThat(el("#authorities").value()).isEqualTo("REPORTING")
    assertThat(el("#jwtFields").value()).isBlank
    assertThat(el("#databaseUsernameField").value()).isBlank
    return this
  }

  fun checkDeploymentDetailsCloudPlatform(team: String = "A Team"): ClientMaintenancePage {
    assertThat(el("#clientType").text()).isEqualTo("SERVICE")
    assertThat(el("#team").text()).isEqualTo(team)
    assertThat(el("#teamContact").text()).isEqualTo("A Team contact")
    assertThat(el("#teamSlack").text()).isEqualTo("A team slack")
    assertThat(el("#hosting").text()).isEqualTo("CLOUDPLATFORM")
    assertThat(el("#namespace").text()).isEqualTo("service-dev")
    assertThat(el("#deployment").text()).isEqualTo("service-deployment")
    assertThat(el("#secretName").text()).isEqualTo("service-secret")
    assertThat(el("#clientIdKey").text()).isEqualTo("API_CLIENT_ID")
    assertThat(el("#secretKey").text()).isEqualTo("API_CLIENT_SECRET")
    return this
  }

  fun checkAllowedIps(): ClientMaintenancePage {
    assertThat(el("#ips").text()).isEqualTo("127.0.0.1")
    return this
  }

  fun checkDeploymentDetailsOther(): ClientMaintenancePage {
    assertThat(el("#clientType").text()).isEqualTo("PERSONAL")
    assertThat(el("#team").text()).isEqualTo("Bob")
    assertThat(el("#teamContact").text()).isEqualTo("Bob@digital.justice.gov.uk")
    assertThat(el("#teamSlack").text()).isEqualTo("bob slack")
    assertThat(el("#hosting").text()).isEqualTo("OTHER")

    assertThat(el("#namespace").displayed()).isFalse
    assertThat(el("#deployment").displayed()).isFalse
    assertThat(el("#secretName").displayed()).isFalse
    assertThat(el("#clientIdKey").displayed()).isFalse
    assertThat(el("#secretKey").displayed()).isFalse
    return this
  }

  fun checkServiceDetails(
    name: String = "service add test client",
    description: String = "test client",
    authorisedRoles: String = "SOME THING",
    url: String = "service-deployment",
    email: String = "some@email.com",
    enabled: String = "Yes",
  ): ClientMaintenancePage {
    assertThat(el("#serviceName").text()).isEqualTo(name)
    assertThat(el("#serviceDescription").text()).isEqualTo(description)
    assertThat(el("#serviceAuthorisedRoles").text()).isEqualTo(authorisedRoles)
    assertThat(el("#serviceUrl").text()).isEqualTo(url)
    assertThat(el("#serviceEmail").text()).isEqualTo(email)
    assertThat(el("#serviceEnabled").text()).isEqualTo(enabled)
    return this
  }

  fun checkServiceDetailsNotShown(): ClientMaintenancePage {
    assertThat(el("#serviceName").displayed()).isFalse
    assertThat(el("#serviceDescription").displayed()).isFalse
    assertThat(el("#serviceAuthorisedRoles").displayed()).isFalse
    assertThat(el("#serviceUrl").displayed()).isFalse
    assertThat(el("#serviceEmail").displayed()).isFalse
    assertThat(el("#serviceEnabled").displayed()).isFalse
    return this
  }

  fun edit(field: String, text: String): ClientMaintenancePage {
    el("#$field").click().fill().with(text)
    return this
  }

  fun selectCheckboxOption(type: String): ClientMaintenancePage {
    el("#$type").click()
    return this
  }

  fun save() {
    save.click()
  }

  fun duplicate(): ClientMaintenancePage {
    duplicate.click()
    return this
  }

  fun cancelBackToUI(): ClientMaintenancePage {
    cancelButton.click()
    return this
  }

  fun deploymentChange() {
    el("#deploymentChange").click()
  }

  fun serviceChange() {
    el("#serviceChange").click()
  }
}

@PageUrl("/ui/clients/form")
class ClientMaintenanceAddPage : ClientMaintenancePage("Add client", false)

@PageUrl("/ui/clients/form")
class ClientMaintenancePageWithError : ClientMaintenancePage("Edit client 'rotation-test-client'", false)

@PageUrl("ui/clients/client-success")
open class ClientCreatedSuccessPage : AuthPage<ClientCreatedSuccessPage>(
  "HMPPS Digital Services - Client Configuration",
  "Client has been created"
) {
  @FindBy(css = "#continue")
  private lateinit var continueButton: FluentWebElement

  fun checkClientSuccessDetails(): ClientCreatedSuccessPage {
    assertThat(el("[data-qa='clientId']").text()).isEqualTo("new-client")
    assertThat(el("[data-qa='clientSecret']").text()).isNotBlank
    assertThat(el("[data-qa='base64ClientId']").text()).isEqualTo("bmV3LWNsaWVudA==")
    assertThat(el("[data-qa='base64ClientSecret']").text()).isNotBlank
    return this
  }

  fun continueToClientPage(): ClientCreatedSuccessPage {
    assertThat(continueButton.text()).isEqualTo("Continue")
    continueButton.click()
    return this
  }
}

@PageUrl("ui/clients/duplicate-client-success")
open class DuplicateClientSuccessPage : AuthPage<DuplicateClientSuccessPage>(
  "HMPPS Digital Services - Duplicate Client Configuration",
  "Client has been duplicated"
) {
  @FindBy(css = "#continue")
  private lateinit var continueButton: FluentWebElement

  fun checkClientSuccessDetails(): DuplicateClientSuccessPage {
    assertThat(el("[data-qa='clientId']").text()).isEqualTo("rotation-test-client-3")
    assertThat(el("[data-qa='clientSecret']").text()).isNotBlank
    assertThat(el("[data-qa='base64ClientId']").text()).isEqualTo("cm90YXRpb24tdGVzdC1jbGllbnQtMw==")
    assertThat(el("[data-qa='base64ClientSecret']").text()).isNotBlank
    return this
  }

  fun continueToClientUiPage(): DuplicateClientSuccessPage {
    assertThat(continueButton.text()).isEqualTo("Continue")
    continueButton.click()
    return this
  }
}
