package uk.gov.justice.digital.hmpps.oauth2server.resource.user

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.authentication.*
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.resource.ExistingPasswordController
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.DeliusAuthenticationServiceException
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService

class ExistingPasswordControllerTest {
  private val authenticationManager: AuthenticationManager = mock()
  private val tokenService: TokenService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val controller = ExistingPasswordController(authenticationManager, tokenService, telemetryClient)
  private val token = TestingAuthenticationToken(UserDetailsImpl("user", "name", setOf(), AuthSource.auth.name, null), "pass")

  @Nested
  inner class ExistingPasswordRequest {
    @Test
    fun request() {
      val view = controller.existingPasswordRequest(token)
      assertThat(view.viewName).isEqualTo("user/existingPassword")
    }
  }

  @Nested
  inner class ExistingPassword {
    @Test
    fun `password null`() {
      val mandv = controller.existingPassword(null, token)
      assertThat(mandv.viewName).isEqualTo("user/existingPassword")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to "required", "username" to "user"))
    }

    @Test
    fun `password blank`() {
      val mandv = controller.existingPassword("    ", token)
      assertThat(mandv.viewName).isEqualTo("user/existingPassword")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to "required", "username" to "user"))
    }

    @Test
    fun `authenticate success`() {
      whenever(authenticationManager.authenticate(any())).thenReturn(token)
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      val mandv = controller.existingPassword("somepass", token)
      assertThat(mandv.viewName).isEqualTo("redirect:/new-password")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("token" to "sometoken"))

    }

    @Test
    fun `authenticate success create token`() {
      whenever(authenticationManager.authenticate(any())).thenReturn(token)
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      controller.existingPassword("somepass", token)
      verify(tokenService).createToken(UserToken.TokenType.CHANGE, "user")
    }

    @Test
    fun `authenticate success call authenticate`() {
      whenever(authenticationManager.authenticate(any())).thenReturn(token)
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      controller.existingPassword("somepass", token)
      verify(authenticationManager).authenticate(check {
        assertThat(it.credentials).isEqualTo("somepass")
        assertThat(it.principal).isEqualTo("USER")
      })
    }

    @Test
    fun `authenticate failure delius down`() {
      whenever(authenticationManager.authenticate(any())).thenThrow(DeliusAuthenticationServiceException())
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      val mandv = controller.existingPassword("somepass", token)
      assertThat(mandv.viewName).isEqualTo("user/existingPassword")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to listOf("invalid", "deliusdown"), "username" to "user"))
    }

    @Test
    fun `authenticate failure bad credentials`() {
      whenever(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("some bad message"))
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      val mandv = controller.existingPassword("somepass", token)
      assertThat(mandv.viewName).isEqualTo("user/existingPassword")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to "invalid", "username" to "user"))
    }

    @Test
    fun `authenticate failure locked`() {
      whenever(authenticationManager.authenticate(any())).thenThrow(LockedException("some locked message"))
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      val mandv = controller.existingPassword("somepass", token)
      assertThat(mandv.viewName).isEqualTo("redirect:/logout")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to "locked"))
    }

    @Test
    fun `authenticate failure other`() {
      whenever(authenticationManager.authenticate(any())).thenThrow(CredentialsExpiredException("some expired message"))
      whenever(tokenService.createToken(any(), anyString())).thenReturn("sometoken")
      val mandv = controller.existingPassword("somepass", token)
      assertThat(mandv.viewName).isEqualTo("redirect:/logout")
      assertThat(mandv.model).containsExactlyInAnyOrderEntriesOf(mapOf("error" to "invalid"))
    }
  }
}
