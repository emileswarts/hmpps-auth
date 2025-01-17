package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityManager

class AuthUserDetailsServiceTest {
  private val userService: AuthUserService = mock()
  private val authEntityManager: EntityManager = mock()
  private val service = AuthUserDetailsService(userService, authEntityManager)

  @Test
  fun testAuthEntityDetached() {
    val user = buildAuthUser()
    whenever(userService.getAuthUserByUsername(user.username)).thenReturn(Optional.of(user))
    val itagUser = service.loadUserByUsername(user.username)
    verify(authEntityManager).detach(user)
    assertThat((itagUser as UserPersonDetails).name).isEqualTo("first last")
  }

  @Test
  fun testAuthOnlyUser() {
    val user = buildAuthUser()
    whenever(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(user))
    val itagUser = service.loadUserByUsername(user.username)
    assertThat(itagUser).isNotNull()
    assertThat(itagUser.isAccountNonExpired).isTrue()
    assertThat(itagUser.isAccountNonLocked).isTrue()
    assertThat(itagUser.isCredentialsNonExpired).isTrue()
    assertThat(itagUser.isEnabled).isTrue()
  }

  @Test
  fun testUserNotFound() {
    whenever(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.empty())
    assertThatThrownBy { service.loadUserByUsername("user") }.isInstanceOf(UsernameNotFoundException::class.java)
  }

  private fun buildAuthUser(): User =
    createSampleUser(
      username = "user",
      email = "email",
      verified = true,
      firstName = "first",
      lastName = "last",
      enabled = true,
      passwordExpiry = LocalDateTime.now().plusDays(1),
    )
}
