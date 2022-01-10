package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.model.EmailAddress
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.model.UserDetail
import uk.gov.justice.digital.hmpps.oauth2server.model.UserRole
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class UserControllerTest {
  private val userService: UserService = mock()
  private val userController = UserController(userService)
  private val authentication = UsernamePasswordAuthenticationToken("bob", "pass", listOf())

  @Test
  fun user_nomisUserNoCaseload() {
    setupFindUserCallForNomis(activeCaseLoadId = null, enabled = false)
    val user = userController.user("joe")
    assertThat(user).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        false,
        "Joe Bloggs",
        AuthSource.nomis,
        5L,
        null,
        "5",
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun user_nomisUser() {
    setupFindUserCallForNomis(enabled = false)
    val user = userController.user("joe")
    assertThat(user).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        false,
        "Joe Bloggs",
        AuthSource.nomis,
        5L,
        "somecase",
        "5",
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun user_authUser() {
    setupFindUserCallForAuth()
    val user = userController.user("joe")
    assertThat(user).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        true,
        "Joe Bloggs",
        AuthSource.auth,
        null,
        null,
        USER_ID,
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun me_userNotFound() {
    val principal = TestingAuthenticationToken("principal", "credentials")
    assertThat(userController.me(principal)).usingRecursiveComparison().isEqualTo(UserDetail.fromUsername("principal"))
  }

  @Test
  fun me_nomisUserNoCaseload() {
    setupFindUserCallForNomis(activeCaseLoadId = null, enabled = false)
    val principal = TestingAuthenticationToken("principal", "credentials")
    assertThat(userController.me(principal)).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        false,
        "Joe Bloggs",
        AuthSource.nomis,
        5L,
        null,
        "5",
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun me_nomisUser() {
    setupFindUserCallForNomis(enabled = false)
    val principal = TestingAuthenticationToken("principal", "credentials")
    assertThat(userController.me(principal)).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        false,
        "Joe Bloggs",
        AuthSource.nomis,
        5L,
        "somecase",
        "5",
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun me_authUser() {
    setupFindUserCallForAuth()
    val principal = TestingAuthenticationToken("principal", "credentials")
    assertThat(userController.me(principal)).usingRecursiveComparison().isEqualTo(
      UserDetail(
        "principal",
        true,
        "Joe Bloggs",
        AuthSource.auth,
        null,
        null,
        USER_ID,
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun myRoles() {
    val authorities = listOf(SimpleGrantedAuthority("ROLE_BOB"), SimpleGrantedAuthority("ROLE_JOE_FRED"))
    val token = UsernamePasswordAuthenticationToken("principal", "credentials", authorities)
    assertThat(userController.myRoles(token)).containsOnly(UserRole("BOB"), UserRole("JOE_FRED"))
  }

  @Test
  fun myRoles_noRoles() {
    val token = UsernamePasswordAuthenticationToken("principal", "credentials", emptyList())
    assertThat(userController.myRoles(token)).isEmpty()
  }

  @Test
  fun userRoles_authUser() {
    setupFindUserCallForAuth()
    assertThat(userController.userRoles("JOE")).contains(UserRole("TEST_1"), UserRole("TEST_2"))
  }

  @Test
  fun userRoles_nomisUser() {
    setupFindUserCallForNomis()
    assertThat(userController.userRoles("JOE")).contains(UserRole("PRISON"))
  }

  @Test
  fun userRoles_notFound() {
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.empty())
    assertThatThrownBy { userController.userRoles("JOE") }
      .isInstanceOf(UsernameNotFoundException::class.java)
      .hasMessage("Account for username JOE not found")
  }

  @Test
  fun myEmail() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(
      Optional.of(
        createSampleUser(username = "JOE", verified = true, email = "someemail")
      )
    )
    val principal = TestingAuthenticationToken("joe", "credentials")
    val responseEntity = userController.myEmail(principal = principal)
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).usingRecursiveComparison().isEqualTo(EmailAddress("JOE", "someemail", true))
  }

  @Test
  fun userEmail_found() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(
      Optional.of(
        createSampleUser(username = "JOE", verified = true, email = "someemail")
      )
    )
    val responseEntity = userController.getUserEmail("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).usingRecursiveComparison().isEqualTo(EmailAddress("JOE", "someemail", true))
  }

  @Test
  fun userEmail_found_unverified() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(
      Optional.of(
        createSampleUser(username = "JOE", verified = false, email = "someemail")
      )
    )
    val responseEntity = userController.getUserEmail("joe", unverified = true)
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).usingRecursiveComparison().isEqualTo(EmailAddress("JOE", "someemail", false))
  }

  @Test
  fun userEmail_notFound() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.empty())
    val responseEntity = userController.getUserEmail("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(404)
    assertThat(responseEntity.body).usingRecursiveComparison().isEqualTo(
      ErrorDetail(
        "Not Found",
        "Account for username joe not found",
        "username"
      )
    )
  }

  @Test
  fun userEmail_notVerified() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(createSampleUser("JOE")))
    val responseEntity = userController.getUserEmail("joe")
    assertThat(responseEntity.statusCodeValue).isEqualTo(204)
    assertThat(responseEntity.body).isNull()
  }

  @Test
  fun userEmail_null() {
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(createSampleUser("JOE")))
    val responseEntity = userController.getUserEmail("joe", unverified = true)
    assertThat(responseEntity.statusCodeValue).isEqualTo(200)
    assertThat(responseEntity.body).usingRecursiveComparison().isEqualTo(EmailAddress("JOE", null, false))
  }

  private val fakeUser: User
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
  fun userSearch_multipleSourceSystems() {
    val unpaged = Pageable.unpaged()
    whenever(userService.searchUsersInMultipleSourceSystems(anyString(), any(), anyString(), any(), any(), anyOrNull()))
      .thenReturn(PageImpl(listOf(fakeUser)))

    val pageOfUsers = userController.searchForUsersInMultipleSourceSystems(
      "somename",
      UserFilter.Status.ALL,
      listOf(AuthSource.auth, AuthSource.nomis),
      unpaged,
      authentication
    )

    assertThat(pageOfUsers.totalElements).isEqualTo(1)

    verify(userService).searchUsersInMultipleSourceSystems(
      "somename",
      unpaged,
      "bob",
      emptyList(),
      UserFilter.Status.ALL,
      listOf(AuthSource.auth, AuthSource.nomis),
    )
  }

  @Test
  fun userSearch_mulitpleSourcesDefaultValues() {
    val unpaged = Pageable.unpaged()
    whenever(userService.searchUsersInMultipleSourceSystems(anyString(), any(), anyString(), any(), any(), anyOrNull()))
      .thenReturn(PageImpl(listOf(fakeUser)))

    val pageOfUsers = userController.searchForUsersInMultipleSourceSystems(
      "somename",
      UserFilter.Status.ALL,
      null,
      unpaged,
      authentication
    )

    assertThat(pageOfUsers.totalElements).isEqualTo(1)

    verify(userService).searchUsersInMultipleSourceSystems(
      "somename",
      unpaged,
      "bob",
      emptyList(),
      UserFilter.Status.ALL,
      null,
    )
  }

  @Test
  fun userSearch_mulitpleSourcesWithStatusFilterActive() {
    val unpaged = Pageable.unpaged()
    whenever(userService.searchUsersInMultipleSourceSystems(anyString(), any(), anyString(), any(), any(), anyOrNull()))
      .thenReturn(PageImpl(listOf(fakeUser)))

    val pageOfUsers = userController.searchForUsersInMultipleSourceSystems(
      "somename",
      UserFilter.Status.ACTIVE,
      listOf(AuthSource.auth, AuthSource.nomis, AuthSource.delius),
      unpaged,
      authentication
    )

    assertThat(pageOfUsers.totalElements).isEqualTo(1)

    verify(userService).searchUsersInMultipleSourceSystems(
      "somename",
      unpaged,
      "bob",
      emptyList(),
      UserFilter.Status.ACTIVE,
      listOf(AuthSource.auth, AuthSource.nomis, AuthSource.delius),
    )
  }

  private fun setupFindUserCallForNomis(activeCaseLoadId: String? = "somecase", enabled: Boolean = true): NomisUserPersonDetails {
    val user = createSampleNomisUser(
      firstName = "Joe",
      lastName = "Bloggs",
      userId = "5",
      username = "principal",
      accountStatus = AccountStatus.EXPIRED_LOCKED,
      activeCaseLoadId = activeCaseLoadId,
      enabled = enabled
    )
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(user))
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(createSampleUser()))
    return user
  }

  private fun setupFindUserCallForAuth() {
    whenever(userService.findMasterUserPersonDetails(anyString())).thenReturn(Optional.of(createSampleUser()))
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(createSampleUser()))
  }

  private fun createSampleUser() = createSampleUser(
    id = UUID.fromString(USER_ID),
    username = "principal",
    email = "email",
    verified = true,
    firstName = "Joe",
    lastName = "Bloggs",
    enabled = true,
    source = AuthSource.auth,
    authorities = setOf(
      Authority(roleCode = "ROLE_TEST_1", roleName = "Test 1"),
      Authority(roleCode = "ROLE_TEST_2", roleName = "Test 2"),
    ),
  )

  companion object {
    private const val USER_ID = "07395ef9-53ec-4d6c-8bb1-0dc96cd4bd2f"
  }
}
