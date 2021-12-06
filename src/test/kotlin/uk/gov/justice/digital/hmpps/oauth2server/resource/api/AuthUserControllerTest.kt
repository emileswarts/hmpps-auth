@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Group
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User.EmailType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter.Status
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService.CreateUserException
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserGroup
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.AuthUserController.AmendUser
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.AuthUserController.AuthUser
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.AuthUserController.CreateUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.ValidEmailException
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import javax.servlet.http.HttpServletRequest

class AuthUserControllerTest {
  private val userService: UserService = mock()
  private val authUserService: AuthUserService = mock()
  private val authUserGroupService: AuthUserGroupService = mock()
  private val authUserRoleService: AuthUserRoleService = mock()
  private val request: HttpServletRequest = mock()
  private val authUserController = AuthUserController(userService, authUserService, authUserGroupService, authUserRoleService, false)
  private val authentication = UsernamePasswordAuthenticationToken("bob", "pass", listOf())

  @Test
  fun user_userNotFound() {
    val responseEntity = authUserController.user("bob")
    assertThat(responseEntity.statusCodeValue).isEqualTo(404)
    assertThat(responseEntity.body).isEqualTo(
      ErrorDetail(
        "Not Found",
        "Account for username bob not found",
        "username"
      )
    )
  }

  @Test
  fun user_success() {
    whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(authUser))
    val responseEntity = authUserController.user("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).isEqualTo(
      AuthUser(
        USER_ID,
        "authentication",
        "email",
        "Joe",
        "Bloggs",
        false,
        true,
        true,
        LocalDateTime.of(2019, 1, 1, 12, 0)
      )
    )
  }

  @Test
  fun userByUserId_userNotFound() {
    assertThatThrownBy { authUserController.getUserById("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", authentication) }.isInstanceOf(UsernameNotFoundException::class.java)
  }

  @Test
  fun userByUserId_success() {
    whenever(authUserService.getAuthUserByUserId(anyString(), any(), any())).thenReturn(authUser)
    val response = authUserController.getUserById("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", authentication)
    assertThat(response).isEqualTo(
      AuthUser(
        USER_ID,
        "authentication",
        "email",
        "Joe",
        "Bloggs",
        false,
        true,
        true,
        LocalDateTime.of(2019, 1, 1, 12, 0)
      )
    )
  }

  @Test
  fun search() {
    whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(authUser))
    val responseEntity = authUserController.searchForUser("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).isEqualTo(
      listOf(
        AuthUser(
          USER_ID,
          "authentication",
          "email",
          "Joe",
          "Bloggs",
          false,
          true,
          true,
          LocalDateTime.of(2019, 1, 1, 12, 0)
        )
      )
    )
  }

  @Test
  fun search_noResults() {
    whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf())
    val responseEntity = authUserController.searchForUser("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(204)
    assertThat(responseEntity.body).isNull()
  }

  @Test
  fun `createUserByEmail username already exists`() {
    whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(createSampleUser(id = UUID.fromString("d4bbc232-45e6-4086-bb0f-f96192438f03"))))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email@justice.gov.uk", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(409)
    assertThat(responseEntity.body).isEqualTo(
      AuthUserController.ErrorDetailUserId("username.exists", "User email@justice.gov.uk already exists", "userId", "d4bbc232-45e6-4086-bb0f-f96192438f03")
    )
  }

  @Test
  fun `createUserByEmail email already exists`() {
    whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(createSampleUser("joe", id = UUID.fromString(USER_ID))))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email@justice.gov.uk", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(409)
    assertThat(responseEntity.body).isEqualTo(
      AuthUserController.ErrorDetailUserId("email.exists", "User email@justice.gov.uk already exists", "email", USER_ID)
    )
  }

  @Test
  fun `createUserByEmail email already exists can't determine username`() {
    whenever(authUserService.findAuthUsersByEmail(anyString())).thenReturn(listOf(createSampleUser("joe"), createSampleUser("bob")))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email@justice.gov.uk", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(409)
    assertThat(responseEntity.body).isEqualTo(
      AuthUserController.ErrorDetailUserId("email.exists", "User email@justice.gov.uk already exists", "email", null)
    )
  }

  @Test
  fun `createUserByEmail success`() {
    whenever(authUserService.createUserByEmail(any(), any(), any(), any(), any(), any(), any())).thenReturn(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).isEqualTo(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))
  }

  @Test
  fun `createUserByEmail trim email`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    authUserController.createUserByEmail(
      CreateUser("   email@justice.gov.uk    ", "first", "last", null, null),
      request,
      authentication
    )
    verify(authUserService).getAuthUserByUsername("email@justice.gov.uk")
    verify(authUserService).createUserByEmail(
      "email@justice.gov.uk",
      "first",
      "last",
      emptySet(),
      "http://some.url/auth/initial-password?token=",
      "bob",
      authentication.authorities
    )
  }

  @Test
  fun `createUserByEmail create user error`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    whenever(
      authUserService.createUserByEmail(
        anyString(),
        anyString(),
        anyString(),
        any(),
        anyString(),
        anyString(),
        any()
      )
    ).thenThrow(CreateUserException("username", "errorcode"))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(400)
    assertThat(responseEntity.body).isEqualTo(
      ErrorDetail(
        "username.errorcode",
        "username failed validation",
        "username"
      )
    )
  }

  @Test
  fun `createUserByEmail verify email address error`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    whenever(
      authUserService.createUserByEmail(
        anyString(),
        anyString(),
        anyString(),
        any(),
        anyString(),
        anyString(),
        any()
      )
    ).thenThrow(ValidEmailException("reason"))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email", "first", "last", null, null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(400)
    assertThat(responseEntity.body).isEqualTo(ErrorDetail("email.reason", "Email address failed validation", "email"))
  }

  @Test
  fun `createUserByEmail Initial Password Url`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    authUserController.createUserByEmail(
      CreateUser("email", "first", "last", null, null),
      request,
      authentication
    )
    verify(authUserService).createUserByEmail(
      "email",
      "first",
      "last",
      emptySet(),
      "http://some.url/auth/initial-password?token=",
      "bob",
      authentication.authorities
    )
  }

  @Test
  fun `createUserByEmail no additional roles`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    authUserController.createUserByEmail(
      CreateUser("email", "first", "last", null, null),
      request,
      authentication
    )
    verify(authUserService).createUserByEmail(
      "email",
      "first",
      "last",
      emptySet(),
      "http://some.url/auth/initial-password?token=",
      "bob",
      authentication.authorities
    )
  }

  @Test
  fun `createUserByEmail multiple additional roles`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    authUserController.createUserByEmail(
      CreateUser("email", "first", "last", null, setOf("ROLE1", "ROLE2")),
      request,
      authentication
    )
    verify(authUserService).createUserByEmail(
      "email",
      "first",
      "last",
      setOf("ROLE1", "ROLE2"),
      "http://some.url/auth/initial-password?token=",
      "bob",
      authentication.authorities
    )
  }

  @Test
  fun `createUserByEmail single additonal role`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/create"))
    authUserController.createUserByEmail(
      CreateUser("email", "first", "last", "ROLE1", null),
      request,
      authentication
    )
    verify(authUserService).createUserByEmail(
      "email",
      "first",
      "last",
      setOf("ROLE1"),
      "http://some.url/auth/initial-password?token=",
      "bob",
      authentication.authorities
    )
  }

  @Test
  fun `createUserByEmail handles group code as empty string`() {
    whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/api/authuser/"))
    whenever(authUserService.createUserByEmail(any(), any(), any(), any(), any(), any(), any())).thenReturn(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))
    val responseEntity =
      authUserController.createUserByEmail(
        CreateUser("email", "first", "last", "", null),
        request,
        authentication
      )
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).isEqualTo(UUID.fromString("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a"))
  }

  @Test
  fun enableUserByUserId() {
    whenever(request.requestURL).thenReturn(StringBuffer("some/auth/url"))
    authUserController.enableUserByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", authentication, request)
    verify(authUserService).enableUserByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "bob", "some/auth/url", authentication.authorities)
  }

  @Test
  fun disableUserByUserId() {
    authUserController.disableUserByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", DeactivateReason("A Reason"), authentication)
    verify(authUserService).disableUserByUserId("00000000-aaaa-0000-aaaa-0a0a0a0a0a0a", "bob", "A Reason", authentication.authorities)
  }

  @Nested
  inner class alterUserEmailByUserId {
    @Test
    fun `check service`() {
      whenever(request.requestURL).thenReturn(StringBuffer("http://some.url/auth/api/authuser/newusername"))
      authUserController.alterUserEmail("user", AmendUser("a@b.com"), request, authentication)
      verify(authUserService).amendUserEmailByUserId(
        "user",
        "a@b.com",
        "http://some.url/auth/initial-password?token=",
        "bob",
        authentication.authorities,
        EmailType.PRIMARY
      )
    }
  }

  @Test
  fun assignableGroups_success() {
    val group1 = Group("FRED", "desc")
    val group2 = Group("GLOBAL_SEARCH", "desc2")
    whenever(authUserGroupService.getAssignableGroups(anyString(), any())).thenReturn(listOf(group1, group2))
    val responseEntity = authUserController.assignableGroups(authentication)
    assertThat(responseEntity).containsOnly(AuthUserGroup(group1), AuthUserGroup(group2))
  }

  @Test
  fun `Get list of searchable roles success`() {
    val role1 = Authority("roles1", "name1", "desc1")
    val role2 = Authority("roles2", "name2")
    whenever(authUserRoleService.getAllAssignableRoles(anyString(), any())).thenReturn(setOf(role1, role2))
    val responseEntity = authUserController.searchableRoles(authentication)
    assertThat(responseEntity).containsOnly(AuthUserRole(role1), AuthUserRole(role2))
  }

  private val authUser: User
    get() = createSampleUser(
      id = UUID.fromString(USER_ID),
      username = "authentication",
      email = "email",
      verified = true,
      enabled = true,
      firstName = "Joe",
      lastName = "Bloggs",
      lastLoggedIn = LocalDateTime.of(2019, 1, 1, 12, 0)
    )

  @Test
  fun searchForUser() {
    val unpaged = Pageable.unpaged()
    whenever(authUserService.findAuthUsers(anyString(), anyList(), anyList(), any(), anyString(), any(), any(), anyOrNull())).thenReturn(
      PageImpl(
        listOf(
          authUser
        )
      )
    )
    authUserController.searchForUser("somename", listOf("somerole"), listOf("somegroup"), Status.ALL, unpaged, authentication)
    verify(authUserService).findAuthUsers(
      "somename",
      listOf("somerole"),
      listOf("somegroup"),
      unpaged,
      "bob",
      emptyList(),
      Status.ALL,
      listOf(AuthSource.auth)
    )
  }

  @Test
  fun `searchForUser map auth user`() {
    val unpaged = Pageable.unpaged()
    whenever(authUserService.findAuthUsers(anyString(), anyList(), anyList(), any(), anyString(), any(), any(), anyOrNull())).thenReturn(
      PageImpl(
        listOf(
          authUser
        )
      )
    )
    val page = authUserController.searchForUser("somename", listOf("somerole"), listOf("somegroup"), Status.ALL, unpaged, authentication).toList()
    assertThat(page).hasSize(1).containsExactlyInAnyOrder(
      AuthUser(
        userId = USER_ID,
        username = "authentication",
        email = "email",
        verified = true,
        enabled = true,
        firstName = "Joe",
        lastName = "Bloggs",
        lastLoggedIn = LocalDateTime.parse("2019-01-01T12:00")
      )
    )
    verify(authUserService).findAuthUsers(
      "somename",
      listOf("somerole"),
      listOf("somegroup"),
      unpaged,
      "bob",
      emptyList(),
      Status.ALL,
      listOf(AuthSource.auth),
    )
  }

  @Test
  fun `get verified emails for a list of users`() {
    whenever(authUserService.findAuthUsersByUsernames(anyList())).thenReturn(
      listOf(
        createSampleUser(verified = true, source = AuthSource.nomis, username = "U1", email = "u1@b.com"),
        createSampleUser(verified = true, source = AuthSource.nomis, username = "U2", email = "u2@b.com"),
        createSampleUser(verified = false, source = AuthSource.nomis, username = "U3", email = "u3@b.com"),
      )
    )

    val emails = authUserController.getAuthUserEmails(listOf("U1", "U1", "U3"))

    assertThat(emails).extracting("email").containsExactlyInAnyOrder("u1@b.com", "u2@b.com")
  }

  companion object {
    private const val USER_ID = "07395ef9-53ec-4d6c-8bb1-0dc96cd4bd2f"
  }
}
