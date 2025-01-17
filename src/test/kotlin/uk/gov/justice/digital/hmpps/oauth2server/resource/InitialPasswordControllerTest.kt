package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.InitialPasswordService
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordService
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import java.util.Optional
import javax.servlet.http.HttpServletRequest

class InitialPasswordControllerTest {
  private val resetPasswordService: ResetPasswordService = mock()
  private val initialPasswordService: InitialPasswordService = mock()
  private val tokenService: TokenService = mock()
  private val userService: UserService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val request: HttpServletRequest = mock()

  private val controller = InitialPasswordController(
    resetPasswordService,
    initialPasswordService,
    tokenService,
    userService,
    telemetryClient,
    setOf("password1"),
    true
  )

  @Nested
  inner class InitialPasswordSuccess {
    @Test
    fun initialPasswordSuccess_checkView() {
      val modelAndView = controller.initialPasswordSuccess()
      assertThat(modelAndView).isEqualTo("initialPasswordSuccess")
    }
  }

  @Nested
  inner class InitialPassword {
    @Test
    fun `initialPassword no token`() {
      val modelAndView = controller.initialPassword("  ", request)
      assertThat(modelAndView.viewName).isEqualTo("redirect:/reset-password")
      verifyNoInteractions(tokenService)
    }

    @Test
    fun initialPassword_checkView() {
      setupGetUserCallForProfile()
      setupCheckAndGetTokenValid()
      val modelAndView = controller.initialPassword("token", request)
      assertThat(modelAndView.viewName).isEqualTo("setPassword")
    }

    @Test
    fun initialPassword_checkModel() {
      setupCheckAndGetTokenValid()
      setupGetUserCallForProfile()
      val modelAndView = controller.initialPassword("sometoken", request)
      assertThat(modelAndView.model).containsExactlyInAnyOrderEntriesOf(
        mapOf(
          "token" to "sometoken",
          "isAdmin" to false,
          "initial" to true,
          "username" to "someuser"
        )
      )
    }

    @Test
    fun initialPassword_checkModelAdminUser() {
      val user = createSampleNomisUser("TAG_ADMIN", admin = true)
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))
      setupCheckAndGetTokenValid()
      val modelAndView = controller.initialPassword("sometoken", request)
      assertThat(modelAndView.model).containsExactlyInAnyOrderEntriesOf(
        mapOf(
          "token" to "sometoken",
          "isAdmin" to true,
          "initial" to true,
          "username" to "someuser"
        )
      )
    }

    @Test
    fun initialPassword_FailureExpiredCheckView() {
      whenever(request.requestURL).thenReturn(StringBuffer("someurl"))
      whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.of("expired"))
      val modelAndView = controller.initialPassword("token", request)
      assertThat(modelAndView.viewName).isEqualTo("redirect:/initial-password-expired")
    }

    @Test
    fun initialPassword_FailureInvalidCheckView() {
      whenever(request.requestURL).thenReturn(StringBuffer("someurl"))
      whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.of("invalid"))
      val modelAndView = controller.initialPassword("token", request)
      assertThat(modelAndView.viewName).isEqualTo("redirect:/reset-password")
    }

    @Test
    fun initialPassword_FailureCheckModel() {
      whenever(request.requestURL).thenReturn(StringBuffer("someurl"))
      whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.of("expired"))
      val modelAndView = controller.initialPassword("sometoken", request)
      assertThat(modelAndView.model).containsOnly(entry("token", "sometoken"))
    }
  }

  @Nested
  inner class InitialPasswordLinkExpired {
    @Test
    fun initialPasswordLinkExpired_checkView() {
      whenever(request.requestURL).thenReturn(StringBuffer("someurl"))
      whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.of("expired"))
      whenever(tokenService.getUserFromToken(any(), anyString())).thenReturn(createSampleUser(username = "bob"))
      whenever(initialPasswordService.resendInitialPasswordLink(anyString(), anyString())).thenReturn("newToken")
      val modelAndView = controller.initialPasswordLinkExpired("sometoken", request)
      assertThat(modelAndView.viewName).isEqualTo("createPasswordExpired")
    }

    @Test
    fun initialPasswordLinkExpired_checkModel() {
      whenever(request.requestURL).thenReturn(StringBuffer("someurl"))
      whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.of("expired"))
      whenever(tokenService.getUserFromToken(any(), anyString())).thenReturn(createSampleUser(username = "bob"))
      whenever(initialPasswordService.resendInitialPasswordLink(anyString(), anyString())).thenReturn("newToken")
      val modelAndView = controller.initialPasswordLinkExpired("sometoken", request)
      assertThat(modelAndView.model).containsOnly(entry("link", "newToken"))
    }
  }

  private fun setupCheckAndGetTokenValid() {
    whenever(tokenService.checkToken(any(), anyString())).thenReturn(Optional.empty())
    whenever(tokenService.getToken(any(), anyString())).thenReturn(
      Optional.of(
        createSampleUser(username = "someuser").createToken(UserToken.TokenType.RESET)
      )
    )
  }

  private fun setupGetUserCallForProfile(): NomisUserPersonDetails {
    val user = createSampleNomisUser("TAG_GENERAL")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))
    return user
  }
}
