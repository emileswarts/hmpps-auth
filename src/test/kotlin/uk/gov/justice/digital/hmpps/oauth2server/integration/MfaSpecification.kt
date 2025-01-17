package uk.gov.justice.digital.hmpps.oauth2server.integration

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.oauth2server.resource.RemoteClientMockServer.Companion.clientBaseUrl

class MfaSpecification : AbstractNomisAndDeliusAuthSpecification() {

  @Page
  private lateinit var mfaEmailPage: MfaEmailPage

  @Page
  private lateinit var mfaTextPage: MfaTextPage

  @Page
  private lateinit var mfaEmailResendCodePage: MfaEmailResendCodePage

  @Page
  private lateinit var mfaTextResendCodePage: MfaTextResendCodePage

  @Page
  private lateinit var homePage: HomePage

  @Test
  fun `Attempt MFA challenge with invalid token`() {
    goTo("/mfa-challenge?token=invalidtoken&mfaPreference=TEXT")
    loginPage.checkLoginAuthenticationFailedError()
  }

  @Test
  fun `Attempt MFA challenge with expired token`() {
    goTo("/mfa-challenge?token=mfa_expired&mfaPreference=EMAIL")
    loginPage.checkLoginAuthenticationTimeoutError()
  }

  @Test
  fun `Login as user with email MFA enabled`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_USER")
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode()
    homePage.isAt()
  }

  @Test
  fun `Login as user with text MFA enabled`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT2")
      .assertMobileCodeDestination("*******0321")
      .submitCode()
    homePage.isAt()
  }

  @Test
  fun `Login as user with second email MFA enabled`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL")
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()
    homePage.isAt()
  }

  @Test
  fun `Log in sets mfa passed in jwt cookie to true`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL")
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()
    homePage.isAt()
    val jwt = homePage.parseJwt()
    assertThat(jwt.getBooleanClaim("passed_mfa")).isTrue
  }

  @Test
  fun `Login as user with unverified text MFA enabled but email verified`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_TEXT_EMAIL")
      .assertEmailCodeDestination("auth******@******.gov.uk")
      .submitCode()
    homePage.isAt()
  }

  @Test
  fun `Login as user with unverified secondary email MFA enabled but email verified`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL_EMAIL")
      .assertEmailCodeDestination("auth_u******@******.gov.uk")
      .submitCode()
    homePage.isAt()
  }

  @Test
  fun `Login as user with MFA enabled but no email addresses or mobile number`() {
    goTo(loginPage)
      .loginError("AUTH_MFA_NOEMAIL_USER")

    loginPage.checkError(
      "We need to send you a security code to sign in, but we can't find a verified email " +
        "address or phone number. Please verify your email address by clicking the link in your email."
    )
  }

  @Test
  fun `Login as user with MFA enabled but no phone or email address (preference text)`() {
    goTo(loginPage)
      .loginError("AUTH_MFA_NOTEXT_USER")

    loginPage.checkError(
      "We need to send you a security code to sign in, but we can't find a verified email " +
        "address or phone number. Please verify your email address by clicking the link in your email."
    )
  }

  @Test
  fun `MFA code is required - email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_EMAIL2")
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .submitCode(" ")

    mfaEmailPage.checkError("Enter the code received in the email")
      .assertEmailCodeDestination("auth.******@******.gov.uk")
  }

  @Test
  fun `MFA code is required - text message`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT2")
      .assertMobileCodeDestination("*******0321")
      .submitCode(" ")

    mfaTextPage.checkError("Enter the code received in the text message")
      .assertMobileCodeDestination("*******0321")
  }

  @Test
  fun `MFA code is incorrect email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_USER")
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123")
    mfaEmailPage.checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
  }

  @Test
  fun `MFA code is incorrect text`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT2")
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")
    mfaTextPage.checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
  }

  @Test
  fun `MFA user email preference gets locked after 3 invalid MFA attempts`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_LOCKED_EMAIL")
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123")

    loginPage.checkLoginAccountLockedError()
      .loginError("AUTH_MFA_LOCKED_EMAIL")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `MFA user text preference gets locked after 3 invalid MFA attempts`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_LOCKED_TEXT")
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")
      .checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")
      .checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")

    loginPage.checkLoginAccountLockedError()
      .loginError("AUTH_MFA_LOCKED_TEXT")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `MFA user secondary email preference gets locked after 3 invalid MFA attempts`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_LOCKED_2ND_EMAIL")
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123")

    loginPage.checkLoginAccountLockedError()
      .loginError("AUTH_MFA_LOCKED_2ND_EMAIL")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `MFA user email preference gets locked after mix of MFA and login attempts`() {

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_EMAIL", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginWithMfaEmail("AUTH_MFA_LOCKED2_EMAIL")
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode("123456")
      .checkEmailCodeIsIncorrectError()

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_EMAIL", "wrongpass")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `MFA user text preference gets locked after mix of MFA and login attempts`() {

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_TEXT", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginWithMfaText("AUTH_MFA_LOCKED2_TEXT")
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")
      .checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
      .submitCode("123456")
      .checkTextCodeIsIncorrectError()

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_TEXT", "wrongpass")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `MFA user secondary email preference gets locked after mix of MFA and login attempts`() {

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_2ND_EMAIL", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginWithMfaEmail("AUTH_MFA_LOCKED2_2ND_EMAIL")
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123456")
      .checkEmailCodeIsIncorrectError()

    goTo(loginPage)
      .loginError("AUTH_MFA_LOCKED2_2ND_EMAIL", "wrongpass")
      .checkLoginAccountLockedError()
  }

  @Test
  fun `Locked count gets reset after successful MFA login email preference`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_EMAIL3")

    val validMfaCode = mfaEmailPage.getCode()
    mfaEmailPage.submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .submitCode(validMfaCode)

    homePage.isAt()

    goTo(loginPage)
      .loginError("AUTH_MFA_PREF_EMAIL3", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginError("AUTH_MFA_PREF_EMAIL3", "wrongpass")
      .checkLoginUsernamePasswordError()
  }

  @Test
  fun `Locked count gets reset after successful MFA login text preference`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT3")

    val validMfaCode = mfaTextPage.getCode()
    mfaTextPage.submitCode("123")
      .checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
      .submitCode("123")
      .checkTextCodeIsIncorrectError()
      .assertMobileCodeDestination("*******0321")
      .submitCode(validMfaCode)

    homePage.isAt()

    goTo(loginPage)
      .loginError("AUTH_MFA_PREF_TEXT3", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginError("AUTH_MFA_PREF_TEXT3", "wrongpass")
      .checkLoginUsernamePasswordError()
  }

  @Test
  fun `Locked count gets reset after successful MFA login secondary email preference`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL3")

    val validMfaCode = mfaEmailPage.getCode()
    mfaEmailPage.submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode("123")
      .checkEmailCodeIsIncorrectError()
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode(validMfaCode)

    homePage.isAt()

    goTo(loginPage)
      .loginError("AUTH_MFA_PREF_2ND_EMAIL3", "wrongpass")
      .checkLoginUsernamePasswordError()
      .loginError("AUTH_MFA_PREF_2ND_EMAIL3", "wrongpass")
      .checkLoginUsernamePasswordError()
  }

  @Test
  fun `Mfa preference email - I would like the MFA code to be resent by email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_USER")

    mfaEmailPage
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeByEmail()

    mfaEmailPage
      .assertEmailCodeDestination("mfa_******@******.gov.uk")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `Mfa preference email - I would like the MFA code to be resent by text`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_EMAIL4")

    mfaEmailPage
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeByText()

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `Mfa preference email - I would like the MFA code to be resent by secondary email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_EMAIL5")

    mfaEmailPage
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeBySecondaryEmail()

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `MFA preference text - I would like the MFA code to be resent by email`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT5")

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .resendCodeLink()

    mfaTextResendCodePage.resendCodeByEmail()

    mfaEmailPage
      .assertEmailCodeDestination("auth******@******.gov.uk")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `MFA preference text - I would like the MFA code to be resent by text`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT5")

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .resendCodeLink()

    mfaTextResendCodePage.resendCodeByText()

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `MFA preference text - I would like the MFA code to be resent by secondary email`() {
    goTo(loginPage)
      .loginWithMfaText("AUTH_MFA_PREF_TEXT5")

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .resendCodeLink()

    mfaTextResendCodePage.resendCodeBySecondaryEmail()

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `Mfa preference secondary email - I would like the MFA code to be resent by Email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL2")

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeByEmail()

    mfaEmailPage
      .assertEmailCodeDestination("auth_u******@******.gov.uk")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `Mfa preference secondary email - I would like the MFA code to be resent by text`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL2")

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeByText()

    mfaTextPage
      .assertMobileCodeDestination("*******0321")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `Mfa preference secondary email - I would like the MFA code to be resent by secondary email`() {
    goTo(loginPage)
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL2")

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .resendCodeLink()

    mfaEmailResendCodePage.resendCodeBySecondaryEmail()

    mfaEmailPage
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()

    homePage.isAt()
  }

  @Test
  fun `I can sign in from another client with MFA enabled email preference`() {
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")

    loginPage
      .loginWithMfaEmail("AUTH_MFA_PREF_EMAIL4")
      .assertEmailCodeDestination("auth.******@******.gov.uk")
      .submitCode()

    val url = driver.currentUrl
    assertThat(url).startsWith("$clientBaseUrl?code")
    assertThat(url).contains("state=$state")

    val authCode = splitQuery(url)["code"]?.first()
    assertThat(authCode).isNotNull

    getAccessToken(authCode!!)
      .jsonPath(".user_name").isEqualTo("AUTH_MFA_PREF_EMAIL4")
      .jsonPath(".auth_source").isEqualTo("auth")
  }

  @Test
  fun `I can sign in from another client with MFA enabled text preference`() {
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")

    loginPage
      .loginWithMfaText("AUTH_MFA_PREF_TEXT4")
      .assertMobileCodeDestination("*******0321")
      .submitCode()

    val url = driver.currentUrl
    assertThat(url).startsWith("$clientBaseUrl?code")
    assertThat(url).contains("state=$state")

    val authCode = splitQuery(url)["code"]?.first()
    assertThat(authCode).isNotNull

    getAccessToken(authCode!!)
      .jsonPath(".user_name").isEqualTo("AUTH_MFA_PREF_TEXT4")
      .jsonPath(".auth_source").isEqualTo("auth")
  }

  private fun splitQuery(url: String): MultiValueMap<String, String> {
    return UriComponentsBuilder.fromUriString(url).build().queryParams
  }

  @Test
  fun `I can sign in from another client with MFA enabled secondary email preference`() {
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")

    loginPage
      .loginWithMfaEmail("AUTH_MFA_PREF_2ND_EMAIL4")
      .assertEmailCodeDestination("jo******@******ith.com")
      .submitCode()

    val url = driver.currentUrl
    assertThat(url).startsWith("$clientBaseUrl?code")
    assertThat(url).contains("state=$state")

    val authCode = splitQuery(url)["code"]?.first()
    assertThat(authCode).isNotNull

    getAccessToken(authCode!!)
      .jsonPath(".user_name").isEqualTo("AUTH_MFA_PREF_2ND_EMAIL4")
      .jsonPath(".auth_source").isEqualTo("auth")
  }
}

@PageUrl("/mfa-challenge")
open class MfaEmailPage : AuthPage<MfaEmailPage>("HMPPS Digital Services - Email Verification", "Check your email") {
  @FindBy(css = "button[id='submit']")
  private lateinit var continueButton: FluentWebElement

  @FindBy(css = "input[name='code']")
  private lateinit var code: FluentWebElement

  @FindBy(css = "input[name='rememberMe']")
  private lateinit var rememberMe: FluentWebElement

  @FindBy(linkText = "Not received an email?")
  private lateinit var resend: FluentWebElement

  @FindBy(css = "#mfa-pref-email-code-destination")
  private lateinit var emailCodeDestination: FluentWebElement

  fun getCode(): String {
    return el("[data-qa='mfa-code']").text()
  }

  fun submitCode(code: String? = null): MfaEmailPage {
    val mfaCode = code ?: el("[data-qa='mfa-code']").text()
    this.code.fill().withText(mfaCode)
    continueButton.submit()
    return this
  }

  fun submitWithoutCode(): MfaEmailPage {
    continueButton.submit()
    return this
  }

  fun rememberMe(): MfaEmailPage {
    rememberMe.click()
    return this
  }

  fun resendCodeLink() {
    resend.click()
  }

  fun checkEmailCodeIsIncorrectError(): MfaEmailPage {
    checkError("Security code is incorrect. Please check your email and try again. You will be locked out if you enter the wrong code 3 times.")
    return this
  }

  fun enterTheCodeError(): MfaEmailPage {
    checkError("Enter the code received in the email")
    return this
  }

  fun assertEmailCodeDestination(text: String): MfaEmailPage {
    assertThat(emailCodeDestination.text()).isEqualTo(text)
    return this
  }
}

@PageUrl("/mfa-challenge")
open class MfaTextPage : AuthPage<MfaTextPage>("HMPPS Digital Services - Text Message Verification", "Check your phone") {
  @FindBy(css = "button[id='submit']")
  private lateinit var continueButton: FluentWebElement

  @FindBy(css = "input[name='code']")
  private lateinit var code: FluentWebElement

  @FindBy(css = "input[name='rememberMe']")
  private lateinit var rememberMe: FluentWebElement

  @FindBy(linkText = "Not received a text message?")
  private lateinit var resend: FluentWebElement

  @FindBy(css = "#mfa-pref-text-code-destination")
  private lateinit var mobileCodeDestination: FluentWebElement

  fun getCode(): String {
    return el("[data-qa='mfa-code']").text()
  }

  fun submitCode() {
    val mfaCode = el("[data-qa='mfa-code']").text()
    this.code.fill().withText(mfaCode)
    continueButton.submit()
  }

  fun submitCode(code: String): MfaTextPage {
    this.code.fill().withText(code)
    continueButton.submit()
    return this
  }

  fun rememberMe(): MfaTextPage {
    rememberMe.click()
    return this
  }

  fun checkTextCodeIsIncorrectError(): MfaTextPage {
    checkError("Security code is incorrect. Please check your phone and try again. You will be locked out if you enter the wrong code 3 times.")
    return this
  }

  fun resendCodeLink() {
    resend.click()
  }

  fun assertMobileCodeDestination(text: String): MfaTextPage {
    assertThat(mobileCodeDestination.text()).isEqualTo(text)
    return this
  }
}

@PageUrl("/mfa-challenge")
open class MfaEmailResendCodePage :
  AuthPage<MfaEmailResendCodePage>("HMPPS Digital Services - Resend Security Code", "Resend security code") {
  @FindBy(css = "button[id='submit']")
  private lateinit var resendSecurityCode: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-email']")
  private lateinit var selectMfaPreferenceEmail: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-text']")
  private lateinit var selectMfaPreferenceText: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-secondary-email']")
  private lateinit var selectMfaPreferenceSecondaryEmail: FluentWebElement

  fun resendCodeByEmail() {
    selectMfaPreferenceEmail.click()
    resendSecurityCode.submit()
  }

  fun resendCodeByText() {
    selectMfaPreferenceText.click()
    resendSecurityCode.submit()
  }

  fun resendCodeBySecondaryEmail() {
    selectMfaPreferenceSecondaryEmail.click()
    resendSecurityCode.submit()
  }
}

@PageUrl("/mfa-resend-text")
open class MfaTextResendCodePage :
  AuthPage<MfaTextResendCodePage>("HMPPS Digital Services - Resend Security Code", "Resend security code") {
  @FindBy(css = "button[id='submit']")
  private lateinit var resendSecurityCode: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-email']")
  private lateinit var selectMfaPreferenceEmail: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-text']")
  private lateinit var selectMfaPreferenceText: FluentWebElement

  @FindBy(css = "input[id='mfa-pref-secondary-email']")
  private lateinit var selectMfaPreferenceSecondaryEmail: FluentWebElement

  fun resendCodeByEmail() {
    selectMfaPreferenceEmail.click()
    resendSecurityCode.submit()
  }

  fun resendCodeByText() {
    selectMfaPreferenceText.click()
    resendSecurityCode.submit()
  }

  fun resendCodeBySecondaryEmail() {
    selectMfaPreferenceSecondaryEmail.click()
    resendSecurityCode.submit()
  }
}

@PageUrl("/account/mfa-challenge")
class AccountMfaEmailPage : MfaEmailPage()

@PageUrl("/account/mfa-challenge")
class AccountMfaTextPage : MfaTextPage()

@PageUrl("/account/mfa-resend")
class AccountMfaEmailResendCodePage : MfaEmailResendCodePage()

@PageUrl("/account/mfa-resend")
class AccountMfaTextResendCodePage : MfaTextResendCodePage()
