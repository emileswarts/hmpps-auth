@file:Suppress("ClassName", "DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Person
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.MfaPreferenceType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType
import uk.gov.justice.digital.hmpps.oauth2server.security.LockingAuthenticationProvider.MfaUnavailableException
import uk.gov.justice.digital.hmpps.oauth2server.security.UserRetriesService
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService
import uk.gov.service.notify.NotificationClientApi
import java.util.Optional

internal class MfaServiceTest {
  private val tokenService: TokenService = mock()
  private val userService: UserService = mock()
  private val userRetriesService: UserRetriesService = mock()
  private val notificationClientApi: NotificationClientApi = mock()
  private val service = MfaService(
    "emailTemplate",
    "textTemplate",
    tokenService,
    userService,
    notificationClientApi,
    userRetriesService,
  )

  @Test
  fun `validateAndRemoveMfaCode null`() {
    assertThatThrownBy { service.validateAndRemoveMfaCode("", null, "user") }.isInstanceOf(MfaFlowException::class.java)
      .withFailMessage("missingcode")
  }

  @Test
  fun `validateAndRemoveMfaCode blank`() {
    assertThatThrownBy { service.validateAndRemoveMfaCode("", "   ", "user") }.isInstanceOf(MfaFlowException::class.java)
      .withFailMessage("missingcode")
  }

  @Test
  fun `validateAndRemoveMfaCode token error`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))

    whenever(tokenService.checkTokenForUser(any(), anyString(), eq("user"))).thenReturn(Optional.of("someproblem"))
    assertThatThrownBy { service.validateAndRemoveMfaCode("", "somecode", "user") }.isInstanceOf(MfaFlowException::class.java)
      .withFailMessage("someproblem")
  }

  @Test
  fun `validateAndRemoveMfaCode success`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")
  }

  @Test
  fun `validateAndRemoveMfaCode success get token call`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")

    verify(tokenService).getToken(TokenType.MFA, "sometoken")
  }

  @Test
  fun `validateAndRemoveMfaCode success check token call`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")

    verify(tokenService).checkTokenForUser(TokenType.MFA_CODE, "somecode", "user")
  }

  @Test
  fun `validateAndRemoveMfaCode success find master details`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")

    verify(userService).findMasterUserPersonDetails("user")
  }

  @Test
  fun `validateAndRemoveMfaCode success remove tokens`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")

    verify(tokenService).removeToken(TokenType.MFA, "sometoken")
    verify(tokenService).removeToken(TokenType.MFA_CODE, "somecode")
  }

  @Test
  fun `validateAndRemoveMfaCode success reset retries `() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser(username = "bob")))
    service.validateAndRemoveMfaCode("sometoken", "somecode", "user")

    verify(userRetriesService).resetRetries("bob")
  }

  @Test
  fun `validateAndRemoveMfaCode account locked`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(
      Optional.of(createSampleUser(username = "bob", locked = true))
    )
    assertThatThrownBy {
      service.validateAndRemoveMfaCode("sometoken", "somecode", "user")
    }.isInstanceOf(LoginFlowException::class.java).withFailMessage("locked")
  }

  @Test
  fun `createTokenAndSendMfaCode success`() {
    val user = createSampleUser(username = "bob", email = "email", verified = true)
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    assertThat(service.createTokenAndSendMfaCode("user")).isEqualTo(
      MfaData(
        "sometoken",
        "somecode",
        MfaPreferenceType.EMAIL
      )
    )
  }

  @Test
  fun `createTokenAndSendMfaCode by Email check email params`() {
    val user = createSampleUser(username = "bob", email = "email", verified = true)
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "email",
      mapOf("firstName" to "first", "code" to "somecode"),
      null
    )
  }

  @Test
  fun `createTokenAndSendMfaCode by text check text params`() {
    val user =
      createSampleUser(
        username = "bob",
        mobile = "07700900321",
        mobileVerified = true,
        mfaPreference = MfaPreferenceType.TEXT
      )
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")

    verify(notificationClientApi).sendSms("textTemplate", "07700900321", mapOf("mfaCode" to "somecode"), null, null)
  }

  @Test
  fun `createTokenAndSendMfaCode by secondary Email check secondary email params`() {
    val user = createSampleUser(
      username = "first",
      email = "bob@digital.justice.gov.uk",
      verified = false,
      mobile = "07700900321",
      secondaryEmail = "secondaryEmail",
      secondaryEmailVerified = true,
      mfaPreference = MfaPreferenceType.SECONDARY_EMAIL,
    )
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "secondaryEmail",
      mapOf("firstName" to "first", "code" to "somecode"),
      null
    )
  }

  @Test
  fun `createTokenAndSendMfaCode MfaPreference email unverified code sent to text`() {
    val user = createSampleUser(
      username = "first",
      email = "bob@digital.justice.gov.uk",
      verified = false,
      mobile = "07700900321",
      mobileVerified = true,
      secondaryEmail = "secondaryEmail",
      secondaryEmailVerified = true,
      mfaPreference = MfaPreferenceType.EMAIL
    )
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")
    verify(notificationClientApi).sendSms("textTemplate", "07700900321", mapOf("mfaCode" to "somecode"), null, null)
  }

  @Test
  fun `createTokenAndSendMfaCode MfaPreference text unverified code sent to email`() {
    val user = createSampleUser(
      username = "first",
      email = "bob@digital.justice.gov.uk",
      verified = true,
      mobile = "07700900321",
      secondaryEmail = "secondaryEmail",
      secondaryEmailVerified = true,
      mfaPreference = MfaPreferenceType.TEXT
    )
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "bob@digital.justice.gov.uk",
      mapOf("firstName" to "first", "code" to "somecode"),
      null
    )
  }

  @Test
  fun `createTokenAndSendMfaCode MfaPreference secondary email unverified code sent to email`() {
    val user = createSampleUser(
      username = "first",
      email = "bob@digital.justice.gov.uk",
      verified = true,
      mobile = "07700900321",
      mobileVerified = true,
      secondaryEmail = "secondaryEmail",
      mfaPreference = MfaPreferenceType.SECONDARY_EMAIL
    )
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.createTokenAndSendMfaCode("user")

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "bob@digital.justice.gov.uk",
      mapOf("firstName" to "first", "code" to "somecode"),
      null
    )
  }

  @Test
  fun `createTokenAndSendMfaCode no valid preference`() {
    val user = createSampleUser(username = "bob")
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(user))
    whenever(tokenService.createToken(eq(TokenType.MFA), anyString())).thenReturn("sometoken")
    whenever(tokenService.createToken(eq(TokenType.MFA_CODE), anyString())).thenReturn("somecode")
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    assertThatThrownBy { service.createTokenAndSendMfaCode("user") }.isInstanceOf(MfaUnavailableException::class.java)
  }

  @Test
  fun `resendMfaCode no code`() {
    val userToken = createSampleUser(username = "user").createToken(TokenType.MFA)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))

    val code = service.resendMfaCode("sometoken", MfaPreferenceType.EMAIL)
    assertThat(code).isEqualTo(null)
    verify(userService, never()).findMasterUserPersonDetails(anyString())
  }

  @Test
  fun `resendMfaCode check code`() {
    val user = createSampleUser(username = "user", email = "email")
    val userToken = user.createToken(TokenType.MFA)
    val userCode = user.createToken(TokenType.MFA_CODE)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    assertThat(service.resendMfaCode("sometoken", MfaPreferenceType.EMAIL)).isEqualTo(userCode.token)
  }

  @Test
  fun `resendMfaCode check email`() {
    val user = createSampleUser(username = "user", person = Person("Bob", "Smith"), email = "email")
    val userToken = user.createToken(TokenType.MFA)
    val userCode = user.createToken(TokenType.MFA_CODE)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.resendMfaCode("sometoken", MfaPreferenceType.EMAIL)

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "email",
      mapOf("firstName" to "Bob", "code" to userCode.token),
      null
    )
  }

  @Test
  fun `resendMfaCode check secondary email`() {
    val user = createSampleUser(
      username = "user",
      person = Person("Bob", "Smith"),
      secondaryEmail = "email",
      secondaryEmailVerified = true
    )
    val userToken = user.createToken(TokenType.MFA)
    val userCode = user.createToken(TokenType.MFA_CODE)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.resendMfaCode("sometoken", MfaPreferenceType.SECONDARY_EMAIL)

    verify(notificationClientApi).sendEmail(
      "emailTemplate",
      "email",
      mapOf("firstName" to "Bob", "code" to userCode.token),
      null
    )
  }

  @Test
  fun `resendMfaCode check Text`() {
    val user = createSampleUser(mobile = "07700900321", mobileVerified = true, mfaPreference = MfaPreferenceType.TEXT)
    val userToken = user.createToken(TokenType.MFA)
    val userCode = user.createToken(TokenType.MFA_CODE)
    whenever(tokenService.getToken(any(), anyString())).thenReturn(Optional.of(userToken))
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))

    service.resendMfaCode("sometoken", MfaPreferenceType.TEXT)

    verify(notificationClientApi).sendSms("textTemplate", "07700900321", mapOf("mfaCode" to userCode.token), null, null)
  }

  @Test
  fun `Update User Mfa Preference to text`() {
    val user = createSampleUser(username = "user")
    whenever(userService.findUser(anyString())).thenReturn(Optional.of(user))
    service.updateUserMfaPreference(MfaPreferenceType.TEXT, "user")
    assertThat(user.mfaPreference).isEqualTo(MfaPreferenceType.TEXT)
    verify(userService).findUser("user")
  }

  @Test
  fun `Update User Mfa Preference to email`() {
    val user = createSampleUser(username = "user", mfaPreference = MfaPreferenceType.TEXT)
    whenever(userService.findUser(anyString())).thenReturn(Optional.of(user))
    service.updateUserMfaPreference(MfaPreferenceType.EMAIL, "user")
    assertThat(user.mfaPreference).isEqualTo(MfaPreferenceType.EMAIL)
    verify(userService).findUser("user")
  }

  @Test
  fun `Update User Mfa Preference to secondary email`() {
    val user = createSampleUser(username = "user", mfaPreference = MfaPreferenceType.TEXT)
    whenever(userService.findUser(anyString())).thenReturn(Optional.of(user))
    service.updateUserMfaPreference(MfaPreferenceType.SECONDARY_EMAIL, "user")
    assertThat(user.mfaPreference).isEqualTo(MfaPreferenceType.SECONDARY_EMAIL)
    verify(userService).findUser("user")
  }
}
