package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.GroupAssignableRole
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.GroupRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService.AuthUserGroupException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService.AuthUserGroupManagerException
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck.AuthUserGroupRelationshipException
import java.util.Optional
import java.util.UUID

class AuthUserGroupServiceTest {
  private val userRepository: UserRepository = mock()
  private val groupRepository: GroupRepository = mock()
  private val maintainUserCheck: MaintainUserCheck = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val service = AuthUserGroupService(userRepository, groupRepository, maintainUserCheck, telemetryClient)

  @Test
  fun addGroup_blank() {
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(createSampleUser(username = "user")))
    assertThatThrownBy {
      service.addGroup(
        "user",
        "        ",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: notfound")
  }

  @Test
  fun addGroupByUserId_blank() {
    whenever(userRepository.findById(any())).thenReturn(Optional.of(createSampleUser(username = "user")))
    assertThatThrownBy {
      service.addGroupByUserId(
        "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
        "        ",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: notfound")
  }

  @Test
  fun addGroup_groupAlreadyOnUser() {
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val user = createSampleUser(username = "user", groups = setOf(group))
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    assertThatThrownBy {
      service.addGroup(
        "user",
        "LICENCE_VARY",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: exists")
  }

  @Test
  fun addGroupByUserId_groupAlreadyOnUser() {
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val user = createSampleUser(username = "user", groups = setOf(group))
    whenever(userRepository.findById(any())).thenReturn(Optional.of(user))
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    assertThatThrownBy {
      service.addGroupByUserId(
        "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
        "LICENCE_VARY",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: exists")
  }

  @Test
  fun addGroup_success() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    service.addGroup("user", "GROUP_LICENCE_VARY", "admin", SUPER_USER)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("GROUP_JOE", "GROUP_LICENCE_VARY")
    assertThat(user.authorities).extracting<String> { it.roleCode }.containsOnly("LICENCE_VARY")
  }

  @Test
  fun addGroupByUserId_success() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findById(any())).thenReturn(Optional.of(user))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    service.addGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "GROUP_LICENCE_VARY", "admin", SUPER_USER)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("GROUP_JOE", "GROUP_LICENCE_VARY")
    assertThat(user.authorities).extracting<String> { it.roleCode }.containsOnly("LICENCE_VARY")
  }

  @Test
  fun addGroup_success_groupManager() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("user")).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    service.addGroup("user", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("GROUP_JOE", "GROUP_LICENCE_VARY")
    assertThat(user.authorities).extracting<String> { it.roleCode }.containsOnly("LICENCE_VARY")
  }

  @Test
  fun addGroupByUserId_success_groupManager() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findById(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    service.addGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("GROUP_JOE", "GROUP_LICENCE_VARY")
    assertThat(user.authorities).extracting<String> { it.roleCode }.containsOnly("LICENCE_VARY")
  }

  @Test
  fun addGroup_failure_groupManagerNotMemberOfGroup() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("user")).thenReturn(Optional.of(user))
    val manager = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    assertThatThrownBy {
      service.addGroup("user", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    }.isInstanceOf(AuthUserGroupManagerException::class.java)
      .hasMessage("Add group failed for field group with reason: managerNotMember")
  }

  @Test
  fun addGroupByUserId_failure_groupManagerNotMemberOfGroup() {
    val user = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findById(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))).thenReturn(Optional.of(user))
    val manager = createSampleUser(username = "user", groups = setOf(Group("GROUP_JOE", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    assertThatThrownBy {
      service.addGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    }.isInstanceOf(AuthUserGroupManagerException::class.java)
      .hasMessage("Add group failed for field group with reason: managerNotMember")
  }

  @Test
  fun addGroup_failure_GroupManagerNotAllowedToMaintainUser() {
    val user = createSampleUser(username = "user")
    whenever(userRepository.findByUsernameAndMasterIsTrue("user")).thenReturn(Optional.of(user))
    val manager = createSampleUser(username = "user", groups = setOf(Group("GROUP_LICENCE_VARY", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    doThrow(AuthUserGroupRelationshipException("user", "User not with your groups")).whenever(maintainUserCheck)
      .ensureUserLoggedInUserRelationship(
        anyString(),
        any(),
        any()
      )

    assertThatThrownBy {
      service.addGroup("user", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    }.isInstanceOf(AuthUserGroupRelationshipException::class.java)
      .hasMessage("Unable to maintain user: user with reason: User not with your groups")
  }

  @Test
  fun addGroupByUserId_failure_GroupManagerNotAllowedToMaintainUser() {
    val user = createSampleUser(username = "user")
    whenever(userRepository.findById(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))).thenReturn(Optional.of(user))
    val manager = createSampleUser(username = "user", groups = setOf(Group("GROUP_LICENCE_VARY", "desc")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    val group = Group("GROUP_LICENCE_VARY", "desc")
    val roleLicence = Authority("ROLE_LICENCE_VARY", "Role Licence Vary")
    val roleJoe = Authority("JOE", "Role Joe")
    group.assignableRoles.addAll(
      setOf(
        GroupAssignableRole(roleLicence, group, true),
        GroupAssignableRole(roleJoe, group, false)
      )
    )
    whenever(groupRepository.findByGroupCode(anyString())).thenReturn(group)
    doThrow(AuthUserGroupRelationshipException("user", "User not with your groups")).whenever(maintainUserCheck)
      .ensureUserLoggedInUserRelationship(
        anyString(),
        any(),
        any()
      )

    assertThatThrownBy {
      service.addGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "GROUP_LICENCE_VARY", "manager", GROUP_MANAGER_ROLE)
    }.isInstanceOf(AuthUserGroupRelationshipException::class.java)
      .hasMessage("Unable to maintain user: user with reason: User not with your groups")
  }

  @Test
  fun removeGroup_groupNotOnUser() {
    val user = createSampleUser(username = "user")
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    assertThatThrownBy {
      service.removeGroup(
        "user",
        "BOB",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: missing")
  }

  @Test
  fun removeGroupByUserId_groupNotOnUser() {
    val user = createSampleUser(username = "user")
    whenever(userRepository.findById(any())).thenReturn(Optional.of(user))
    assertThatThrownBy {
      service.removeGroupByUserId(
        "00000000-aaaa-0000-aaaa-0a0a0a0a0a0a",
        "BOB",
        "admin",
        listOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
      )
    }.isInstanceOf(AuthUserGroupException::class.java)
      .hasMessage("Add group failed for field group with reason: missing")
  }

  @Test
  fun removeGroup_success() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("LICENCE_VARY", "desc2")))
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    service.removeGroup("user", "  licence_vary   ", "admin", SUPER_USER)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun removeGroupByUserId_success() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("LICENCE_VARY", "desc2")))
    whenever(userRepository.findById(any())).thenReturn(Optional.of(user))
    service.removeGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "  licence_vary   ", "admin", SUPER_USER)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun removeGroup_success_groupManager() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc2")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("user")).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    service.removeGroup("user", "  group_licence_vary   ", "MANAGER", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun removeGroupByUserId_success_groupManager() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc2")))
    whenever(userRepository.findById(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    service.removeGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "  group_licence_vary   ", "MANAGER", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun removeGroup_failure_groupManager() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc2")))
    whenever(userRepository.findByUsernameAndMasterIsTrue("user")).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    service.removeGroup("user", "  group_licence_vary   ", "MANAGER", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun removeGroupByUserId_failure_groupManager() {
    val user = createSampleUser(username = "user")
    user.groups.addAll(setOf(Group("JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc2")))
    whenever(userRepository.findById(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))).thenReturn(Optional.of(user))
    val manager = createSampleUser(
      username = "user",
      groups = setOf(Group("GROUP_JOE", "desc"), Group("GROUP_LICENCE_VARY", "desc"))
    )
    whenever(userRepository.findByUsernameAndMasterIsTrue("MANAGER")).thenReturn(Optional.of(manager))
    service.removeGroupByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "  group_licence_vary   ", "MANAGER", GROUP_MANAGER_ROLE)
    assertThat(user.groups).extracting<String> { it.groupCode }.containsOnly("JOE")
  }

  @Test
  fun authUserGroups_notfound() {
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.empty())
    val groups = service.getAuthUserGroups(" BOB ")
    assertThat(groups).isNull()
  }

  @Test
  fun authUserAssignableGroups_notAdminAndNoUser() {
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.empty())
    val groups = service.getAssignableGroups(" BOB ", setOf())
    assertThat(groups).isEmpty()
  }

  @Test
  fun authUserGroups_success() {
    val user = createSampleUser(username = "user", groups = setOf(Group("JOE", "desc"), Group("LICENCE_VARY", "desc2")))
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    val groups = service.getAuthUserGroups(" BOB ")
    assertThat(groups).extracting<String> { it.groupCode }.containsOnly("JOE", "LICENCE_VARY")
  }

  @Test
  fun authUserAssignableGroups_normalUser() {
    val user = createSampleUser(username = "user", groups = setOf(Group("JOE", "desc"), Group("LICENCE_VARY", "desc2")))
    whenever(userRepository.findByUsernameAndMasterIsTrue(anyString())).thenReturn(Optional.of(user))
    val groups = service.getAssignableGroups(" BOB ", setOf())
    assertThat(groups).extracting<String> { it.groupCode }.containsOnly("JOE", "LICENCE_VARY")
  }

  @Test
  fun authUserAssignableGroups_superUser() {
    whenever(groupRepository.findAllByOrderByGroupName()).thenReturn(
      listOf(
        Group("JOE", "desc"),
        Group("LICENCE_VARY", "desc2")
      )
    )
    val groups = service.getAssignableGroups(" BOB ", setOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS")))
    assertThat(groups).extracting<String> { it.groupCode }.containsOnly("JOE", "LICENCE_VARY")
  }

  companion object {
    private val SUPER_USER: Set<GrantedAuthority> = setOf(SimpleGrantedAuthority("ROLE_MAINTAIN_OAUTH_USERS"))
    private val GROUP_MANAGER_ROLE: Set<GrantedAuthority> = setOf(SimpleGrantedAuthority("ROLE_AUTH_GROUP_MANAGER"))
  }
}
