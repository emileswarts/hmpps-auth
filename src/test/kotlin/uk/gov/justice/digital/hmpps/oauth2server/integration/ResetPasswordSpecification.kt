package uk.gov.justice.digital.hmpps.oauth2server.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension.Companion.nomisApi
import java.net.HttpURLConnection

class ResetPasswordSpecification : AbstractNomisAndDeliusAuthSpecification() {

  @Page
  private lateinit var resetPasswordRequestPage: ResetPasswordRequestPage

  @Page
  private lateinit var resetPasswordLinkSentPage: ResetPasswordLinkSentPage

  @Page
  private lateinit var resetPasswordUsernamePage: ResetPasswordUsernamePage

  @Page
  private lateinit var resetPasswordPage: ResetPasswordPage

  @Page
  private lateinit var setNewPasswordPage: SetNewPasswordPage

  @Page
  private lateinit var usernameResetPasswordPage: UsernameResetPasswordPage

  @Page
  private lateinit var resetPasswordPageInvalidToken: ResetPasswordPageInvalidToken

  @Page
  private lateinit var resetPasswordSuccessPage: ResetPasswordSuccessPage

  @Page
  private lateinit var homePage: HomePage

  @Test
  fun `A user can cancel reset password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage.isAtPage()
      .cancel()

    loginPage.isAtPage()
  }

  @Test
  fun `A user must enter a valid email address`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage.isAtPage()
      .submitUsernameOrEmail("joe@bloggs.com")
      .checkError("Enter your work email address")
      .assertUsernameOrEmailText("joe@bloggs.com")
  }

  @Test
  fun `A user can enter their gsi email address to reset their password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage.isAtPage()
      .submitUsernameOrEmail("reset_test@hmps.gsi.gov.uk")

    resetPasswordLinkSentPage.isAtPage()
  }

  @Test
  fun `A user can reset their password by email address`() {
    nomisApi.stubFor(
      get(urlPathEqualTo("/users/CA_USER_TEST"))
        .atPriority(1)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpURLConnection.HTTP_OK)
            .withBody(
              """
            {
                  "username": "CA_USER_TEST",
                  "staffId": 100,
                  "firstName": "Api",
                  "lastName": "User",
                  "activeCaseloadId": "MDI",
                  "accountStatus": "EXPIRED",
                  "accountType": "GENERAL",
                  "primaryEmail": "ca_user@digital.justice.gov.uk",
                  "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"],
                  "accountNonLocked": true,
                  "credentialsNonExpired": true,
                  "enabled": true,
                  "active": true
            }
              """.trimIndent()
            )
        )
    )

    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("ca_user@digital.justice.gov.uk")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordUsernamePage
      .inputUsernameAndContinue("EXPIRED_TEST2_USER")
      .checkError("The username entered is not linked to your email address")
      .inputUsernameAndContinue("CA_USER_TEST")

    usernameResetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    goTo(loginPage)
      .loginAs("CA_USER_TEST", "helloworld2")
    homePage.isAt()

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/CA_USER_TEST")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/CA_USER_TEST/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/CA_USER_TEST/unlock-user")))
  }

  @Test
  fun `A user can reset their password by username`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("RESET_TEST_USER")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    goTo(loginPage)
      .loginAs("RESET_TEST_USER", "helloworld2")
    homePage.isAt()

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/RESET_TEST_USER")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/RESET_TEST_USER/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/RESET_TEST_USER/unlock-user")))
  }

  @Test
  fun `An auth user can reset their password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("AUTH_LOCKED2")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage()

    goTo(loginPage)
      .loginAs("AUTH_LOCKED2", "helloworld2")
    homePage.isAt()
  }

  @Test
  fun `A locked NOMIS user can reset their password`() {
    nomisApi.stubFor(
      get(urlPathEqualTo("/users/LOCKED_NOMIS_USER"))
        .atPriority(1)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpURLConnection.HTTP_OK)
            .withBody(
              """
            {
                  "username": "LOCKED_NOMIS_USER",
                  "staffId": 100,
                  "firstName": "Api",
                  "lastName": "User",
                  "activeCaseloadId": "MDI",
                  "accountStatus": "EXPIRED",
                  "accountType": "GENERAL",
                  "primaryEmail": "reset_test@digital.justice.gov.uk",
                  "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"],
                  "accountNonLocked": true,
                  "credentialsNonExpired": true,
                  "enabled": true,
                  "active": false
            }
              """.trimIndent()
            )
        )
    )

    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("LOCKED_NOMIS_USER")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    // can't now call attempt login as the unlock was calling the nomis api

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/LOCKED_NOMIS_USER")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/LOCKED_NOMIS_USER/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/LOCKED_NOMIS_USER/unlock-user")))
  }

  @Test
  fun `A DELIUS user can reset their password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("DELIUS_PASSWORD_RESET")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider("nDelius")

    goTo(loginPage)
      .loginAs("DELIUS_PASSWORD_RESET", "helloworld2")
    homePage.isAt()
  }

  @Test
  fun `A DELIUS user with locked disabled auth account can reset their password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("DELIUS_ENABLED_AUTH_DISABLED_LOCKED")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage()

    goTo(loginPage)
      .loginAs("DELIUS_ENABLED_AUTH_DISABLED_LOCKED", "helloworld2")
    homePage.isAt()
  }

  @Test
  fun `A user can reset their password back with lowercase username`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("reset_test_user")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    goTo(loginPage)
      .loginAs("reset_test_user", "helloworld2")
    homePage.isAt()

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/RESET_TEST_USER")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/RESET_TEST_USER/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/RESET_TEST_USER/unlock-user")))
  }

  @Test
  fun `Attempt reset password without credentials`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("RESET_TEST_USER")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("", "")
    setNewPasswordPage
      .checkError(
        "Enter your new password\n" +
          "Enter your new password again"
      )
      .inputAndConfirmNewPassword("somepass", "d")
      .checkError(
        "Your password must have both letters and numbers\n" +
          "Your password must have at least 9 characters\n" +
          "Your passwords do not match. Enter matching passwords."
      )
  }

  @Test
  fun `A user is asked to reset password again if the reset link is invalid`() {
    goTo("/reset-password-confirm?token=someinvalidtoken")

    resetPasswordPageInvalidToken.checkError("This link is invalid. Please enter your username or email address and try again.")
  }

  @Test
  fun `A NOMIS user who has never logged into DPS can reset password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("NOMIS_NEVER_LOGGED_IN")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    goTo(loginPage)
      .loginAs("NOMIS_NEVER_LOGGED_IN", "helloworld2")
    homePage.isAt()

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN/unlock-user")))
  }

  @Test
  fun `A NOMIS user who has never logged into DPS can reset password by email address`() {
    nomisApi.stubFor(
      post(urlPathEqualTo("/users/user"))
        .withQueryParam("email", equalTo("bob.smith.never@justice.gov.uk"))
        .atPriority(1)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpURLConnection.HTTP_OK)
            .withBody(
              """
            [{
                "username": "NOMIS_NEVER_LOGGED_IN2",
                "staffId": 100,
                "firstName": "Api",
                "lastName": "User",
                "activeCaseloadId": "MDI",
                "accountStatus": "OPEN",
                "accountType": "GENERAL",
                "primaryEmail": "bob.smith.never@justice.gov.uk",
                "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"],
                "accountNonLocked": true,
                "credentialsNonExpired": true,
                "enabled": true,
                "active": true
            }]
              """.trimIndent()
            )
        )
    )
    nomisApi.stubFor(
      get(urlPathEqualTo("/users/NOMIS_NEVER_LOGGED_IN2"))
        .atPriority(1)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpURLConnection.HTTP_OK)
            .withBody(
              """
            {
                  "username": "NOMIS_NEVER_LOGGED_IN2",
                  "staffId": 100,
                  "firstName": "Api",
                  "lastName": "User",
                  "activeCaseloadId": "MDI",
                  "accountStatus": "EXPIRED",
                  "accountType": "GENERAL",
                  "primaryEmail": "bob.smith.never@justice.gov.uk",
                  "dpsRoleCodes": ["ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN"],
                  "accountNonLocked": true,
                  "credentialsNonExpired": true,
                  "enabled": true,
                  "active": false
            }
              """.trimIndent()
            )
        )
    )

    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("bob.smith.never@justice.gov.uk")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider()

    goTo(loginPage)
      .loginAs("NOMIS_NEVER_LOGGED_IN2", "helloworld2")
    homePage.isAt()

    nomisApi.verify(getRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN2")))

    nomisApi.verify(
      putRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN2/change-password"))
        .withRequestBody(equalTo("helloworld2"))
    )
    nomisApi.verify(putRequestedFor(urlEqualTo("/users/NOMIS_NEVER_LOGGED_IN2/unlock-user")))
  }

  @Test
  fun `A Delius user who has never logged into DPS can reset password`() {
    goTo(loginPage)
      .forgottenPasswordLink()

    resetPasswordRequestPage
      .submitUsernameOrEmail("DELIUS_PASSWORD_NEW")

    resetPasswordLinkSentPage.isAtPage()
    val resetLink = resetPasswordLinkSentPage.getResetLink()

    goTo(resetLink)

    resetPasswordPage
      .inputAndConfirmNewPassword("helloworld2")

    resetPasswordSuccessPage.isAtPage().checkIdProvider("nDelius")

    goTo(loginPage)
      .loginAs("DELIUS_PASSWORD_NEW", "helloworld2")
    homePage.isAt()
  }
}

@PageUrl("/reset-password")
open class ResetPasswordRequestPage :
  AuthPage<ResetPasswordRequestPage>("HMPPS Digital Services - Reset Password", "Create a new password") {
  @FindBy(css = "input[type='submit']")
  private lateinit var continueButton: FluentWebElement
  private lateinit var usernameOrEmail: FluentWebElement

  fun submitUsernameOrEmail(usernameOrEmail: String): ResetPasswordRequestPage {
    this.usernameOrEmail.fill().withText(usernameOrEmail)
    assertThat(continueButton.value()).isEqualTo("Continue")
    continueButton.click()
    return this
  }

  fun assertUsernameOrEmailText(email: String) {
    assertThat(this.usernameOrEmail.value()).isEqualTo(email)
  }

  fun cancel() {
    el("[data-qa='back-link']").click()
  }
}

@PageUrl("/reset-password")
open class ResetPasswordLinkSentPage :
  AuthPage<ResetPasswordLinkSentPage>("HMPPS Digital Services - Reset Password Email Sent", "Check your email") {

  fun getResetLink(): String = el("#resetLink").attribute("href")
}

@PageUrl("/reset-password-select")
open class ResetPasswordUsernamePage :
  AuthPage<ResetPasswordUsernamePage>("HMPPS Digital Services - Set Password Select", "Enter your username") {
  @FindBy(css = "input[id='username']")
  private lateinit var username: FluentWebElement

  @FindBy(css = "input[type='submit']")
  private lateinit var continueButton: FluentWebElement

  fun inputUsernameAndContinue(username: String): ResetPasswordUsernamePage {
    this.username.fill().withText(username)
    continueButton.submit()
    return this
  }
}

@PageUrl("/reset-password-confirm")
open class ResetPasswordPage :
  AuthPage<ResetPasswordPage>("HMPPS Digital Services - Create a password", "Create a new password") {
  @FindBy(css = "input[id='new-password']")
  private lateinit var newPassword: FluentWebElement

  @FindBy(css = "input[id='confirm-password']")
  private lateinit var confirmPassword: FluentWebElement

  @FindBy(css = "input[type='submit']")
  private lateinit var savePassword: FluentWebElement

  fun inputAndConfirmNewPassword(password: String, confirmPassword: String = password): ResetPasswordPage {
    this.newPassword.fill().withText(password)
    this.confirmPassword.fill().withText(confirmPassword)
    savePassword.submit()
    return this
  }
}

// Duplicate of ResetPasswordPage as resetPasswordChosen method needs redirects implementing to update the url
@PageUrl("/reset-password-select")
open class UsernameResetPasswordPage :
  AuthPage<UsernameResetPasswordPage>("HMPPS Digital Services - Create a password", "Create a new password") {
  @FindBy(css = "input[id='new-password']")
  private lateinit var newPassword: FluentWebElement

  @FindBy(css = "input[id='confirm-password']")
  private lateinit var confirmPassword: FluentWebElement

  @FindBy(css = "input[type='submit']")
  private lateinit var savePassword: FluentWebElement

  fun inputAndConfirmNewPassword(password: String, confirmPassword: String = password): UsernameResetPasswordPage {
    this.newPassword.fill().withText(password)
    this.confirmPassword.fill().withText(confirmPassword)
    savePassword.submit()
    return this
  }
}

@PageUrl("/reset-password-confirm")
open class ResetPasswordPageInvalidToken :
  AuthPage<ResetPasswordPageInvalidToken>("HMPPS Digital Services - Reset Password", "Create a new password")

@PageUrl("/reset-password-success")
open class ResetPasswordSuccessPage :
  AuthPage<ResetPasswordSuccessPage>("HMPPS Digital Services - Reset Password Success", "Your password has changed")
