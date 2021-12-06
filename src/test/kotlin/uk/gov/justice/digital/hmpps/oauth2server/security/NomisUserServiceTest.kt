@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.security

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.jdbc.CannotGetJdbcConnectionException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisApiUser
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.LinkEmailAndUsername
import javax.sql.DataSource

internal class NomisUserServiceTest {
  private val dataSource: DataSource = mock()
  private val passwordEncoder: PasswordEncoder = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val userRepository: UserRepository = mock()
  private val verifyEmailService: VerifyEmailService = mock()
  private val nomisUserApiService: NomisUserApiService = mock()
  private val nomisUserService: NomisUserService =
    NomisH2AlterUserService(
      dataSource,
      passwordEncoder,
      staffUserAccountRepository,
      verifyEmailService,
      userRepository,
      nomisUserApiService,
      true
    )
  private val disabledNomisUserService: NomisUserService =
    NomisH2AlterUserService(
      dataSource,
      passwordEncoder,
      staffUserAccountRepository,
      verifyEmailService,
      userRepository,
      nomisUserApiService,
      false
    )

  @Nested
  inner class getNomisUsersByEmail {
    @Test
    fun `no users found`() {
      assertThat(nomisUserService.getNomisUsersByEmail("EmAil")).isEmpty()

      verify(nomisUserApiService).findUsersByEmailAddressAndUsernames("email", setOf())
      verify(userRepository).findByEmailAndSourceOrderByUsername("email", nomis)
      verifyNoMoreInteractions(staffUserAccountRepository)
    }

    @Test
    fun `users found`() {
      whenever(userRepository.findByEmailAndSourceOrderByUsername(anyString(), any())).thenReturn(
        listOf(getUserFromAuth("JOE"), getUserFromAuth("BOB"))
      )
      whenever(nomisUserApiService.findUsersByEmailAddressAndUsernames(anyString(), any())).thenReturn(
        listOf(
          createSampleNomisApiUser("JOE"),
          createSampleNomisApiUser("FRED"),
          createSampleNomisApiUser("HARRY"),
        )
      )
      assertThat(nomisUserService.getNomisUsersByEmail("email@address")).containsExactlyInAnyOrder(
        createSampleNomisApiUser("JOE", email = "email@address"),
        createSampleNomisApiUser("FRED", email = "email@address"),
        createSampleNomisApiUser("HARRY", email = "email@address"),
      )
      verify(nomisUserApiService).findUsersByEmailAddressAndUsernames("email@address", setOf("JOE", "BOB"))
    }

    @Test
    fun `ignore unverified auth`() {
      val user = createSampleUser(username = "joe", source = nomis, verified = false)

      whenever(userRepository.findByEmailAndSourceOrderByUsername(anyString(), any())).thenReturn(listOf(user))

      assertThat(nomisUserService.getNomisUsersByEmail("email")).isEmpty()

      verify(nomisUserApiService).findUsersByEmailAddressAndUsernames("email", setOf())
    }
  }

  @Nested
  inner class changeEmailAndRequestVerification {
    @Test
    fun `success path`() {
      val lue = LinkEmailAndUsername("link", "email", "username")
      whenever(nomisUserApiService.findUserByUsername(any())).thenReturn(getNomisApiUser("joe"))
      whenever(verifyEmailService.changeEmailAndRequestVerification(any(), any(), any(), any(), any(), any()))
        .thenReturn(lue)

      val response = nomisUserService.changeEmailAndRequestVerification("user", "email", "url", User.EmailType.PRIMARY)
      assertThat(response).isSameAs(lue)
      verify(nomisUserApiService).findUserByUsername("USER")
      verify(verifyEmailService).changeEmailAndRequestVerification(
        "joe",
        "email",
        "Bob",
        "Bob Harris",
        "url",
        User.EmailType.PRIMARY
      )
    }

    @Test
    fun `user not found`() {
      whenever(nomisUserApiService.findUserByUsername(any())).thenReturn(null)

      assertThatThrownBy {
        nomisUserService.changeEmailAndRequestVerification("user", "email", "url", User.EmailType.PRIMARY)
      }.isInstanceOf(UsernameNotFoundException::class.java).hasMessage("Account for username user not found")
    }
  }

  @Nested
  inner class ChangePassword {
    @Test
    fun `changePassword disabled`() {
      assertThatThrownBy { disabledNomisUserService.changePassword("user", "pass") }
        .isInstanceOf(CannotGetJdbcConnectionException::class.java)
      verify(staffUserAccountRepository).changePassword("user", "pass")
      verifyZeroInteractions(nomisUserApiService)
    }

    @Test
    fun `changePassword enabled`() {
      assertThatThrownBy { nomisUserService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(CannotGetJdbcConnectionException::class.java)

      verify(nomisUserApiService).changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
    }
  }

  @Nested
  inner class ChangePasswordWithUnlock {
    @Test
    fun `changePasswordWithUnlock disabled`() {
      assertThatThrownBy { disabledNomisUserService.changePasswordWithUnlock("user", "pass") }
        .isInstanceOf(CannotGetJdbcConnectionException::class.java)
      verify(staffUserAccountRepository).changePassword("user", "pass")

      // doesn't actually call staffUserAccountRepository.unlockUser since the exception is thrown first

      verifyZeroInteractions(nomisUserApiService)
    }

    @Test
    fun `changePasswordWithUnlock enabled`() {
      assertThatThrownBy { nomisUserService.changePasswordWithUnlock("NOMIS_PASSWORD_RESET", "helloworld2") }
        .isInstanceOf(CannotGetJdbcConnectionException::class.java)

      verify(nomisUserApiService).changePassword("NOMIS_PASSWORD_RESET", "helloworld2")

      // doesn't actually call nomisUserApiService.unlockAccount since the exception is thrown first
    }
  }

  @Nested
  inner class lockAccount {
    @Test
    fun `lockAccount disabled`() {
      disabledNomisUserService.lockAccount("user")
      verify(staffUserAccountRepository).lockUser("user")
      verifyZeroInteractions(nomisUserApiService)
    }

    @Test
    fun `lockAccount enabled`() {
      nomisUserService.lockAccount("NOMIS_PASSWORD_RESET")
      verify(nomisUserApiService).lockAccount("NOMIS_PASSWORD_RESET")
    }
  }

  private fun getNomisApiUser(username: String): NomisApiUserPersonDetails =
    createSampleNomisApiUser(username = username)

  private fun getUserFromAuth(username: String) = createSampleUser(username = username, source = nomis, verified = true)
}
