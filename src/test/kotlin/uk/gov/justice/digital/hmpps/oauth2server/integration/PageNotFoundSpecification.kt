package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy

class PageNotFoundSpecification : AbstractNomisAuthSpecification() {

  @Page
  private lateinit var pageNotFoundPage: PageNotFoundPage

  @Page
  private lateinit var homePage: HomePage

  @Test
  fun `Page not found page shown when page does not exist`() {
    goTo("/sign-out")
    goTo("/pagethatdoesntexist")
    loginPage
      .loginPageNotFound("ITAG_USER", "password")
    pageNotFoundPage
      .isAtPage()
      .accept()
    homePage
      .isAtPage()
  }
}

@PageUrl("/error/404")
class PageNotFoundPage : AuthPage<PageNotFoundPage>(
  "Error: HMPPS Digital Services - Page Not Found",
  "Page not found"
) {
  @FindBy(css = "#continue")
  private lateinit var continueButton: FluentWebElement

  fun accept() {
    Assertions.assertThat(continueButton.text()).isEqualTo("OK, continue")
    continueButton.click()
  }

  override fun isAt() {
    Assertions.assertThat(window().title()).isEqualTo(title)
    if (headingStartsWith) {
      Assertions.assertThat(headingText.text()).startsWith(heading)
    } else {
      Assertions.assertThat(headingText.text()).isEqualTo(heading)
    }
  }
}
