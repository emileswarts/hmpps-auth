package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole

class AuthUserRolesControllerTest {
  private val principal: Authentication = UsernamePasswordAuthenticationToken("bob", "pass")
  private val authUserService: AuthUserService = mock()
  private val authUserRoleService: AuthUserRoleService = mock()
  private val authUserRolesController = AuthUserRolesController(authUserService, authUserRoleService)

  @Test
  fun rolesByUserId_userNotFound() {
    assertThatThrownBy { authUserRolesController.rolesByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", principal) }
      .isInstanceOf(UsernameNotFoundException::class.java).withFailMessage("Account for userId 00000000-aaaa-0000-aaaa-0a0a0a0a0a0a not found")
  }

  @Test
  fun rolesByUserId_success() {
    whenever(authUserService.getAuthUserByUserId(anyString(), anyString(), any())).thenReturn(authUser)
    val responseEntity = authUserRolesController.rolesByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", principal)
    assertThat(responseEntity).containsOnly(
      AuthUserRole(Authority("FRED", "FRED")),
      AuthUserRole(Authority("GLOBAL_SEARCH", "Global Search"))
    )
  }

  @Test
  fun addRoleByUserId_success() {
    authUserRolesController.addRoleByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "role", principal)
    verify(authUserRoleService).addRolesByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", listOf("role"), "bob", principal.authorities)
  }

  @Test
  fun removeRoleByUserId_success() {
    authUserRolesController.removeRoleByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "joe", principal)
    verify(authUserRoleService).removeRoleByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "joe", "bob", principal.authorities)
  }

  @Test
  fun assignableRolesByUserId() {
    whenever(authUserRoleService.getAssignableRolesByUserId(anyString(), any())).thenReturn(
      listOf(
        Authority("FRED", "FRED"),
        Authority("GLOBAL_SEARCH", "Global Search")
      )
    )
    val response = authUserRolesController.assignableRolesByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", principal)
    assertThat(response).containsOnly(
      AuthUserRole(Authority("FRED", "FRED")),
      AuthUserRole(Authority("GLOBAL_SEARCH", "Global Search"))
    )
  }

  private val authUser: User
    get() {
      return createSampleUser(
        username = "USER",
        email = "email",
        verified = true,
        authorities = setOf(Authority("FRED", "FRED"), Authority("GLOBAL_SEARCH", "Global Search"))
      )
    }
}
