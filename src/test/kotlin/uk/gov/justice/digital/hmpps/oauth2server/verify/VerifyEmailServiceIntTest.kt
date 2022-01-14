package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.service.notify.NotificationClientApi

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
class VerifyEmailServiceIntTest {
  private lateinit var verifyEmailService: VerifyEmailService
  private val telemetryClient: TelemetryClient = mock()
  private val notificationClient: NotificationClientApi = mock()

  @Autowired
  private lateinit var referenceCodesService: EmailDomainService

  @Autowired
  private lateinit var nomisUserApiService: NomisUserApiService

  @Autowired
  private lateinit var userRepository: UserRepository

  @BeforeEach
  fun setUp() {
    val userTokenRepository: UserTokenRepository = mock()
    verifyEmailService = VerifyEmailService(
      userRepository,
      userTokenRepository,
      telemetryClient,
      notificationClient,
      referenceCodesService,
      nomisUserApiService,
      "templateId"
    )
  }

  @Test
  fun emailAddressSetToNotVerified() {
    val userBefore = userRepository.findByUsername("AUTH_CHANGE_EMAIL")
    assertThat(userBefore.get().verified).isTrue
    verifyEmailService.changeEmailAndRequestVerification(
      "AUTH_CHANGE_EMAIL",
      "phillips@fredjustice.gov.uk",
      "AUTH",
      "full name",
      "url",
      User.EmailType.PRIMARY
    )
    val userAfter = userRepository.findByUsername("AUTH_CHANGE_EMAIL")
    assertThat(userAfter.get().verified).isFalse
  }

  @Test
  fun secondaryEmailAddressSetToNotVerified() {
    val userBefore = userRepository.findByUsername("AUTH_SECOND_EMAIL_CHANGE")
    assertThat(userBefore.get().isSecondaryEmailVerified).isTrue
    verifyEmailService.changeEmailAndRequestVerification(
      "AUTH_CHANGE_EMAIL",
      "phillips@fredjustice.gov.uk",
      "AUTH",
      "full name",
      "url",
      User.EmailType.SECONDARY
    )
    val userAfter = userRepository.findByUsername("AUTH_CHANGE_EMAIL")
    assertThat(userAfter.get().isSecondaryEmailVerified).isFalse
  }
}
