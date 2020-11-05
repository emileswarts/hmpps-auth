package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.authentication.TestingAuthenticationToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Contact
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ContactType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
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
    fun newEmailRequest() {
      setupGetToken()
      setupGetUserCallForProfile()
      val view = controller.newEmailRequest("token")
      assertThat(view.viewName).isEqualTo("changeEmail")
      val model = controller.newEmailRequest("token")
      assertThat(model.model["email"]).isEqualTo("someuser@justice.gov.uk")
    }

    @Test
    fun newSecondaryEmailRequest() {
      val user =
        User.builder().contacts(setOf(Contact(ContactType.SECONDARY_EMAIL, "someuser@gmail.com", true))).build()
      whenever(userService.getUserWithContacts(token.name)).thenReturn(user)
      val view = controller.newSecondaryEmailRequest(token)
      assertThat(view.viewName).isEqualTo("account/changeBackupEmail")
      val model = controller.newSecondaryEmailRequest(token)
      assertThat(model.model["secondaryEmail"]).isEqualTo("someuser@gmail.com")
    }
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
          User.builder().username("someuser").email("someuser@justice.gov.uk").build()
            .createToken(UserToken.TokenType.RESET)
        )
      )
  }
}
