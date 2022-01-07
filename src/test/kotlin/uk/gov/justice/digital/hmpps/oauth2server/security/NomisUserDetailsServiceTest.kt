package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.EXPIRED
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.EXPIRED_GRACE
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.EXPIRED_LOCKED
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.LOCKED
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.LOCKED_TIMED
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisApiUserPersonDetails

class NomisUserDetailsServiceTest {
  private val userService: NomisUserService = mock()
  private val service = NomisUserDetailsService(userService)

  @Test
  fun testHappyUserPath() {
    val user = buildStandardUser("ITAG_USER")
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isTrue()
    assertThat(itagUser.isCredentialsNonExpired).isTrue()
    assertThat(itagUser.isEnabled).isTrue()
    assertThat((itagUser as UserPersonDetails).name).isEqualTo("Itag User")
  }

  @Test
  fun testLockedUser() {
    val user = buildLockedUser()
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isFalse()
    assertThat(itagUser.isCredentialsNonExpired).isTrue()
    assertThat(itagUser.isEnabled).isFalse()
  }

  @Test
  fun testExpiredUser() {
    val user = buildExpiredUser()
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isTrue()
    assertThat(itagUser.isCredentialsNonExpired).isFalse()
    assertThat(itagUser.isEnabled).isTrue()
  }

  @Test
  fun testUserNotFound() {
    whenever(userService.getNomisUserByUsername(anyString())).thenReturn(null)
    assertThatThrownBy { service.loadUserByUsername("user") }.isInstanceOf(UsernameNotFoundException::class.java)
  }

  @Test
  fun testExpiredGraceUser() {
    val user = buildExpiredGraceUser()
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isTrue()
    assertThat(itagUser.isCredentialsNonExpired).isTrue()
    assertThat(itagUser.isEnabled).isTrue()
  }

  @Test
  fun testExpiredLockedUser() {
    val user = buildExpiredLockedUser()
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonLocked).isFalse()
    assertThat(itagUser.isCredentialsNonExpired).isFalse()
    assertThat(itagUser.isEnabled).isFalse()
  }

  @Test
  fun testLockedTimedUser() {
    val user = buildLockedTimedUser()
    whenever(userService.getNomisUserByUsername(user.username)).thenReturn(user)
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isEnabled).isFalse()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isFalse()
    assertThat(itagUser.isCredentialsNonExpired).isTrue()
  }

  private fun buildStandardUser(
    username: String,
    accountStatus: AccountStatus = AccountStatus.OPEN,
    accountNonLocked: Boolean = true,
    credentialsNonExpired: Boolean = true,
    enabled: Boolean = true
  ): NomisApiUserPersonDetails {
    return NomisApiUserPersonDetails(
      username = username,
      accountStatus = accountStatus,
      userId = "1",
      firstName = "Itag",
      surname = "User",
      activeCaseLoadId = "BXI",
      email = "b.h@somewhere.com",
      accountNonLocked = accountNonLocked,
      credentialsNonExpired = credentialsNonExpired,
      enabled = enabled
    )
  }

  private fun buildExpiredUser(): NomisApiUserPersonDetails =
    buildStandardUser("EXPIRED_USER", EXPIRED, accountNonLocked = true, credentialsNonExpired = false, enabled = true)

  private fun buildLockedUser(): NomisApiUserPersonDetails =
    buildStandardUser("LOCKED_USER", LOCKED, accountNonLocked = false, credentialsNonExpired = true, enabled = false)

  private fun buildExpiredLockedUser(): NomisApiUserPersonDetails =
    buildStandardUser(
      "EXPIRED_USER",
      EXPIRED_LOCKED,
      accountNonLocked = false,
      credentialsNonExpired = false,
      enabled = false
    )

  private fun buildLockedTimedUser(): NomisApiUserPersonDetails =
    buildStandardUser(
      "LOCKED_USER",
      LOCKED_TIMED,
      accountNonLocked = false,
      credentialsNonExpired = true,
      enabled = false
    )

  private fun buildExpiredGraceUser(): NomisApiUserPersonDetails =
    buildStandardUser("EXPIRED_USER", EXPIRED_GRACE)
}
