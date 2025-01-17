package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthServiceRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.service.notify.NotificationClientApi
import java.util.Map.entry
import java.util.Optional
import javax.persistence.EntityNotFoundException

class InitialPasswordServiceTest {
  private val userRepository: UserRepository = mock()
  private val oauthServiceRepository: OauthServiceRepository = mock()
  private val userService: UserService = mock()
  private val notificationClient: NotificationClientApi = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val initialPasswordService = InitialPasswordService(
    userRepository,
    oauthServiceRepository,
    userService,
    notificationClient,
    "resendTemplate",
    telemetryClient
  )

  @Test
  fun `resend Initial Password Link`() {
    val user = createSampleUser(username = "someuser", firstName = "Bob", lastName = "Smith", email = "email")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(staffUserAccountForBobOptional)
    val service = Service("serviceCode", "service", "service", "ANY_ROLES", "ANY_URL", true, "supportLink")
    whenever(oauthServiceRepository.findById(anyString())).thenReturn(Optional.of(service))
    val link = initialPasswordService.resendInitialPasswordLink("user", "url-expired")
    verify(notificationClient).sendEmail(
      eq("resendTemplate"),
      eq("email"),
      check {
        assertThat(it).containsOnly(
          entry("firstName", "Bob"),
          entry("fullName", "Bob Smith"),
          entry("resetLink", link),
          entry("supportLink", "supportLink")
        )
      },
      isNull()
    )
    assertThat(link).isNotEmpty()
  }

  @Test
  fun `resend Initial Password Link check telemetry`() {
    val user = createSampleUser(username = "someuser", firstName = "Bob", lastName = "Smith", email = "email")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(staffUserAccountForBobOptional)
    val service = Service("serviceCode", "service", "service", "ANY_ROLES", "ANY_URL", true, "supportLink")
    whenever(oauthServiceRepository.findById(anyString())).thenReturn(Optional.of(service))
    initialPasswordService.resendInitialPasswordLink("user", "url-expired")
    verify(telemetryClient).trackEvent("reissueInitialPasswordLink", mapOf("username" to "someuser"), null)
  }

  @Test
  fun `resend Initial Password Link User not found`() {
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
    assertThatThrownBy { initialPasswordService.resendInitialPasswordLink("user", "url-expired") }.isInstanceOf(
      EntityNotFoundException::class.java
    )
  }

  @Test
  fun `resend Initial Password Link Master User not found`() {
    val user =
      createSampleUser(username = "someuser", firstName = "Bob", lastName = "Smith", email = "email", source = AuthSource.nomis)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.empty())
    assertThatThrownBy { initialPasswordService.resendInitialPasswordLink("user", "url-expired") }.isInstanceOf(
      EntityNotFoundException::class.java
    )
  }

  private val staffUserAccountForBob: UserPersonDetails
    get() = createSampleNomisUser(firstName = "bOb", lastName = "Smith", userId = "5")

  private val staffUserAccountForBobOptional: Optional<UserPersonDetails> = Optional.of(staffUserAccountForBob)
}
