package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserRetries
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRetriesRepository
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.service.DelegatingUserService
import java.time.LocalDateTime
import java.util.Optional

class UserRetriesServiceTest {
  private val userRetriesRepository: UserRetriesRepository = mock()
  private val userRepository: UserRepository = mock()
  private val delegatingUserService: DelegatingUserService = mock()
  private val userService: UserService = mock()
  private val service = UserRetriesService(userRetriesRepository, userRepository, delegatingUserService, userService, 3)

  @Nested
  inner class resetRetriesAndRecordLogin {
    @Test
    fun resetRetriesAndRecordLogin() {
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.of("bob@bob.justice.gov.uk"))
      service.resetRetriesAndRecordLogin(userPersonDetailsForBob)
      verify(userRetriesRepository).save<UserRetries>(
        check {
          assertThat(it).isEqualTo(UserRetries("bob", 0))
        }
      )
    }

    @Test
    fun resetRetriesAndRecordLogin_RecordLastLogginIn() {
      val user = createSampleUser(username = "joe", lastLoggedIn = LocalDateTime.now().minusDays(1))
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      service.resetRetriesAndRecordLogin(userPersonDetailsForBob)
      assertThat(user.lastLoggedIn).isBetween(LocalDateTime.now().plusMinutes(-1), LocalDateTime.now())
    }

    @Test
    fun `resetRetriesAndRecordLogin save delius email address and name for existing user`() {
      val user = createSampleUser(username = "joe", lastLoggedIn = LocalDateTime.now().minusDays(1))
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      service.resetRetriesAndRecordLogin(
        DeliusUserPersonDetails(
          "deliusUser",
          "12345",
          "Delius",
          "Smith",
          "newemail@bob.com",
          true,
          false,
          emptySet()
        )
      )
      assertThat(user.email).isEqualTo("newemail@bob.com")
      assertThat(user.verified).isTrue
      assertThat(user.person?.firstName).isEqualTo("Delius")
      assertThat(user.person?.lastName).isEqualTo("Smith")
      assertThat(user.name).isEqualTo("Delius Smith")
    }

    @Test
    fun `resetRetriesAndRecordLogin update auth source existing user`() {
      val user = createSampleUser(username = "joe", lastLoggedIn = LocalDateTime.now().minusDays(1))
      assertThat(user.source).isEqualTo(AuthSource.auth)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      service.resetRetriesAndRecordLogin(
        DeliusUserPersonDetails(
          "deliusUser",
          "12345",
          "Delius",
          "Smith",
          "newemail@bob.com",
          true,
          false,
          emptySet()
        )
      )
      assertThat(user.source).isEqualTo(AuthSource.delius)
    }

    @Test
    fun `resetRetriesAndRecordLogin save delius email address and name for new user`() {
      service.resetRetriesAndRecordLogin(
        DeliusUserPersonDetails(
          "deliusUser",
          "12345",
          "Delius",
          "Smith",
          "newemail@bob.com",
          true,
          false,
          emptySet()
        )
      )
      verify(userRepository).save(
        check { user ->
          assertThat(user.email).isEqualTo("newemail@bob.com")
          assertThat(user.verified).isTrue()
          assertThat(user.person?.firstName).isEqualTo("Delius")
          assertThat(user.person?.lastName).isEqualTo("Smith")
        }
      )
    }

    @Test
    fun resetRetriesAndRecordLogin_SaveNewUserWithNomisEmailVerified() {
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.of("bob@bob.justice.gov.uk"))
      service.resetRetriesAndRecordLogin(userPersonDetailsForBob)
      verify(userRepository).save<User>(
        check {
          assertThat(it.username).isEqualTo("bob")
          assertThat(it.email).isEqualTo("bob@bob.justice.gov.uk")
          assertThat(it.verified).isTrue
          assertThat(it.lastLoggedIn).isBetween(LocalDateTime.now().plusMinutes(-1), LocalDateTime.now())
        }
      )
    }

    @Test
    fun resetRetriesAndRecordLogin_SaveNewNomisUserNoEmailAsNotJusticeEmail() {
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.empty())
      val userPersonDetailsNotVerified = createSampleNomisUser(username = "bob", firstName = "Bob", lastName = "bloggs", accountStatus = AccountStatus.OPEN, email = "")
      service.resetRetriesAndRecordLogin(userPersonDetailsNotVerified)
      verify(userRepository).save<User>(
        check {
          assertThat(it.username).isEqualTo("bob")
          assertThat(it.verified).isFalse
          assertThat(it.lastLoggedIn).isBetween(LocalDateTime.now().plusMinutes(-1), LocalDateTime.now())
        }
      )
    }

    @Test
    fun resetRetriesAndRecordLogin_SaveNewNomisUserWithFirstAndLastNames() {
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.empty())
      val userPersonDetailsNotVerified = createSampleNomisUser(username = "bob", firstName = "Bob", lastName = "bloggs", accountStatus = AccountStatus.OPEN, email = "")
      service.resetRetriesAndRecordLogin(userPersonDetailsNotVerified)
      verify(userRepository).save<User>(
        check {
          assertThat(it.username).isEqualTo("bob")
          assertThat(it.verified).isFalse
          assertThat(it.person?.firstName).isEqualTo("Bob")
          assertThat(it.person?.lastName).isEqualTo("bloggs")
          assertThat(it.lastLoggedIn).isBetween(LocalDateTime.now().plusMinutes(-1), LocalDateTime.now())
        }
      )
    }

    @Test
    fun resetRetriesAndRecordLogin_UpdateExistingNomisUserWithFirstAndLastNames() {
      val user = createSampleUser(username = "bob", lastLoggedIn = LocalDateTime.now())
      whenever(userService.getEmailAddressFromNomis(anyString())).thenReturn(Optional.empty())
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      service.resetRetriesAndRecordLogin(userPersonDetailsForBob)
      verify(userRepository).save<User>(
        check {
          assertThat(it.username).isEqualTo("bob")
          assertThat(it.verified).isFalse
          assertThat(it.person?.firstName).isEqualTo("Bob")
          assertThat(it.person?.lastName).isEqualTo("bloggs")
          assertThat(it.lastLoggedIn).isBetween(LocalDateTime.now().plusMinutes(-1), LocalDateTime.now())
        }
      )
    }
  }

  @Nested
  inner class incrementRetriesAndLockAccountIfNecessary {
    @Test
    fun incrementRetriesAndLockAccountIfNecessary_retriesTo0() {
      service.incrementRetriesAndLockAccountIfNecessary(userPersonDetailsForBob)
      verify(userRetriesRepository).save<UserRetries>(
        check {
          assertThat(it).isEqualTo(UserRetries("bob", 0))
        }
      )
    }

    @Test
    fun incrementRetriesAndLockAccountIfNecessary_moreAttemptsAllowed() {
      whenever(userRetriesRepository.findById(anyString())).thenReturn(Optional.of(UserRetries("bob", 1)))
      val userPersonDetailsForBob = userPersonDetailsForBob
      service.incrementRetriesAndLockAccountIfNecessary(userPersonDetailsForBob)
      verify(delegatingUserService, never()).lockAccount(any())
    }

    @Test
    fun incrementRetriesAndLockAccountIfNecessary_lockAccount() {
      whenever(userRetriesRepository.findById(anyString())).thenReturn(Optional.of(UserRetries("bob", 2)))
      val userPersonDetailsForBob = userPersonDetailsForBob
      service.incrementRetriesAndLockAccountIfNecessary(userPersonDetailsForBob)
      verify(delegatingUserService).lockAccount(userPersonDetailsForBob)
    }

    @Test
    fun incrementRetriesAndLockAccountIfNecessary_NoExistingRow() {
      whenever(userRetriesRepository.findById(anyString())).thenReturn(Optional.empty())
      assertThat(service.incrementRetriesAndLockAccountIfNecessary(userPersonDetailsForBob)).isEqualTo(false)
      verify(userRetriesRepository).save<UserRetries>(
        check {
          assertThat(it).isEqualTo(UserRetries("bob", 11))
        }
      )
    }

    @Test
    fun incrementRetriesAndLockAccountIfNecessary_ExistingRow() {
      whenever(userRetriesRepository.findById(anyString())).thenReturn(Optional.of(UserRetries("bob", 5)))
      assertThat(service.incrementRetriesAndLockAccountIfNecessary(userPersonDetailsForBob)).isEqualTo(true)
      verify(userRetriesRepository).save<UserRetries>(
        check {
          assertThat(it).isEqualTo(UserRetries("bob", 6))
        }
      )
    }
  }

  @Nested
  inner class resetRetries {
    @Test
    fun resetRetries() {
      service.resetRetries("bob")
      verify(userRetriesRepository).save<UserRetries>(
        check {
          assertThat(it).isEqualTo(UserRetries("bob", 0))
        }
      )
    }
  }

  private val userPersonDetailsForBob: UserPersonDetails
    get() = createSampleNomisUser(username = "bob", firstName = "Bob", lastName = "bloggs", accountStatus = AccountStatus.OPEN)
}
