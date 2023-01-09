@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.data.MapEntry
import org.assertj.core.data.MapEntry.entry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType.ACCOUNT
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType.CHANGE
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType.RESET
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType.VERIFIED
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.TokenByEmailTypeRequest
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityNotFoundException

class TokenServiceTest {
  private val userTokenRepository: UserTokenRepository = mock()
  private val userService: UserService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val tokenService = TokenService(userTokenRepository, userService, telemetryClient, 7)

  @Nested
  inner class getToken {
    @Test
    fun `get token notfound`() {
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.empty())
      assertThat(tokenService.getToken(RESET, "token")).isEmpty
    }

    @Test
    fun `get token WrongType`() {
      val userToken = createSampleUser(username = "user").createToken(VERIFIED)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.getToken(RESET, "token")).isEmpty
    }

    @Test
    fun `get token`() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.getToken(RESET, "token")).get().isSameAs(userToken)
    }
  }

  @Nested
  inner class getUserFromToken {
    @Test
    fun `get user from token notfound`() {
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.empty())
      assertThatThrownBy {
        tokenService.getUserFromToken(
          RESET,
          "token"
        )
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `get user from token WrongType`() {
      val userToken = createSampleUser(username = "user").createToken(VERIFIED)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThatThrownBy {
        tokenService.getUserFromToken(
          RESET,
          "token"
        )
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `get user from token token`() {
      val user = createSampleUser(username = "user")
      val userToken = user.createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.getUserFromToken(RESET, "token")).isSameAs(user)
    }
  }

  @Nested
  inner class checkToken {
    @Test
    fun checkToken() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.checkToken(RESET, "token")).isEmpty
    }

    @Test
    fun `checkToken invalid`() {
      val userToken = createSampleUser(username = "user").createToken(CHANGE)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.checkToken(RESET, "token")).get().isEqualTo("invalid")
    }

    @Test
    fun `checkToken expiredTelemetryUsername`() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      userToken.tokenExpiry = LocalDateTime.now().minusHours(1)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      tokenService.checkToken(RESET, "token")
      verify(telemetryClient).trackEvent(
        eq("ResetPasswordFailure"),
        check {
          assertThat(it).containsOnly(entry("username", "user"), entry("reason", "expired"))
        },
        isNull()
      )
    }

    @Test
    fun `checkToken expired`() {
      val userToken = createSampleUser(username = "joe").createToken(RESET)
      userToken.tokenExpiry = LocalDateTime.now().minusHours(1)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.checkToken(RESET, "token")).get().isEqualTo("expired")
    }
  }

  @Nested
  inner class `check token for user` {
    @Test
    fun `empty token`() {
      assertThat(tokenService.checkTokenForUser(ACCOUNT, "", "joe")).hasValue("invalid")
      verifyEventRecorded("ChangeAccountDetailsFailure", entry("reason", "invalid"))
    }

    @Test
    fun `token not found`() {
      val token = "1234-5678-8989-2233"
      whenever(userTokenRepository.findById(token)).thenReturn(Optional.empty())

      assertThat(tokenService.checkTokenForUser(ACCOUNT, token, "bob")).hasValue("invalid")
      verifyEventRecorded("ChangeAccountDetailsFailure", entry("reason", "invalid"))
    }

    @Test
    fun `token expired`() {
      val token = "1234-5678-8989-2233"
      val username = "bob"
      val userToken = createSampleUser(username = username).createToken(ACCOUNT)
      userToken.tokenExpiry = LocalDateTime.now().minusHours(1)
      whenever(userTokenRepository.findById(token)).thenReturn(Optional.of(userToken))

      assertThat(tokenService.checkTokenForUser(ACCOUNT, token, username)).hasValue("expired")
      verifyEventRecorded("ChangeAccountDetailsFailure", entry("reason", "expired"), entry("username", username))
    }

    @Test
    fun `token not issued to user`() {
      val token = "1234-5678-8989-2233"
      val username = "bob"
      val userToken = createSampleUser(username = username).createToken(ACCOUNT)
      whenever(userTokenRepository.findById(token)).thenReturn(Optional.of(userToken))

      assertThat(tokenService.checkTokenForUser(ACCOUNT, token, username + "x")).hasValue("invalid")
      verifyEventRecorded("ChangeAccountDetailsFailure", entry("reason", "invalid"))
      verify(userTokenRepository).delete(userToken)
    }

    @Test
    fun `token ok`() {
      val token = "1234-5678-8989-2233"
      val username = "bob"
      val userToken = createSampleUser(username = username).createToken(ACCOUNT)
      whenever(userTokenRepository.findById(token)).thenReturn(Optional.of(userToken))

      assertThat(tokenService.checkTokenForUser(ACCOUNT, token, username)).isEmpty
      verifyNoInteractions(telemetryClient)
      verify(userTokenRepository, never()).delete(userToken)
    }
  }

  @Nested
  inner class `isValid token` {
    @Test
    fun `isValid with no username`() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.isValid(RESET, "token", null)).isFalse
      verify(userTokenRepository).delete(userToken)
    }

    @Test
    fun `isValid with username match`() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThat(tokenService.isValid(RESET, "token", "user")).isTrue
      verify(userTokenRepository, never()).delete(any())
    }
  }

  @Nested
  inner class createToken {
    @Test
    fun createToken() {
      val user = createSampleUser(username = "joe")
      whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
      val token = tokenService.createToken(RESET, "token")
      assertThat(token).isNotNull()
      assertThat(user.tokens.map { it.token }).contains(token)
    }

    @Test
    fun `createToken check telemetry`() {
      val user = createSampleUser(username = "joe")
      whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
      tokenService.createToken(RESET, "token")
      verify(telemetryClient).trackEvent(
        eq("ResetPasswordRequest"),
        check {
          assertThat(it).containsOnly(entry("username", "token"))
        },
        isNull()
      )
    }
  }

  @Nested
  inner class removeToken {
    @Test
    fun `remove token notfound`() {
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.empty())
      tokenService.removeToken(RESET, "token")
      verify(userTokenRepository, never()).delete(any())
    }

    @Test
    fun `remove token WrongType`() {
      val userToken = createSampleUser(username = "user").createToken(VERIFIED)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      tokenService.removeToken(RESET, "token")
      verify(userTokenRepository, never()).delete(any())
    }

    @Test
    fun `remove token`() {
      val userToken = createSampleUser(username = "user").createToken(RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      tokenService.removeToken(RESET, "token")
      verify(userTokenRepository).delete(userToken)
    }
  }

  private fun verifyEventRecorded(eventName: String, vararg entries: MapEntry<String, String>) {
    verify(telemetryClient).trackEvent(
      eq(eventName),
      check {
        assertThat(it).containsOnly(*entries)
      },
      isNull()
    )
  }
  @Nested
  inner class CreateTokenByEmailType {
    @Test
    fun `Create Token for Email Type`() {
      val tokenByEmailTypeRequest = TokenByEmailTypeRequest("USER", User.EmailType.PRIMARY)
      val user = createSampleUser(username = "user")
      whenever(userService.getUser(anyString())).thenReturn(user)
      val token = tokenService.createTokenByEmailType(tokenByEmailTypeRequest)
      assertThat(token).isNotNull
      verify(userService).getUser("USER")
    }
  }
}
