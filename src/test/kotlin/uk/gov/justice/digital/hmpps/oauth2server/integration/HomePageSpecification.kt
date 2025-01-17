package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentList
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.oauth2server.resource.AzureOIDCExtension.Companion.azureOIDC

class HomePageSpecification : AbstractNomisAndDeliusAuthSpecification() {
  @Page
  private lateinit var homePage: HomePage

  @Test
  fun `Log in with licences user`() {
    goTo(loginPage).loginAs("CA_USER")
    homePage.isAt()

    homePage.checkLinks("prison-staff-hub", "HDC", "DETAILS")
  }

  @Test
  fun `Log in with nomis user`() {
    goTo(loginPage).loginAs("ITAG_USER", "password")
    homePage.isAt()

    homePage.checkLinks("categorisationtool", "prison-staff-hub", "KW", "DETAILS", "manage-user-accounts-ui")
  }

  @Test
  fun `Log in with azuread user not linked to any users`() {
    azureOIDC.stubToken("notlinkedazureuser@justice.gov.uk")
    goTo(loginPage).clickAzureOIDCLink()
    homePage.isAt()
    homePage.checkNotificationBannerContains("You cannot access any services because you do not have any roles associated with your account")

    homePage.checkLinks("DETAILS")
  }

  @Test
  fun `Log in with azuread user with linked user no roles`() {
    azureOIDC.stubToken("azureuser@justice.gov.uk")
    goTo(loginPage).clickAzureOIDCLink()
    homePage.isAt()
    homePage.checkNotificationBannerContains("You cannot access any services because you do not have any roles associated with your account")

    homePage.checkLinks("DETAILS")
  }

  @Test
  fun `Log in with azuread user with one linked auth user with a role`() {
    azureOIDC.stubToken("authsinglerole@digital.justice.gov.uk")
    goTo(loginPage).clickAzureOIDCLink()
    homePage.isAt()

    homePage.checkLinks("DETAILS", "manage-user-accounts-ui")
  }

  @Test
  fun `Log in with azuread user linked to multiple auth users`() {
    azureOIDC.stubToken("auth_test2@digital.justice.gov.uk")
    goTo(loginPage).clickAzureOIDCLink()
    homePage.isAt()

    homePage.checkLinks("DETAILS", "manage-user-accounts-ui", "OAUTHADMIN", "VIEWCLIENT")
  }

  @Test
  fun `Log in with azuread fails due to invalid access token`() {
    azureOIDC.stubTokenFailure()
    goTo(loginPage).clickAzureOIDCLink()
    loginPage.checkJusticeGovUkError()
  }
}

@PageUrl("/")
class HomePage : AuthPage<HomePage>("HMPPS Digital Services - Home", "Select service") {
  @FindBy(css = "#DETAILS")
  private lateinit var accountDetails: FluentWebElement

  @FindBy(css = ".govuk-heading-m a.govuk-link")
  private lateinit var serviceLinks: FluentList<FluentWebElement>

  fun navigateToAccountDetails() {
    assertThat(accountDetails.text()).isEqualTo("Manage account details")
    accountDetails.click()
  }

  fun checkLinks(vararg links: String) {
    assertThat(serviceLinks.attributes("id")).containsExactlyInAnyOrderElementsOf(links.toList())
  }

  fun checkNomisCurrentlyUnavailableMessage() {
    assertThat(el("[id='service unavailable']").text()).isEqualTo("NOMIS is currently unavailable so we may not have been able to retrieve all your account details")
  }

  fun checkDeliusCurrentlyUnavailableMessage() {
    assertThat(el("[id='service unavailable']").text()).isEqualTo("DELIUS is currently unavailable so we may not have been able to retrieve all your account details")
  }
  fun checkNomisAndDeliusCurrentlyUnavailableMessage() {
    assertThat(el("[id='service unavailable']").text()).isEqualTo("NOMIS and DELIUS are currently unavailable so we may not have been able to retrieve all your account details")
  }
}
