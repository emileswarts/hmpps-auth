package uk.gov.justice.digital.hmpps.oauth2server.security

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.oauth2server.security.LockingAuthenticationProvider.MfaRequiredException
import uk.gov.justice.digital.hmpps.oauth2server.security.LockingAuthenticationProvider.MfaUnavailableException
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientNetworkService

class LockingAuthenticationProviderTest {
  private val userRetriesService: UserRetriesService = mock()
  private val userDetailsService: AuthUserDetailsService = mock()
  private val userService: UserService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val mfaClientNetworkService: MfaClientNetworkService = mock()

  private val lockingAuthenticationProvider =
    AuthAuthenticationProvider(userDetailsService, userRetriesService, mfaClientNetworkService, userService, telemetryClient)

  @Test
  fun `authenticate nomisUser`() { // test that oracle passwords are authenticated okay
    setupLoadUser("S:39BA463D55E5C8936A6798CC37B1347BA8BEC37B6407397EB769BC356F0C")
    lockingAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken("user", "somepass1"))
  }
  @Test
  fun `authenticate verify telemetry event`() { // test that oracle passwords are authenticated okay
    setupLoadUser("S:39BA463D55E5C8936A6798CC37B1347BA8BEC37B6407397EB769BC356F0C")
    lockingAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken("user", "somepass1"))
    verify(telemetryClient).trackEvent("AuthenticateSuccess", mapOf("username" to "user", "authSource" to "none"), null)
  }

  @Test
  fun `authenticate authUser`() {
    setupLoadUser("{bcrypt}${BCryptPasswordEncoder().encode("some_pass")}")
    lockingAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken("user", "some_pass"))
  }

  @Test
  fun `authenticate authUser needs MFA`() {
    whenever(mfaClientNetworkService.needsMfa(any())).thenReturn(true)
    whenever(userService.hasVerifiedMfaMethod(any())).thenReturn(true)

    setupLoadUser("{bcrypt}${BCryptPasswordEncoder().encode("some_pass")}")

    assertThatThrownBy {
      lockingAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken("user", "some_pass"))
    }.isInstanceOf(MfaRequiredException::class.java)
  }

  @Test
  fun `authenticate authUser MFA unavailable`() {
    whenever(mfaClientNetworkService.needsMfa(any())).thenReturn(true)

    setupLoadUser("{bcrypt}${BCryptPasswordEncoder().encode("some_pass")}")

    assertThatThrownBy {
      lockingAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken("user", "some_pass"))
    }.isInstanceOf(MfaUnavailableException::class.java)
  }

  private fun setupLoadUser(password: String) {
    val userDetails = UserDetailsImpl("user", "name", emptyList(), "none", "user", "jwtId")
    ReflectionTestUtils.setField(userDetails, "password", password)
    whenever(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails)
  }
}
