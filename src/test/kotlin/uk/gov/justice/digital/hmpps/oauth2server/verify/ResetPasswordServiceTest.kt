package uk.gov.justice.digital.hmpps.oauth2server.verify

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.LockedException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Person
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserTokenRepository
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.azuread
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.delius
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.service.DelegatingUserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordServiceImpl.ResetPasswordException
import uk.gov.service.notify.NotificationClientApi
import uk.gov.service.notify.NotificationClientException
import java.util.Map.entry
import java.util.Optional

class ResetPasswordServiceTest {
  private val userRepository: UserRepository = mock()
  private val userTokenRepository: UserTokenRepository = mock()
  private val userService: UserService = mock()
  private val delegatingUserService: DelegatingUserService = mock()
  private val notificationClient: NotificationClientApi = mock()
  private val resetPasswordService = ResetPasswordServiceImpl(
    userRepository, userTokenRepository,
    userService, delegatingUserService, notificationClient,
    "resetTemplate", "resetUnavailableTemplate", "resetUnavailableEmailNotFoundTemplate", "reset-confirm"
  )

  @Nested
  inner class requestResetPassword {
    @Test
    fun requestResetPassword_noUserEmail() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.empty())
      val optional = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_noEmail() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(createSampleUser(username = "user")))
      val optional = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_noEmail_emailInDelius() {
      val user = createSampleUser(
        username = "someuser",
        email = "email",
        person = Person("Bob", "Smith"),
        enabled = true
      )
      whenever(userRepository.findByEmail(any())).thenReturn(listOf(user, user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)

      val optional = resetPasswordService.requestResetPassword("email@address", "http://url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optional.get())
          )
        },
        isNull()
      )
      assertThat(optional).isPresent
    }

    @Test
    fun requestResetPassword_noNomisUser() {
      val user = createSampleUser(username = "USER", email = "email", verified = true, source = nomis)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.empty())
      val optional = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "first"),
            entry("fullName", "first last"),
            entry("nomisUser", true),
            entry("deliusUser", false),
            entry("authUser", false)
          )
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_noDeliusUser() {
      val user = createSampleUser(username = "USER", email = "email", verified = true, source = delius)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.empty())
      whenever(userService.getMasterUserPersonDetails(anyString(), any())).thenReturn(Optional.empty())
      val optional = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "first"),
            entry("fullName", "first last"),
            entry("deliusUser", true),
            entry("nomisUser", false),
            entry("authUser", false)
          )
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_emailAddressDifferentInDelius() {
      val user = createSampleUser(
        username = "USER",
        email = "email",
        person = Person("Bob", "Smith"),
        enabled = true,
        source = delius
      )
      val userFromDelius = createSampleUser(
        username = "USER",
        email = "emailFromDelius",
        person = Person("Bob", "Smith"),
        enabled = true,
        source = delius
      )
      whenever(userRepository.findByEmail(any())).thenReturn(listOf(user, user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)
      whenever(userService.getMasterUserPersonDetails(anyString(), any())).thenReturn(Optional.of(userFromDelius))

      val optionalLink = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("emailFromDelius"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun requestResetPassword_inactive() {
      val user = createSampleUser(username = "someuser", email = "email", source = nomis, verified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val staffUserAccount =
        nomisUserPersonDetails(AccountStatus.EXPIRED_LOCKED, enabled = false, active = false)
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccount)
      val optional = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("nomisUser", true),
            entry("deliusUser", false),
            entry("authUser", false)
          )
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_authLocked() {
      val user = createSampleUser(
        username = "someuser",
        email = "email",
        verified = true,
        locked = true,
        firstName = "Bob",
        lastName = "Smith",
        enabled = true
      )
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)

      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun requestResetPassword_notAuthLocked() {
      val user = createSampleUser(username = "someuser", email = "email", verified = true, source = nomis)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(
        staffUserAccountLockedForBob
      )
      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun requestResetPassword_userLocked() {
      val user = createSampleUser(username = "someuser", email = "email", source = nomis, verified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(
        staffUserAccountExpiredLockedForBob
      )
      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun requestResetPassword_existingToken() {
      val user = createSampleUser(username = "someuser", email = "email", verified = true, locked = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(staffUserAccountForBobOptional)
      val existingUserToken = user.createToken(UserToken.TokenType.RESET)
      resetPasswordService.requestResetPassword("user", "url")
      assertThat(user.tokens).extracting<String> { it.token }.containsExactly(existingUserToken.token)
    }

    @Test
    fun requestResetPassword_verifyToken() {
      val user = createSampleUser(
        username = "someuser",
        person = Person("Bob", "Smith"),
        email = "email",
        locked = true,
        source = nomis
      )
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun requestResetPassword_uppercaseUsername() {
      val user = createSampleUser(username = "SOMEUSER", email = "email", locked = true, source = nomis)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      resetPasswordService.requestResetPassword("someuser", "url")
      verify(userRepository).findByUsername("SOMEUSER")
      verify(userService).findEnabledOrNomisLockedUserPersonDetails("SOMEUSER")
    }

    @Test
    fun requestResetPassword_verifyNotification() {
      val user = createSampleUser(username = "someuser", email = "email", locked = true, source = nomis)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      val linkOptional = resetPasswordService.requestResetPassword("user", "url")
      val value = user.tokens.stream().findFirst().orElseThrow()
      assertThat(linkOptional).get().isEqualTo(String.format("url-confirm?token=%s", value.token))
      assertThat(value.tokenType).isEqualTo(UserToken.TokenType.RESET)
      assertThat(value.user.email).isEqualTo("email")
    }

    @Test
    fun requestResetPassword_sendFailure() {
      val user = createSampleUser(username = "someuser", email = "email", locked = true, source = nomis)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      whenever(notificationClient.sendEmail(anyString(), anyString(), anyMap<String, Any?>(), isNull())).thenThrow(
        NotificationClientException("message")
      )
      assertThatThrownBy {
        resetPasswordService.requestResetPassword(
          "user",
          "url"
        )
      }.hasMessageContaining("NotificationClientException: message")
    }

    @Test
    fun requestResetPassword_emailAddressNotFound() {
      whenever(userRepository.findByEmail(any())).thenReturn(emptyList())
      val optional = resetPasswordService.requestResetPassword("someuser@somewhere", "url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableEmailNotFoundTemplate"),
        eq("someuser@somewhere"),
        check {
          assertThat(it).isEmpty()
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_emailAddressNotFound_formatEmailInput() {
      whenever(userRepository.findByEmail(any())).thenReturn(emptyList())
      val optional = resetPasswordService.requestResetPassword("some.u’ser@SOMEwhere", "url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableEmailNotFoundTemplate"),
        eq("some.u'ser@somewhere"),
        check {
          assertThat(it).isEmpty()
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_multipleEmailAddresses() {
      val user = createSampleUser(
        username = "someuser",
        email = "email",
        person = Person("Bob", "Smith"),
        enabled = true
      )
      whenever(userRepository.findByEmail(any())).thenReturn(listOf(user, user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)

      val optional = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optional.get())
          )
        },
        isNull()
      )
      assertThat(optional).isPresent
    }

    @Test
    fun requestResetPassword_multipleEmailAddresses_verifyToken() {
      val user = createSampleUser(
        username = "someuser",
        email = "email",
        person = Person("Bob", "Smith"),
        enabled = true
      )
      whenever(userRepository.findByEmail(any())).thenReturn(listOf(user, user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)
      val linkOptional = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")
      val userToken = user.tokens.stream().findFirst().orElseThrow()
      assertThat(linkOptional).get().isEqualTo(String.format("http://url-select?token=%s", userToken.token))
    }

    @Test
    fun requestResetPassword_multipleEmailAddresses_noneCanBeReset() {
      val user =
        createSampleUser(username = "someuser", email = "email", locked = true, person = Person("Bob", "Smith"))
      whenever(userRepository.findByEmail(any())).thenReturn(listOf(user, user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)
      val optional = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("nomisUser", false),
            entry("deliusUser", false),
            entry("authUser", true)
          )
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun requestResetPassword_byEmail() {
      val user = createSampleUser(username = "someuser", email = "email", locked = true, source = nomis)
      whenever(userRepository.findByEmail(anyString())).thenReturn(listOf(user))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      val optionalLink = resetPasswordService.requestResetPassword("user@where", "url")
      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("email"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent
    }

    @Test
    fun `Azure User can not reset password`() {
      val userPersonDetails = buildStandardUser("user")
      val user = createSampleUser(username = "someuser", email = "email", verified = true, source = azuread)
      whenever(userRepository.findByEmail(anyString())).thenReturn(listOf(user))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(userPersonDetails))
      assertThatThrownBy {
        resetPasswordService.requestResetPassword("azureuser@justice.gov.uk", "url")
      }
        .isInstanceOf(ResetPasswordException::class.java)
        .withFailMessage("User password not stored in this system.")
    }

    @Test
    fun `Nomis User who has not logged into auth can reset password`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      val standardUser = buildStandardUser("user")
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(standardUser))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(standardUser)
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.of("Bob.smith@justice.gov.uk"))
      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optionalLink).isPresent
      verify(userRepository).save(
        check { user ->
          assertThat(user.username).isEqualTo("user")
          assertThat(user.email).isEqualTo("Bob.smith@justice.gov.uk")
          assertThat(user.verified).isTrue
          assertThat(user.source).isEqualTo(nomis)
        }
      )
    }

    @Test
    fun `Nomis User who has not logged into DPS can reset password with email`() {
      val userPersonDetails = buildStandardUser("user")
      val user = createSampleUser(
        username = "someuser",
        email = "a@b.com",
        person = Person("Bob", "Smith"),
        enabled = true,
        source = nomis,
        verified = true
      )

      whenever(userRepository.findByEmail(anyString())).thenReturn(listOf(), listOf(user))
      whenever(userService.findUserPersonDetailsByEmail(anyString(), eq(nomis))).thenReturn(
        listOf(
          createSampleNomisUser("user", email = "Bob.smith@justice.gov.uk", firstName = "Bob", lastName = "Smith")
        )
      )
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.of("Bob.smith@justice.gov.uk"))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(userPersonDetails))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(userPersonDetails)

      val optionalLink = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")

      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("a@b.com"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "Bob"),
            entry("fullName", "Bob Smith"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent

      verify(userRepository).save(
        check {
          assertThat(it.username).isEqualTo("user")
          assertThat(it.email).isEqualTo("Bob.smith@justice.gov.uk")
          assertThat(it.verified).isTrue
          assertThat(it.source).isEqualTo(nomis)
        }
      )
    }

    @Test
    fun `User who has not logged into DPS that has accounts in both nomis and delius (with the same email) cannot reset password with email`() {
      val nomisUserDetails = buildStandardUser("user")
      val deliusUserDetails = createDeliusUser()

      whenever(userRepository.findByEmail(anyString())).thenReturn(listOf()) // no user in auth
      whenever(userService.findUserPersonDetailsByEmail(anyString(), eq(nomis))).thenReturn(listOf(nomisUserDetails))
      whenever(userService.findUserPersonDetailsByEmail(anyString(), eq(delius))).thenReturn(listOf(deliusUserDetails))
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.of("Bob.smith@justice.gov.uk"))
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(nomisUserDetails))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(nomisUserDetails)

      val optionalLink = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")

      verify(notificationClient).sendEmail(
        eq("resetUnavailableEmailNotFoundTemplate"),
        eq("someuser@somewhere"),
        check {
          assertThat(it).isEmpty()
        },
        isNull()
      )
      assertThat(optionalLink).isEmpty
      verify(userRepository, never()).save(any())
    }

    @Test
    fun `Nomis User who has not logged reset password request no email`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(buildStandardUser("user")))
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.empty())
      val optional = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optional).isEmpty
    }

    @Test
    fun `Delius User who has not logged into DPS reset password request`() {
      val deliusUser = createDeliusUser()
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(deliusUser))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(deliusUser)
      val optionalLink = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optionalLink).isPresent
    }

    @Test
    fun `Delius User who has not logged into DPS can reset password request with email`() {
      val deliusUserDetails = createDeliusUser()
      val user = createSampleUser(
        username = "someuser",
        email = "a@b.com",
        person = Person("Bob", "Smith"),
        enabled = true,
        source = delius,
        verified = true
      )
      whenever(userRepository.findByEmail(anyString())).thenReturn(listOf(), listOf(user)) // no user in auth
      whenever(userService.findUserPersonDetailsByEmail(anyString(), eq(delius))).thenReturn(listOf(deliusUserDetails))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(deliusUserDetails)

      val optionalLink = resetPasswordService.requestResetPassword("a@b.com", "http://url")

      verify(notificationClient).sendEmail(
        eq("resetTemplate"),
        eq("a@b.com"),
        check {
          assertThat(it).containsOnly(
            entry("firstName", "F"),
            entry("fullName", "F L"),
            entry("resetLink", optionalLink.get())
          )
        },
        isNull()
      )
      assertThat(optionalLink).isPresent

      verify(userRepository).save(
        check {
          assertThat(it.username).isEqualTo("user")
          assertThat(it.email).isEqualTo("a@b.com")
          assertThat(it.verified).isTrue
          assertThat(it.source).isEqualTo(delius)
        }
      )
    }

    @Test
    fun `Delius User who has not logged into DPS with multiple email accounts can not be reset`() {
      val user = createDeliusUser()
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty()) // no user in auth
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(user)
      whenever(userService.findUserPersonDetailsByEmail(anyString(), any())).thenReturn(listOf(user, user))
      val optional = resetPasswordService.requestResetPassword("someuser@somewhere", "http://url")
      verify(notificationClient).sendEmail(
        eq("resetUnavailableEmailNotFoundTemplate"),
        eq("someuser@somewhere"),
        check {
          assertThat(it).isEmpty()
        },
        isNull()
      )
      assertThat(optional).isEmpty
    }

    @Test
    fun `Delius User not enabled who has not logged into DPS reset password request`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      val deliusUserNotEnabled = createDeliusUserNotEnabled()
      whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(deliusUserNotEnabled))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(deliusUserNotEnabled)
      val optional = resetPasswordService.requestResetPassword("user", "url")
      assertThat(optional).isEmpty
    }
  }

  private val staffUserAccountForBob: NomisUserPersonDetails
    get() {
      return nomisUserPersonDetails(AccountStatus.OPEN)
    }
  private val staffUserAccountLockedForBob: NomisUserPersonDetails
    get() {
      return nomisUserPersonDetails(AccountStatus.LOCKED)
    }
  private val staffUserAccountExpiredLockedForBob: NomisUserPersonDetails
    get() {
      return nomisUserPersonDetails(AccountStatus.EXPIRED_LOCKED_TIMED)
    }

  private fun nomisUserPersonDetails(
    accountStatus: AccountStatus,
    enabled: Boolean = true,
    locked: Boolean = false,
    active: Boolean = true
  ): NomisUserPersonDetails =
    createSampleNomisUser(
      accountStatus = accountStatus,
      firstName = "Bob",
      lastName = "Smith",
      enabled = enabled,
      locked = locked,
      active = active
    )

  private fun suspendedNomisUserPersonDetails(
    accountStatus: AccountStatus,
    enabled: Boolean = true,
    locked: Boolean = false,
    active: Boolean = true,
    staffStatus: String,
  ): NomisUserPersonDetails =
    createSampleNomisUser(
      accountStatus = accountStatus,
      firstName = "Bob",
      lastName = "Smith",
      enabled = enabled,
      locked = locked,
      active = active,
      staffStatus = staffStatus
    )

  private val staffUserAccountForBobOptional: Optional<UserPersonDetails> = Optional.of(staffUserAccountForBob)

  @Nested
  inner class setPassword {
    @Test
    fun resetPassword() {
      val staffUserAccountForBob = staffUserAccountForBob
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      val user = createSampleUser(username = "USER", person = Person("First", "Last"), source = nomis)
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      resetPasswordService.setPassword("bob", "pass")
      assertThat(user.tokens).isEmpty()
      verify(userRepository).save(user)
      verify(delegatingUserService).changePasswordWithUnlock(staffUserAccountForBob, "pass")
      verify(notificationClient).sendEmail(
        "reset-confirm",
        null,
        mapOf("firstName" to "First", "fullName" to "First Last", "username" to "USER"),
        null
      )
    }

    @Test
    fun `Reset password - when username is email address, reset password email text to contain username in lower case`() {
      val staffUserAccountForBob = staffUserAccountForBob
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccountForBob)
      val user = createSampleUser(
        username = "SOMEUSER@SOMEWHERE.COM",
        person = Person("First", "Last"),
        source = nomis,
        email = "someuser@somewhere.com"
      )
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      resetPasswordService.setPassword("bob", "pass")
      assertThat(user.tokens).isEmpty()
      verify(userRepository).save(user)
      verify(delegatingUserService).changePasswordWithUnlock(staffUserAccountForBob, "pass")
      verify(notificationClient).sendEmail(
        "reset-confirm",
        "someuser@somewhere.com",
        mapOf("firstName" to "First", "fullName" to "First Last", "username" to "someuser@somewhere.com"),
        null
      )
    }

    @Test
    fun resetPassword_authUser() {
      val user = createSampleUser(
        username = "user",
        person = Person("First", "Last"),
        enabled = true,
        source = auth,
        locked = true
      )
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails("user")).thenReturn(user)
      resetPasswordService.setPassword("bob", "pass")
      assertThat(user.tokens).isEmpty()
      verify(userRepository).save(user)
      verify(delegatingUserService).changePasswordWithUnlock(any(), anyString())
      verify(notificationClient).sendEmail(
        "reset-confirm",
        null,
        mapOf("firstName" to "First", "fullName" to "First Last", "username" to "user"),
        null
      )
    }

    @Test
    fun resetPassword_deliusUser() {
      val user =
        createSampleUser(
          username = "user",
          person = Person("First", "Last"),
          enabled = true,
          source = delius,
          locked = true
        )
      val userToken = user.createToken(UserToken.TokenType.RESET)
      val deliusUserPersonDetails =
        DeliusUserPersonDetails("user", "12345", "Delius", "Smith", "a@b.com", true, false, setOf())
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails("user")).thenReturn(deliusUserPersonDetails)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      resetPasswordService.setPassword("bob", "pass")
      assertThat(user.tokens).isEmpty()
      verify(userRepository).save(user)
      verify(delegatingUserService).changePasswordWithUnlock(any(), anyString())
      verify(notificationClient).sendEmail(
        "reset-confirm",
        null,
        mapOf("firstName" to "First", "fullName" to "First Last", "username" to "user"),
        null
      )
    }

    @Test
    fun resetPasswordExpired() {
      val staffUserAccount =
        nomisUserPersonDetails(AccountStatus.EXPIRED, enabled = true, locked = false, active = true)
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccount)
      val user = createSampleUser(username = "USER", person = Person("First", "Last"), source = nomis)
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      resetPasswordService.setPassword("bob", "pass")
      assertThat(user.tokens).isEmpty()
      verify(userRepository).save(user)
      verify(delegatingUserService).changePasswordWithUnlock(staffUserAccount, "pass")
      verify(notificationClient).sendEmail(
        "reset-confirm",
        null,
        mapOf("firstName" to "First", "fullName" to "First Last", "username" to "USER"),
        null
      )
    }

    @Test
    fun resetPassword_ShouldFailFor_SuspendedStaff() {
      val staffUserAccount =
        suspendedNomisUserPersonDetails(
          AccountStatus.EXPIRED,
          enabled = true,
          locked = false,
          staffStatus = "SUS"
        )
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccount)
      val user = createSampleUser(username = "USER", person = Person("First", "Last"), source = nomis)
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThatThrownBy { resetPasswordService.setPassword("bob", "pass") }.isInstanceOf(LockedException::class.java)
    }

    @Test
    fun resetPasswordLockedAccount() {
      val staffUserAccount =
        nomisUserPersonDetails(AccountStatus.EXPIRED_LOCKED, enabled = false, locked = true, active = false)
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails(anyString())).thenReturn(staffUserAccount)
      val user = createSampleUser(username = "user", source = nomis, locked = false)
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThatThrownBy { resetPasswordService.setPassword("bob", "pass") }.isInstanceOf(LockedException::class.java)
    }

    @Test
    fun resetPasswordLockedAccount_authUser() {
      val user = createSampleUser(username = "user")
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      whenever(userService.findEnabledOrNomisLockedUserPersonDetails("user")).thenReturn(user)
      assertThatThrownBy { resetPasswordService.setPassword("bob", "pass") }.isInstanceOf(LockedException::class.java)
    }

    @Test
    fun `set password no enabled account`() {
      val user = createSampleUser(username = "user")
      val userToken = user.createToken(UserToken.TokenType.RESET)
      whenever(userTokenRepository.findById(anyString())).thenReturn(Optional.of(userToken))
      assertThatThrownBy {
        resetPasswordService.setPassword(
          "bob",
          "pass"
        )
      }.isInstanceOf(ResetPasswordException::class.java)
    }
  }

  @Test
  fun moveTokenToAccount_missingUsername() {
    assertThatThrownBy {
      resetPasswordService.moveTokenToAccount(
        "token",
        "  "
      )
    }.hasMessageContaining("failed with reason: missing")
  }

  @Test
  fun moveTokenToAccount_usernameNotFound() {
    assertThatThrownBy {
      resetPasswordService.moveTokenToAccount(
        "token",
        "noone"
      )
    }.hasMessageContaining("failed with reason: notfound")
  }

  @Test
  fun moveTokenToAccount_differentEmail() {
    whenever(userRepository.findByUsername(anyString())).thenReturn(
      Optional.of(
        createSampleUser(username = "user", email = "email", verified = true, locked = true)
      )
    )
    val builtUser = createSampleUser(username = "other", email = "emailother", verified = true)
    whenever(userTokenRepository.findById("token")).thenReturn(Optional.of(builtUser.createToken(UserToken.TokenType.RESET)))
    assertThatThrownBy {
      resetPasswordService.moveTokenToAccount(
        "token",
        "noone"
      )
    }.hasMessageContaining("failed with reason: email")
  }

  @Test
  fun moveTokenToAccount_disabled() {
    whenever(userRepository.findByUsername(anyString())).thenReturn(
      Optional.of(createSampleUser(username = "user", email = "email", verified = true))
    )
    val builtUser = createSampleUser(username = "other", email = "email", verified = true)
    whenever(userTokenRepository.findById("token")).thenReturn(Optional.of(builtUser.createToken(UserToken.TokenType.RESET)))
    assertThatThrownBy { resetPasswordService.moveTokenToAccount("token", "noone") }.extracting("reason")
      .isEqualTo("locked")
  }

  @Test
  fun moveTokenToAccount_sameUserAccount() {
    val user = createSampleUser(username = "USER", email = "email", verified = true, enabled = true)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    whenever(userTokenRepository.findById("token")).thenReturn(Optional.of(user.createToken(UserToken.TokenType.RESET)))
    val newToken = resetPasswordService.moveTokenToAccount("token", "USER")
    assertThat(newToken).isEqualTo("token")
  }

  @Test
  fun moveTokenToAccount_differentAccount() {
    val user = createSampleUser(username = "USER", email = "email", verified = true, enabled = true)
    whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
    val builtUser = createSampleUser(username = "other", email = "email", verified = true)
    val userToken = builtUser.createToken(UserToken.TokenType.RESET)
    whenever(userTokenRepository.findById("token")).thenReturn(Optional.of(userToken))
    val newToken = resetPasswordService.moveTokenToAccount("token", "USER")
    assertThat(newToken).hasSize(36)
    verify(userTokenRepository).delete(userToken)
    assertThat(user.tokens).extracting<String> { obj: UserToken -> obj.token }.containsExactly(newToken)
  }

  private fun createDeliusUser() =
    DeliusUserPersonDetails(
      username = "user",
      userId = "12345",
      firstName = "F",
      surname = "L",
      email = "a@b.com",
      enabled = true
    )

  private fun createDeliusUserNotEnabled() =
    DeliusUserPersonDetails(
      username = "user",
      userId = "12345",
      firstName = "F",
      surname = "L",
      email = "a@b.com",
      enabled = false
    )

  private fun buildStandardUser(
    username: String,
    accountStatus: AccountStatus = AccountStatus.OPEN,
    accountNonLocked: Boolean = true,
    credentialsNonExpired: Boolean = true,
    enabled: Boolean = true
  ): NomisUserPersonDetails {
    return NomisUserPersonDetails(
      username = username,
      accountStatus = accountStatus,
      userId = "1",
      firstName = "Bob",
      surname = "Smith",
      activeCaseLoadId = "BXI",
      email = "Bob.smith@justice.gov.uk",
      accountNonLocked = accountNonLocked,
      credentialsNonExpired = credentialsNonExpired,
      enabled = enabled,
      active = true,
      staffStatus = "ACTIVE"
    )
  }
}
