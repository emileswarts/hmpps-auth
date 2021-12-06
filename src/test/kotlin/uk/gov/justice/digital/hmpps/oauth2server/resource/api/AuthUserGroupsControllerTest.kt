package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ChildGroup
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import java.security.Principal

class AuthUserGroupsControllerTest {
  private val principal: Principal = UsernamePasswordAuthenticationToken("bob", "pass")
  private val authUserService: AuthUserService = mock()
  private val authUserGroupService: AuthUserGroupService = mock()
  private val authUserGroupsController = AuthUserGroupsController(authUserService, authUserGroupService)
  private val authenticationSuperUser =
    TestingAuthenticationToken(
      UserDetailsImpl("bob", "name", SUPER_USER, AuthSource.auth.name, "userid", "jwtId"),
      "pass",
      "ROLE_MAINTAIN_OAUTH_USERS"
    )
  private val authenticationGroupManager =
    TestingAuthenticationToken(
      UserDetailsImpl("JOHN", "name", GROUP_MANAGER, AuthSource.auth.name, "userid", "jwtId"),
      "pass",
      "ROLE_AUTH_GROUP_MANAGER"
    )

  @Nested
  inner class Groups {
    @Test
    fun `groups userNotFound`() {
      whenever(authUserGroupService.getAuthUserGroups(anyString())).thenReturn(null)
      assertThatThrownBy { authUserGroupsController.groups("bob") }
        .isInstanceOf(UsernameNotFoundException::class.java)
    }

    @Test
    fun `groups no children`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      whenever(authUserGroupService.getAuthUserGroups(anyString())).thenReturn(setOf(group1, group2))
      val responseEntity = authUserGroupsController.groups(username = "joe", children = false)
      assertThat(responseEntity).containsOnly(AuthUserGroup(group1), AuthUserGroup(group2))
    }

    @Test
    fun `groups default children`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      val childGroup = ChildGroup("CHILD_1", "child 1")
      group2.children.add(childGroup)
      whenever(authUserGroupService.getAuthUserGroups(anyString())).thenReturn(setOf(group1, group2))
      val responseEntity = authUserGroupsController.groups(username = "joe")
      assertThat(responseEntity).containsOnly(AuthUserGroup("FRED", "desc"), AuthUserGroup("CHILD_1", "child 1"))
    }

    @Test
    fun `groups with children requested`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      val childGroup = ChildGroup("CHILD_1", "child 1")
      group2.children.add(childGroup)
      whenever(authUserGroupService.getAuthUserGroups(anyString())).thenReturn(setOf(group1, group2))
      val responseEntity = authUserGroupsController.groups(username = "joe")
      assertThat(responseEntity).containsOnly(AuthUserGroup("FRED", "desc"), AuthUserGroup("CHILD_1", "child 1"))
    }
  }

  @Nested
  inner class GroupsByUserId {
    @Test
    fun `groups userNotFound`() {
      whenever(authUserGroupService.getAuthUserGroupsByUserId(anyString(), anyString(), any())).thenReturn(null)
      assertThatThrownBy {
        authUserGroupsController.groupsByUserId(
          userId = "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
          authentication = authenticationSuperUser
        )
      }
        .isInstanceOf(UsernameNotFoundException::class.java)
    }

    @Test
    fun `groups no children`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      whenever(authUserGroupService.getAuthUserGroupsByUserId(anyString(), anyString(), any())).thenReturn(
        setOf(
          group1,
          group2
        )
      )
      val responseEntity =
        authUserGroupsController.groupsByUserId(
          userId = "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
          children = false,
          authentication = authenticationSuperUser
        )
      assertThat(responseEntity).containsOnly(AuthUserGroup(group1), AuthUserGroup(group2))
    }

    @Test
    fun `groups default children`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      val childGroup = ChildGroup("CHILD_1", "child 1")
      group2.children.add(childGroup)
      whenever(authUserGroupService.getAuthUserGroupsByUserId(anyString(), anyString(), any())).thenReturn(
        setOf(
          group1,
          group2
        )
      )
      val responseEntity = authUserGroupsController.groupsByUserId(
        userId = "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
        authentication = authenticationSuperUser
      )
      assertThat(responseEntity).containsOnly(AuthUserGroup("FRED", "desc"), AuthUserGroup("CHILD_1", "child 1"))
    }

    @Test
    fun `groups with children requested`() {
      val group1 = Group("FRED", "desc")
      val group2 = Group("GLOBAL_SEARCH", "desc2")
      val childGroup = ChildGroup("CHILD_1", "child 1")
      group2.children.add(childGroup)
      whenever(authUserGroupService.getAuthUserGroupsByUserId(anyString(), anyString(), any())).thenReturn(
        setOf(
          group1,
          group2
        )
      )
      val responseEntity = authUserGroupsController.groupsByUserId(
        userId = "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
        authentication = authenticationSuperUser
      )
      assertThat(responseEntity).containsOnly(AuthUserGroup("FRED", "desc"), AuthUserGroup("CHILD_1", "child 1"))
    }
  }

  @Test
  fun addGroupByUserId_success() {
    authUserGroupsController.addGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "group", authenticationSuperUser)
    verify(authUserGroupService).addGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "group",
      "bob",
      authenticationSuperUser.authorities
    )
  }

  @Test
  fun addGroupByUserId_success_groupManager() {
    authUserGroupsController.addGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "group",
      authenticationGroupManager
    )
    verify(authUserGroupService).addGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "group",
      "JOHN",
      authenticationGroupManager.authorities
    )
  }

  @Test
  fun removeGroupByUserId_success() {
    authUserGroupsController.removeGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "joe", authenticationSuperUser)
    verify(authUserGroupService).removeGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "joe",
      "bob",
      authenticationSuperUser.authorities
    )
  }

  @Test
  fun removeGroupByUserId_success_groupManager() {
    authUserGroupsController.removeGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "joe",
      authenticationGroupManager
    )
    verify(authUserGroupService).removeGroupByUserId(
      "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
      "joe",
      "JOHN",
      authenticationGroupManager.authorities
    )
  }

  private val authUser: User
    get() {
      return createSampleUser(
        username = "USER",
        email = "email",
        verified = true,
        groups = setOf(Group("GLOBAL_SEARCH", "desc2"), Group("FRED", "desc"))
      )
    }

  companion object {
    private val SUPER_USER: Set<GrantedAuthority> = setOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
    private val GROUP_MANAGER: Set<GrantedAuthority> = setOf(SimpleGrantedAuthority("ROLE_AUTH_GROUP_MANAGER"))
  }
}
