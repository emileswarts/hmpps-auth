package uk.gov.justice.digital.hmpps.oauth2server.verify

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyMobileService.VerifyMobileException
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.util.Optional

class VerifyMobileServiceTest {
  private val userRepository: UserRepository = mock()
  private val userTokenRepository: UserTokenRepository = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val notificationClient: NotificationClientApi = mock()
  private val verifyMobileService =
    VerifyMobileService(userRepository, userTokenRepository, telemetryClient, notificationClient, "templateId")

  @Test
  fun mobile() {
    val user = createSampleUser(username = "bob", mobile = "07700900321")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    val userOptional = verifyMobileService.getMobile("user")
    assertThat(userOptional).get().isEqualTo(user)
  }

  @Test
  fun mobile_NoMobileSet() {
    val user = createSampleUser(username = "bob")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    val userOptionalOptional = verifyMobileService.getMobile("user")
    assertThat(userOptionalOptional).isEmpty
  }

  @Test
  fun isNotVerified_userMissing() {
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
    assertThat(verifyMobileService.isNotVerified("user")).isTrue()
    verify(userRepository).findByUsername("user")
  }

  @Test
  fun isNotVerified_userFoundNotVerified() {
    val user = createSampleUser(username = "bob")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    assertThat(verifyMobileService.isNotVerified("user")).isTrue()
  }

  @Test
  fun isNotVerified_userFoundVerified() {
    val user = createSampleUser(username = "bob", mobile = "07700900321", verified = true)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    assertThat(verifyMobileService.isNotVerified("user")).isFalse()
  }

  @Test
  fun requestVerification_existingToken() {
    val user = createSampleUser(username = "someuser")
    val existingUserToken = user.createToken(TokenType.MOBILE)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    verifyMobileService.changeMobileAndRequestVerification("user", "07700900321")
    assertThat(user.tokens).extracting<String> { it.token }.containsExactly(existingUserToken.token)
  }

  @Test
  fun requestVerification_verifyToken() {
    val user = createSampleUser(username = "someuser")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    val verification = verifyMobileService.changeMobileAndRequestVerification("user", "07700900321")
    val value = user.tokens.stream().findFirst().orElseThrow()
    assertThat(verification).isEqualTo(value.token)
  }

  @Test
  fun requestVerification_saveMobile() {
    val user = createSampleUser(username = "someuser")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    verifyMobileService.changeMobileAndRequestVerification("user", "07700900321")
    verify(userRepository).save(user)
    assertThat(user.mobile).isEqualTo("07700900321")
    assertThat(user.verified).isFalse()
  }

  @Test
  fun requestVerification_sendFailure() {
    val user = createSampleUser(username = "someuser")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    whenever(notificationClient.sendSms(anyString(), anyString(), anyMap<String, Any?>(), isNull())).thenThrow(
      NotificationClientException("message")
    )
    assertThatThrownBy {
      verifyMobileService.changeMobileAndRequestVerification(
        "user",
        "07700900321"
      )
    }.hasMessage("message")
  }

  @Test
  fun requestVerification_formatMobileInput() {
    val user = Optional.of(createSampleUser(username = "someuser"))
    whenever(userRepository.findByUsername(anyString())).thenReturn(user)
    verifyMobileService.changeMobileAndRequestVerification("user", "07700900321")
    verify(notificationClient).sendSms(eq("templateId"), eq("07700900321"), anyMap<String, Any?>(), isNull())
  }

  @Test
  fun `resendVerificationCode send code`() {
    val user = createSampleUser(firstName = "bob", mobile = "07700900321")
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))

    val verifyCode = verifyMobileService.resendVerificationCode("bob")
    val code = user.tokens.first().token
    assertThat(verifyCode).get().isEqualTo(code)

    Mockito.verify(notificationClient).sendSms(eq("templateId"), eq("07700900321"), anyMap<String, Any?>(), isNull())
  }

  @Test
  fun `resendVerificationCodeSecondaryEmail no second email`() {
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(createSampleUser()))
    assertThatThrownBy { verifyMobileService.resendVerificationCode("bob") }
      .isInstanceOf(VerifyMobileException::class.java).extracting("reason").isEqualTo("nomobile")
  }

  @Test
  fun `resendVerificationCodeSecondaryEmail already verified`() {
    val user = createSampleUser(mobile = "07700900321", mobileVerified = true)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    assertThat(verifyMobileService.resendVerificationCode("bob")).isEqualTo(Optional.empty<String>())
  }

  @Test
  fun verifyMobile_Blank() {
    verifyMobileFailure("", "blank")
  }

  @Test
  fun verifyMobile_format() {
    verifyMobileFailure("0", "format")
  }

  private fun verifyMobileFailure(mobile: String, reason: String) {
    assertThatThrownBy { verifyMobileService.validateMobileNumber(mobile) }.isInstanceOf(VerifyMobileException::class.java)
      .extracting("reason").isEqualTo(reason)
  }
}
