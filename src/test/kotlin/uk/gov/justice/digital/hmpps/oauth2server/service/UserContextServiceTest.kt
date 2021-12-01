@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisApiUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.NomisUserService
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.util.UUID

internal class UserContextServiceTest {
  private val deliusUserService: DeliusUserService = mock()
  private val authUserService: AuthUserService = mock()
  private val nomisUserService: NomisUserService = mock()
  private val userService: UserService = mock()
  private val userContextService =
    UserContextService(deliusUserService, authUserService, nomisUserService, userService, false)
  private val linkAccountsEnabledUserContextService =
    UserContextService(deliusUserService, authUserService, nomisUserService, userService, true)

  @Nested
  inner class discoverUsers {
    @Test
    fun `discoverUsers returns empty list for clients with 'normal' scopes`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, setOf("read"))
      assertThat(users).isEmpty()
    }

    @Test
    fun `discoverUsers returns empty list when not azuread from mapping`() {
      val loginUser = createSampleUser(username = "username", source = auth, id = UUID.randomUUID())
      val scopes = setOf("read", "delius")
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).isEmpty()
    }

    @Test
    fun `discoverUsers can map from azureAD to delius`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "emailid@email.com", "jwtId")
      val deliusUser = DeliusUserPersonDetails("username", "id", "user", "name", "email@email.com", true)
      val scopes = setOf("delius")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).containsExactly(deliusUser)

      verify(deliusUserService).getDeliusUsersByEmail("emailid@email.com")
    }

    @Test
    fun `discoverUsers can map if link accounts enabled`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "nomis", "emailid@email.com", "jwtId")
      val deliusUser = DeliusUserPersonDetails("username", "id", "user", "name", "email@email.com", true)
      val scopes = setOf("delius")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = linkAccountsEnabledUserContextService.discoverUsers(loginUser, scopes)
      assertThat(users).containsExactly(deliusUser)

      verify(deliusUserService).getDeliusUsersByEmail("emailid@email.com")
    }

    @Test
    fun `discoverUsers tries all three sources when no valid scopes found`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "emailid@email.com", "jwtId")
      val scopes = setOf("read,write")
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      userContextService.discoverUsers(loginUser, scopes)
      verify(deliusUserService).getDeliusUsersByEmail("emailid@email.com")
      verify(nomisUserService).getNomisUsersByEmail("emailid@email.com")
      verify(authUserService).findAuthUsersByEmail("emailid@email.com")
    }

    @Test
    fun `discoverUsers can map from azureAD to nomis`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "emailid@email.com", "jwtId")
      val nomisUser = createSampleNomisApiUser(
        username = "username",
        userId = "",
        firstName = "Bob",
        lastName = "Smith",
        email = "a.user@justice.gov.uk",
      )
      val scopes = setOf("nomis")
      whenever(nomisUserService.getNomisUsersByEmail(anyString())).thenReturn(listOf(nomisUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val user = userContextService.discoverUsers(loginUser, scopes)
      assertThat(user).containsExactly(nomisUser)

      verify(nomisUserService).getNomisUsersByEmail("emailid@email.com")
    }

    @Test
    fun `discoverUsers returns no users when the user service returns no users`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val scopes = setOf("delius")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(emptyList())
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).isEmpty()
    }

    @Test
    fun `discoverUsers returns the empty when no to mapping exists`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val scopes = setOf("nomis")
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).isEmpty()
    }

    @Test
    fun `discoverUsers returns all users when multiple users matched`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val deliusUser = DeliusUserPersonDetails("username", "id", "user", "name", "email@email.com", true)
      val authUser = createSampleUser(username = "username", source = auth, enabled = true, verified = true)
      val scopes = setOf("delius", "auth")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
      whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).containsExactlyInAnyOrder(deliusUser, authUser)
    }

    @Test
    fun `discoverUsers filters by user roles`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val deliusUser = DeliusUserPersonDetails("username", "id", "user", "name", "email@email.com", true)
      val authUser = createSampleUser(
        username = "username",
        source = auth,
        enabled = true,
        verified = true,
        authorities = setOf(Authority("ROLE_BOB", "Role Bob"))
      )
      val scopes = setOf("delius", "auth")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
      whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes, roles = listOf("ROLE_BOB"))
      assertThat(users).containsExactlyInAnyOrder(authUser)
    }

    @Test
    fun `discoverUsers returns all users when multiple users matched from same source`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val authUser = createSampleUser(username = "username", source = auth, enabled = true, verified = true)
      val scopes = setOf("delius", "auth")
      whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser, authUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).hasSize(2).containsExactlyInAnyOrder(authUser, authUser)
    }

    @Test
    fun `discoverUsers ignores disabled users`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val deliusUser = DeliusUserPersonDetails(
        "username",
        "id",
        "user",
        "name",
        "email@email.com",
        enabled = false
      )
      val authUser = createSampleUser(username = "username", source = auth, enabled = true, verified = true)
      val scopes = setOf("delius", "auth")
      whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
      whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).containsExactly(authUser)
    }

    @Test
    fun `discoverUsers ignores unverified users`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "azuread", "email@email.com", "jwtId")
      val authUser = createSampleUser(username = "username", source = auth, enabled = true, verified = true)
      val unverifiedAuthUser = createSampleUser(username = "username1", source = auth, enabled = true)
      val scopes = setOf("auth")
      whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser, unverifiedAuthUser))
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.discoverUsers(loginUser, scopes)
      assertThat(users).containsExactly(authUser)
    }
  }

  @Nested
  inner class checkUser {
    @Test
    fun `checkUser returns true for clients with 'normal' scopes and no roles`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "nomis", "email@email.com", "jwtId")

      val users = userContextService.checkUser(loginUser, setOf("read"))
      assertThat(users).isTrue
    }

    @Test
    fun `checkUser returns true when scopes not matched`() {
      val loginUser = createSampleUser(username = "username", source = auth, id = UUID.randomUUID())
      val scopes = setOf("read", "delius")
      whenever(userService.getEmail(any())).thenReturn(loginUser.userId)

      val users = userContextService.checkUser(loginUser, scopes)
      assertThat(users).isFalse
    }

    @Test
    fun `checkUser returns false if no user roles`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(), "delius", "email@email.com", "jwtId")
      val scopes = setOf("delius", "auth")

      val users = userContextService.checkUser(loginUser, scopes, roles = listOf("ROLE_BOB"))
      assertThat(users).isFalse
    }

    @Test
    fun `checkUser returns true if user roles matched`() {
      val loginUser = UserDetailsImpl("username", "name", listOf(SimpleGrantedAuthority("ROLE_BOB"), SimpleGrantedAuthority("ROLE_JOE")), "delius", "email@email.com", "jwtId")
      val scopes = setOf("delius", "auth")

      val users = userContextService.checkUser(loginUser, scopes, roles = listOf("ROLE_BOB"))
      assertThat(users).isTrue
    }
  }
}
