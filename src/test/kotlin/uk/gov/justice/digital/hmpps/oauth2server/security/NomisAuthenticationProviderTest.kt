package uk.gov.justice.digital.hmpps.oauth2server.security

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientNetworkService

class NomisAuthenticationProviderTest {

  private val nomisUserApiService: NomisUserApiService = mock()
  private val nomisUserService: NomisUserService = mock()
  private val userRetriesService: UserRetriesService = mock()
  private val mfaClientNetworkService: MfaClientNetworkService = mock()
  private val userService: UserService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val provider = NomisAuthenticationProvider(
    nomisUserApiService,
    NomisUserDetailsService(nomisUserService),
    userRetriesService,
    mfaClientNetworkService,
    userService,
    telemetryClient
  )

  @Test
  fun authenticate_Success() {
    whenever(nomisUserService.getNomisUserByUsername(ArgumentMatchers.anyString())).thenReturn(
      NomisUserPersonDetailsHelper.createSampleNomisUser(username = "ITAG_USER")
    )
    whenever(nomisUserApiService.authenticateUser(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(true)
    val auth = provider.authenticate(UsernamePasswordAuthenticationToken("ITAG_USER", "password"))
    assertThat(auth).isNotNull()
  }

  @Test
  fun authenticate_NullUsername() {
    assertThatThrownBy { provider.authenticate(UsernamePasswordAuthenticationToken(null, "password")) }.isInstanceOf(
      MissingCredentialsException::class.java
    )
  }

  @Test
  fun authenticate_MissingUsername() {
    assertThatThrownBy {
      provider.authenticate(
        UsernamePasswordAuthenticationToken(
          "      ",
          "password"
        )
      )
    }.isInstanceOf(MissingCredentialsException::class.java)
  }

  @Test
  fun authenticate_MissingPassword() {
    assertThatThrownBy { provider.authenticate(UsernamePasswordAuthenticationToken("ITAG_USER", "   ")) }.isInstanceOf(
      MissingCredentialsException::class.java
    )
  }

  @Test
  fun authenticate_LockAfterThreeFailures() {
    whenever(nomisUserService.getNomisUserByUsername(ArgumentMatchers.anyString())).thenReturn(
      NomisUserPersonDetailsHelper.createSampleNomisUser(username = "CA_USER", locked = false)
    )
    whenever(userRetriesService.incrementRetriesAndLockAccountIfNecessary(any())).thenReturn(true)
    assertThatThrownBy { provider.authenticate(UsernamePasswordAuthenticationToken("CA_USER", "wrong")) }.isInstanceOf(
      LockedException::class.java
    )
  }

  @Test
  fun `authenticate ResetAfterSuccess`() {
    val nomisUser = NomisUserPersonDetailsHelper.createSampleNomisUser(username = "ITAG_USER")
    whenever(nomisUserService.getNomisUserByUsername(ArgumentMatchers.anyString())).thenReturn(nomisUser)
    whenever(nomisUserApiService.authenticateUser(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(true)
    provider.authenticate(UsernamePasswordAuthenticationToken("DELIUS_USER", "password"))
    verify(userRetriesService).resetRetriesAndRecordLogin(nomisUser)
  }
}
