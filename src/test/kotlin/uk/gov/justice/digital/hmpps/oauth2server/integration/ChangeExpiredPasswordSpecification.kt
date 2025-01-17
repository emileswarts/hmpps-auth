package uk.gov.justice.digital.hmpps.oauth2server.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension.Companion.nomisApi
import uk.gov.justice.digital.hmpps.oauth2server.resource.RemoteClientMockServer.Companion.clientBaseUrl

class ChangeExpiredPasswordSpecification : AbstractNomisAndDeliusAuthSpecification() {

  @Page
  lateinit var changeExpiredPasswordPage: ChangeExpiredPasswordPage

  @Page
  private lateinit var homePage: HomePage

  @Page
  private lateinit var mfaEmailPage: MfaEmailPage

  @Test
  fun `Attempt change password without credentials`() {
    goTo(loginPage)
      .loginExpiredUser("EXPIRED_USER", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("", "")
      .checkError("Enter your new password\nEnter your new password again")
  }

  @Test
  fun `Attempt change password with invalid new password`() {
    goTo(loginPage)
      .loginExpiredUser("EXPIRED_USER", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("sompass", "d")
      .checkError(
        "Your password must have both letters and numbers\n" +
          "Your password must have at least 9 characters\n" +
          "Your passwords do not match. Enter matching passwords."
      )
  }

  @Test
  fun `Attempt change password with password on denylist`() {
    goTo(loginPage)
      .loginExpiredUser("EXPIRED_USER", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("iLoveYou2", "iLoveYou2")
      .checkError("Your password is commonly used and may not be secure")
  }

  @Test
  fun `Change password with valid credentials`() {
    goTo(loginPage)
      .loginExpiredUser("EXPIRED_TEST2_USER", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("helloworld2", "helloworld2")

    nomisApi.verify(
      postRequestedFor(urlEqualTo("/users/EXPIRED_TEST2_USER/authenticate"))
        .withRequestBody(equalToJson("""{"password" : "password123456"}"""))
    )

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/EXPIRED_TEST2_USER/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )

    nomisApi.verify(
      postRequestedFor(urlEqualTo("/users/EXPIRED_TEST2_USER/authenticate"))
        .withRequestBody(equalToJson("""{"password" : "helloworld2"}"""))
    )
  }

  @Test
  fun `Change password for auth user with MFA enabled`() {
    goTo(loginPage)
      .loginExpiredUser("AUTH_MFA_EXPIRED", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("helloworld2", "helloworld2")
    mfaEmailPage
      .isAtPage()
      .submitCode(mfaEmailPage.getCode())
    homePage
      .isAtPage()
      .logOut()
    loginPage
      .loginWithMfaEmail("AUTH_MFA_EXPIRED", "helloworld2")
    mfaEmailPage
      .isAtPage()
      .submitCode(mfaEmailPage.getCode())
    homePage
      .isAtPage()
  }

  @Test
  fun `Change password for auth user with valid credentials`() {
    goTo(loginPage)
      .loginExpiredUser("AUTH_EXPIRED", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("helloworld2", "helloworld2")
    homePage
      .isAtPage()
      .logOut()
    loginPage
      .loginAs("AUTH_EXPIRED", "helloworld2")
    homePage
      .isAtPage()
  }

  @Test
  fun `I can sign in from another client`() {
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")
    loginPage
      .loginExpiredUser("EXPIRED_TEST3_USER", "password123456")
    changeExpiredPasswordPage
      .isAtPage()
      .inputAndConfirmNewPassword("dodgypass1", "dodgypass1")

    nomisApi.verify(
      postRequestedFor(urlEqualTo("/users/EXPIRED_TEST3_USER/authenticate"))
        .withRequestBody(equalToJson("""{"password" : "password123456"}"""))
    )

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/EXPIRED_TEST3_USER/change-password"))
        .withRequestBody(equalTo("dodgypass1"))
    )

    nomisApi.verify(
      postRequestedFor(urlEqualTo("/users/EXPIRED_TEST3_USER/authenticate"))
        .withRequestBody(equalToJson("""{"password" : "dodgypass1"}"""))
    )
  }
}

@PageUrl("/change-password")
open class ChangeExpiredPasswordPage : AuthPage<ChangeExpiredPasswordPage>(
  "HMPPS Digital Services - Change Password",
  "Your password has expired"
) {
  @FindBy(css = "input[id='new-password']")
  private lateinit var newPassword: FluentWebElement

  @FindBy(css = "input[id='confirm-password']")
  private lateinit var confirmPassword: FluentWebElement

  @FindBy(css = "input[type='submit']")
  private lateinit var savePassword: FluentWebElement

  fun inputAndConfirmNewPassword(password: String, confirmPassword: String): ChangeExpiredPasswordPage {
    this.newPassword.fill().withText(password)
    this.confirmPassword.fill().withText(confirmPassword)
    savePassword.submit()
    return this
  }
}
