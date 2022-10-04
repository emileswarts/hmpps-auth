package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentList
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy

class ClientConfigViewOnlySpecification : AbstractNomisAuthSpecification() {

  @Page
  private lateinit var clientViewOnlySummaryPage: ClientViewOnlySummaryPage

  @Page
  private lateinit var clientViewOnlyPage: ClientViewOnlyPage

  @Test
  fun `View Viewonly Client Dashboard once logged in`() {
    goTo("/ui/view")
    loginPage.isAtPage().submitLogin("ITAG_USER_ADM", "password123456")

    clientViewOnlySummaryPage.isAtPage()
      .checkClientSummary()
  }

  @Test
  fun `View Viewonly Client Dashboard once logged in with ROLE_OAUTH_VIEW_ONLY_CLIENT`() {
    goTo("/ui/view")
    loginPage.isAtPage().submitLogin("AUTH_DEVELOPER", "password123456")

    clientViewOnlySummaryPage.isAtPage()
      .checkClientSummary()
  }

  @Test
  fun `I can view a client credential`() {
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient()
    clientViewOnlyPage.isAtPage().checkDetails().continueButton()
    clientViewOnlySummaryPage.isAtPage()
  }

  @Test
  fun `View only Client deployment details are displayed for hosting - cloud platform`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("service-client")
    clientViewOnlyPage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
  }

  @Test
  fun `view only Client deployment details are displayed for hosting - other`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("individual-client")
    clientViewOnlyPage.isAtPage()
      .checkDeploymentDetailsOther()
  }

  @Test
  fun `view only Client deployment details are displayed for mfa enabled clients`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("service-mfa-remember-test-client")
    clientViewOnlyPage.isAtPage()
      .checkMfaDetails()
  }

  @Test
  fun `Display last accessed, created and secret updated`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("rotation-test-client-1")
    with(clientViewOnlyPage) {
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
  fun `Client deployment details are displayed for hosting - cloud platform`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("service-client")
    clientViewOnlyPage.isAtPage()
      .checkDeploymentDetailsCloudPlatform()
  }

  @Test
  fun `Client deployment details are displayed for hosting - other`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("individual-client")
    clientViewOnlyPage.isAtPage()
      .checkDeploymentDetailsOther()
  }

  @Test
  fun `Service details are displayed for a authorisation grant client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("manage-user-accounts-ui")
    clientViewOnlyPage.isAtPage()
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

    goTo(clientViewOnlySummaryPage).viewClient("v1-client")
    clientViewOnlyPage.isAtPage()
      .checkServiceDetailsNotShown()
  }

  @Test
  fun `ip address are displayed for clients`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("ip-allow-a-client-1")
    clientViewOnlyPage.isAtPage()
      .checkAllowedIps()
  }

  @Test
  fun `end date are displayed for clients`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).viewClient("end-date-client")
  }

  @Test
  fun `I can filter by role`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).filterBy(role = "report")
    clientViewOnlySummaryPage.checkClientSummary(rowsMin = 2, rowsMax = 2)
    clientViewOnlySummaryPage.checkClientDoesntExist("azure-login-client")

    val roleColumns = find("table tbody td[data-test='roles']").texts()
    assertThat(roleColumns).hasSizeGreaterThanOrEqualTo(2).containsOnly("REPORTING")
  }

  @Test
  fun `I can filter by grant type`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).filterBy(grantType = "client_credentials")
    clientViewOnlySummaryPage.checkClientSummary(rowsMin = 30)
    clientViewOnlySummaryPage.checkClientDoesntExist("azure-login-client")

    val grantTypeColumns = find("table tbody td[data-test='grantTypes']").texts()
    assertThat(grantTypeColumns).hasSizeGreaterThanOrEqualTo(2)
    grantTypeColumns.forEach { assertThat(it).contains("client_credentials") }
  }

  @Test
  fun `I can filter by client type`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).filterBy(clientType = "SERVICE")
    clientViewOnlySummaryPage.checkClientSummary(
      rowsMin = 3, rowsMax = 20, client = "service-client", text = "service-client Service A Team client_credentials"
    )
    clientViewOnlySummaryPage.checkClientDoesntExist("apireporting")

    val clientTypeColumns = find("table tbody td[data-test='service']").texts()
    assertThat(clientTypeColumns).hasSizeGreaterThanOrEqualTo(2).containsOnly("Service")
  }

  @Test
  fun `I can filter by multiple criteria`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).filterBy(grantType = "client_credentials", role = "community_events")
    clientViewOnlySummaryPage.checkClientSummary(
      rowsMin = 2,
      rowsMax = 5,
      client = "probation-offender-events-client",
      text = "probation-offender-events-client client_credentials COMMUNITY COMMUNITY_EVENTS"
    )
    clientViewOnlySummaryPage.checkClientDoesntExist("apireporting")
  }

  @Test
  fun `I can clear search criteria`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientViewOnlySummaryPage).filterBy(grantType = "client_credentials", role = "community_events")
    clientViewOnlySummaryPage.checkClientSummary(
      rowsMin = 2,
      rowsMax = 5,
      client = "probation-offender-events-client",
      text = "probation-offender-events-client client_credentials COMMUNITY COMMUNITY_EVENTS"
    )
    clientViewOnlySummaryPage.checkClientDoesntExist("apireporting")
    clientViewOnlySummaryPage.clearFilter()
    // clientViewOnlySummaryPage.checkClientSummary(client = "apireporting")
    newInstance(clientViewOnlySummaryPage::class.java).isAtPage().checkClientSummary("apireporting")
  }
}

@PageUrl("/ui/view")
class ClientViewOnlySummaryPage : AuthPage<ClientViewOnlySummaryPage>(
  "HMPPS Digital Services - Administration Dashboard",
  "OAuth client details - view only"
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
  ): ClientViewOnlySummaryPage {
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

  fun viewClient(client: String = "apireporting") {
    val baseClient = client.replace(regex = "-[0-9]*$".toRegex(), replacement = "")
    el("#view-$baseClient").click()
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

@PageUrl("/ui/clients/view-client")
open class ClientViewOnlyPage(heading: String = "View client", headingStartsWith: Boolean = true) :
  AuthPage<ClientViewOnlyPage>(
    "HMPPS Digital Services - View Client Configuration",
    heading,
    headingStartsWith
  ) {
  @FindBy(css = "#continue")
  private lateinit var continueButton: FluentWebElement

  fun checkDetails(): ClientViewOnlyPage {
    assertThat(el("[data-qa='accessTokenValiditySeconds']").text()).isEqualTo("3600")
    assertThat(el("[data-qa='authorities']").text()).isEqualTo("REPORTING")
    assertThat(el("[data-qa='jwtFields']").text()).isBlank
    assertThat(el("[data-qa='databaseUsernameField']").text()).isBlank
    return this
  }

  fun checkDeploymentDetailsCloudPlatform(team: String = "A Team"): ClientViewOnlyPage {
    assertThat(el("[data-qa='clientType']").text()).isEqualTo("SERVICE")
    assertThat(el("[data-qa='team']").text()).isEqualTo(team)
    assertThat(el("[data-qa='teamContact']").text()).isEqualTo("A Team contact")
    assertThat(el("[data-qa='teamSlack']").text()).isEqualTo("A team slack")
    assertThat(el("[data-qa='hosting']").text()).isEqualTo("CLOUDPLATFORM")
    assertThat(el("[data-qa='namespace']").text()).isEqualTo("service-dev")
    assertThat(el("[data-qa='deployment']").text()).isEqualTo("service-deployment")
    assertThat(el("[data-qa='secretName']").text()).isEqualTo("service-secret")
    assertThat(el("[data-qa='clientIdKey']").text()).isEqualTo("API_CLIENT_ID")
    assertThat(el("[data-qa='secretKey']").text()).isEqualTo("API_CLIENT_SECRET")
    return this
  }

  fun checkAllowedIps(): ClientViewOnlyPage {
    assertThat(el("[data-qa='ips']").text()).isEqualTo("127.0.0.1/32")
    return this
  }

  fun checkDeploymentDetailsOther(): ClientViewOnlyPage {
    assertThat(el("[data-qa='clientType']").text()).isEqualTo("PERSONAL")
    assertThat(el("[data-qa='team']").text()).isEqualTo("Bob")
    assertThat(el("[data-qa='teamContact']").text()).isEqualTo("Bob@digital.justice.gov.uk")
    assertThat(el("[data-qa='teamSlack']").text()).isEqualTo("bob slack")
    assertThat(el("[data-qa='hosting']").text()).isEqualTo("OTHER")

    assertThat(el("[data-qa='namespace']").displayed()).isFalse
    assertThat(el("[data-qa='deployment']").displayed()).isFalse
    assertThat(el("[data-qa='secretName']").displayed()).isFalse
    assertThat(el("[data-qa='clientIdKey']").displayed()).isFalse
    assertThat(el("[data-qa='secretKey']").displayed()).isFalse
    return this
  }

  fun checkMfaDetails(): ClientViewOnlyPage {
    assertThat(el("[data-qa='mfa']").text()).isEqualTo("all")
    assertThat(el("[data-qa='mfaRememberMe']").text()).isEqualTo("true")
    return this
  }

  fun checkServiceDetails(
    name: String = "service add test client",
    description: String = "test client",
    authorisedRoles: String = "SOME THING",
    url: String = "service-deployment",
    email: String = "some@email.com",
    enabled: String = "Yes",
  ): ClientViewOnlyPage {
    assertThat(el("[data-qa='serviceName']").text()).isEqualTo(name)
    assertThat(el("[data-qa='serviceDescription']").text()).isEqualTo(description)
    assertThat(el("[data-qa='serviceAuthorisedRoles']").text()).isEqualTo(authorisedRoles)
    assertThat(el("[data-qa='serviceUrl']").text()).isEqualTo(url)
    assertThat(el("[data-qa='serviceEmail']").text()).isEqualTo(email)
    assertThat(el("[data-qa='serviceEnabled']").text()).isEqualTo(enabled)
    return this
  }
  fun checkServiceDetailsNotShown(): ClientViewOnlyPage {
    assertThat(el("[data-qa='serviceName']").displayed()).isFalse
    assertThat(el("[data-qa='serviceDescription']").displayed()).isFalse
    assertThat(el("[data-qa='serviceAuthorisedRoles']").displayed()).isFalse
    assertThat(el("[data-qa='serviceUrl']").displayed()).isFalse
    assertThat(el("[data-qa='serviceEmail']").displayed()).isFalse
    assertThat(el("[data-qa='serviceEnabled']").displayed()).isFalse
    return this
  }

  fun continueButton() {
    continueButton.click()
  }
}
