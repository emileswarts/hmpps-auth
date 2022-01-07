@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisApiUser
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.LinkEmailAndUsername

internal class NomisUserServiceTest {
  private val userRepository: UserRepository = mock()
  private val verifyEmailService: VerifyEmailService = mock()
  private val nomisUserApiService: NomisUserApiService = mock()
  private val nomisUserService: NomisUserService =
    NomisUserService(
      userRepository,
      verifyEmailService,
      nomisUserApiService
    )

  @Nested
  inner class getNomisUsersByEmail {
    @Test
    fun `no users found`() {
      assertThat(nomisUserService.getNomisUsersByEmail("EmAil")).isEmpty()

      verify(nomisUserApiService).findUsersByEmailAddressAndUsernames("email", setOf())
      verify(userRepository).findByEmailAndSourceOrderByUsername("email", nomis)
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
      whenever(nomisUserApiService.findUserByUsername(any())).thenReturn(getNomisApiUser())
      whenever(verifyEmailService.changeEmailAndRequestVerification(any(), any(), any(), any(), any(), any()))
        .thenReturn(lue)

      val response = nomisUserService.changeEmailAndRequestVerification("user", "email", "url", User.EmailType.PRIMARY)
      assertThat(response).isSameAs(lue)
      verify(nomisUserApiService).findUserByUsername("USER")
      verify(verifyEmailService).changeEmailAndRequestVerification(
        "bob",
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
    fun `changePassword happy path`() {
      nomisUserService.changePassword("NOMIS_PASSWORD_RESET", "helloworld2")

      verify(nomisUserApiService).changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
    }
  }

  @Nested
  inner class ChangePasswordWithUnlock {
    @Test
    fun `changePasswordWithUnlock happy path`() {
      nomisUserService.changePasswordWithUnlock("NOMIS_PASSWORD_RESET", "helloworld2")

      verify(nomisUserApiService).changePassword("NOMIS_PASSWORD_RESET", "helloworld2")
      verify(nomisUserApiService).unlockAccount("NOMIS_PASSWORD_RESET")
    }
  }

  @Nested
  inner class lockAccount {
    @Test
    fun `lockAccount happy path`() {
      nomisUserService.lockAccount("NOMIS_PASSWORD_RESET")
      verify(nomisUserApiService).lockAccount("NOMIS_PASSWORD_RESET")
    }
  }

  private fun getNomisApiUser(): NomisApiUserPersonDetails =
    createSampleNomisApiUser()

  private fun getUserFromAuth(username: String) = createSampleUser(username = username, source = nomis, verified = true)
}
