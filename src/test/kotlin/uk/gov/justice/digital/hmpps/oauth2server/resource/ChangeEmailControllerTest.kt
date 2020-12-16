package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.authentication.TestingAuthenticationToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.util.Optional

class ChangeEmailControllerTest {
  private val userService: UserService = mock()
  private val tokenService: TokenService = mock()
  private val controller = ChangeEmailController(tokenService, userService)
  private val token = TestingAuthenticationToken(
    UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, "userid", "jwtId"),
    "pass"
  )

  @Nested
  inner class NewEmailRequest {
    @Test
    fun `check view and model`() {
      setupGetToken()
      setupGetUserCallForProfile()
      val modelAndView = controller.newEmailRequest("token")
      assertThat(modelAndView.viewName).isEqualTo("changeEmail")
      assertThat(modelAndView.model["email"]).isEqualTo("someuser@justice.gov.uk")
      assertThat(modelAndView.model).doesNotContainKey("changingUsername")
    }

    @Test
    fun `email address as username auth user`() {
      setupGetToken()
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(
        Optional.of(createSampleUser(username = "someuser@justice.gov.uk"))
      )

      val modelAndView = controller.newEmailRequest("token")
      assertThat(modelAndView.viewName).isEqualTo("changeEmail")
      assertThat(modelAndView.model["changingUsername"]).isEqualTo(true)
    }
  }

  @Test
  fun newSecondaryEmailRequest() {
    val user =
      createSampleUser(secondaryEmail = "someuser@gmail.com", secondaryEmailVerified = true)
    whenever(userService.getUserWithContacts(token.name)).thenReturn(user)
    val modelAndView = controller.newSecondaryEmailRequest(token)
    assertThat(modelAndView.viewName).isEqualTo("account/changeBackupEmail")
    assertThat(modelAndView.model["secondaryEmail"]).isEqualTo("someuser@gmail.com")
  }

  private fun setupGetUserCallForProfile(): NomisUserPersonDetails {
    val user = createSampleNomisUser()
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))
    return user
  }

  private fun setupGetToken() {
    whenever(tokenService.getToken(any(), anyString()))
      .thenReturn(
        Optional.of(
          createSampleUser(username = "someuser", email = "someuser@justice.gov.uk")
            .createToken(UserToken.TokenType.RESET)
        )
      )
  }
}
