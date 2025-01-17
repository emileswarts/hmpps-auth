package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.Test

class NomisDownLoginSpecification : AbstractDeliusAuthSpecification() {

  @Page
  private lateinit var homePage: HomePage

  @Page
  private lateinit var accountDetailsPage: AccountDetailsPage

  @Test
  fun `NOMIS unavailable shows in error`() {
    goTo(loginPage)
      .loginError("NOMIS_USER", "password")
      .checkError(
        "Enter a valid username and password. You will be locked out if you enter the wrong details 3 times." +
          "\nNOMIS is experiencing issues. Please try later if you are attempting to sign in using your NOMIS credentials."
      )
  }

  @Test
  fun `NOMIS unavailable doesn't prevent logging in as auth user`() {
    goTo(loginPage)
      .loginAs("AUTH_USER", "password123456")
    homePage
      .isAtPage()
    assertThat(accountDetailsPage.getCurrentName()).isEqualTo("A. Only")
  }

  @Test
  fun `NOMIS unavailable doesn't prevent logging in as Delius user`() {
    goTo(loginPage)
      .loginAs("DELIUS_USER", "password")
    homePage
      .isAtPage()
    assertThat(accountDetailsPage.getCurrentName()).isEqualTo("D. Smith")
  }

  @Test
  fun `Log in with Azure justice email credentials link results in successful login nomis down`() {
    goTo(loginPage).clickAzureOIDCLink()
    homePage.isAt()

    homePage.checkNomisCurrentlyUnavailableMessage()
  }
}
