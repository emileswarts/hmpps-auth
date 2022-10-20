package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.Test

class ChangeEmailNomisDownSpecification : AbstractDeliusAuthSpecification() {

  @Page
  private lateinit var passwordPromptForEmailPage: PasswordPromptForEmailPage

  @Page
  private lateinit var existingPasswordPage: ExistingPasswordPage

  @Test
  fun `Change email flow incorrect password nomis down`() {
    goTo(loginPage)
      .loginExistingPasswordChangeEmail("AUTH_CHANGE_EMAIL2", "password123456")
    goTo(passwordPromptForEmailPage)
      .isAtPage()
      .inputAndConfirmCurrentPassword("password1234567")
    existingPasswordPage
      .checkError(
        "Your password is incorrect. You will be locked out if you enter the wrong details 3 times." +
          "\nNOMIS is experiencing issues. Please try later if you are attempting to sign in using your NOMIS credentials."

      )
  }
}
