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
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.Staff
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.LinkEmailAndUsername
import java.util.Optional
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

      verify(staffUserAccountRepository).findAllNomisUsersByEmailAddress("email")
      verify(userRepository).findByEmailAndSourceOrderByUsername("email", nomis)
      verifyNoMoreInteractions(staffUserAccountRepository)
    }

    @Test
    fun `users found`() {
      val joe = getNomisUser("JOE")
      val fred = getNomisUser("FRED")
      val harry = getNomisUser("HARRY")
      val bob = getNomisUser("BOB")
      whenever(staffUserAccountRepository.findAllNomisUsersByEmailAddress(anyString())).thenReturn(
        listOf(
          joe,
          fred,
          harry
        )
      )
      whenever(userRepository.findByEmailAndSourceOrderByUsername(anyString(), any())).thenReturn(
        listOf(getUserFromAuth("JOE"), getUserFromAuth("BOB"))
      )
      whenever(staffUserAccountRepository.findAllById(any())).thenReturn(listOf(joe, bob))
      assertThat(nomisUserService.getNomisUsersByEmail("email")).containsExactlyInAnyOrder(joe, fred, harry, bob)

      verify(staffUserAccountRepository).findAllById(listOf("JOE", "BOB"))
    }

    @Test
    fun `ignore unverified auth`() {
      val user = createSampleUser(username = "joe", source = nomis, verified = false)

      whenever(userRepository.findByEmailAndSourceOrderByUsername(anyString(), any())).thenReturn(listOf(user))

      assertThat(nomisUserService.getNomisUsersByEmail("email")).isEmpty()

      verify(staffUserAccountRepository).findAllNomisUsersByEmailAddress("email")
      verifyNoMoreInteractions(staffUserAccountRepository)
    }
  }

  @Nested
  inner class changeEmailAndRequestVerification {
    @Test
    fun `success path`() {
      val lue = LinkEmailAndUsername("link", "email", "username")
      whenever(staffUserAccountRepository.findById(any())).thenReturn(
        Optional.of(getNomisUser("joe"))
      )
      whenever(verifyEmailService.changeEmailAndRequestVerification(any(), any(), any(), any(), any(), any()))
        .thenReturn(lue)

      val response = nomisUserService.changeEmailAndRequestVerification("user", "email", "url", User.EmailType.PRIMARY)
      assertThat(response).isSameAs(lue)
      verify(staffUserAccountRepository).findById("USER")
      verify(verifyEmailService).changeEmailAndRequestVerification(
        "joe",
        "email",
        "Bob",
        "Bob Last",
        "url",
        User.EmailType.PRIMARY
      )
    }

    @Test
    fun `user not found`() {
      whenever(staffUserAccountRepository.findById(any())).thenReturn(Optional.empty())

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

  private fun getNomisUser(username: String): NomisUserPersonDetails =
    createSampleNomisUser(
      staff = Staff(firstName = "bob", status = "INACTIVE", lastName = "last", staffId = 5),
      username = username
    )

  private fun getUserFromAuth(username: String) = createSampleUser(username = username, source = nomis, verified = true)
}
