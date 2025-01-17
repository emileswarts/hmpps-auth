package uk.gov.justice.digital.hmpps.oauth2server.security

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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.azure.AzureUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.azure.service.AzureUserService
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.CreateTokenRequest
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserSummaryDto
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.PrisonCaseload
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.auth
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.azuread
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.delius
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.none
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import java.util.Optional
import java.util.UUID

class UserServiceTest {
  private val nomisUserService: NomisUserService = mock()
  private val authUserService: AuthUserService = mock()
  private val deliusUserService: DeliusUserService = mock()
  private val azureUserService: AzureUserService = mock()
  private val userRepository: UserRepository = mock()
  private val verifyEmailService: VerifyEmailService = mock()
  private val userService = UserService(
    nomisUserService,
    authUserService,
    deliusUserService,
    azureUserService,
    userRepository,
    verifyEmailService
  )

  @Nested
  inner class FindMasterUserPersonDetails {
    @Test
    fun `findMasterUserPersonDetails auth user`() {
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(createUser())
      val user = userService.findMasterUserPersonDetails("   bob   ")
      assertThat(user).isPresent.get().extracting { it.username }.isEqualTo("someuser")
    }

    @Test
    fun `findMasterUserPersonDetails nomis user`() {
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      val user = userService.findMasterUserPersonDetails("bob")
      assertThat(user).isPresent.get().extracting { it.username }.isEqualTo("nomisuser")
    }

    @Test
    fun `findMasterUserPersonDetails delius user`() {
      whenever(deliusUserService.getDeliusUserByUsername(anyString())).thenReturn(deliusUserAccountForBob)
      val user = userService.findMasterUserPersonDetails("bob")
      assertThat(user).isPresent.get().extracting { it.username }.isEqualTo("deliusUser")
    }

    @Test
    fun `findMasterUserPersonDetails azure user`() {
      whenever(azureUserService.getAzureUserByUsername(anyString())).thenReturn(azureUserAccount)
      val user = userService.findMasterUserPersonDetails("bob")
      assertThat(user).isPresent.get().extracting { it.username }.isEqualTo("D6165AD0-AED3-4146-9EF7-222876B57549")
    }
  }

  @Nested
  inner class FindEnabledOrNomisLockedUserPersonDetails {
    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails auth user`() {
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(
        Optional.of(
          createSampleUser(
            "someuser",
            enabled = true
          )
        )
      )
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("   bob   ")
      assertThat(user?.username).isEqualTo("someuser")
    }

    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails nomis user is enabled`() {
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("bob")
      assertThat(user?.username).isEqualTo("nomisuser")
    }

    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails nomis user is locked`() {
      val staffUserAccountForBobLocked =
        createSampleNomisUser(
          firstName = "bOb",
          lastName = "bloggs",
          username = "nomisuser",
          enabled = false,
          accountStatus = AccountStatus.LOCKED
        )
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffUserAccountForBobLocked)
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("bob")
      assertThat(user?.username).isEqualTo("nomisuser")
    }

    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails delius user`() {
      whenever(deliusUserService.getDeliusUserByUsername(anyString())).thenReturn(deliusUserAccountForBob)
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("bob")
      assertThat(user?.username).isEqualTo("deliusUser")
    }

    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails azure user`() {
      whenever(azureUserService.getAzureUserByUsername(anyString())).thenReturn(azureUserAccount)
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("bob")
      assertThat(user?.username).isEqualTo("D6165AD0-AED3-4146-9EF7-222876B57549")
    }

    @Test
    fun `findEnabledOrNomisLockedUserPersonDetails delius user auth user disabled`() {
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(createUser())
      whenever(deliusUserService.getDeliusUserByUsername(anyString())).thenReturn(deliusUserAccountForBob)
      val user = userService.findEnabledOrNomisLockedUserPersonDetails("bob")
      assertThat(user?.username).isEqualTo("deliusUser")
    }
  }

  @Nested
  inner class GetEmailAddressFromNomis {
    @Test
    fun `getEmailAddressFromNomis no matching user`() {
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(null)
      val optionalAddress = userService.getEmailAddressFromNomis("joe")
      assertThat(optionalAddress).isEmpty
    }

    @Test
    fun `getEmailAddressFromNomis no email address`() {
      val user = staffNomisApiUserAccountForBob.copy(email = null)
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(user)
      val optionalAddress = userService.getEmailAddressFromNomis("joe")
      assertThat(optionalAddress).isEmpty
    }

    @Test
    fun `getEmailAddressFromNomis hmps gsi email`() {
      val user = staffNomisApiUserAccountForBob.copy(email = "a@hmps.gsi.gov.uk")
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(user)
      val optionalAddress = userService.getEmailAddressFromNomis("joe")
      assertThat(optionalAddress).isEmpty
    }

    @Test
    fun `getEmailAddressFromNomis valid email`() {
      val user = staffNomisApiUserAccountForBob.copy(email = "Bob.smith@justice.gov.uk")
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(user)
      val optionalAddress = userService.getEmailAddressFromNomis("joe")
      assertThat(optionalAddress).hasValue("Bob.smith@justice.gov.uk")
    }
  }

  @Nested
  inner class GetOrCreateUser {

    @Test
    fun `getOrCreateUser user exists already`() {
      val user = createSampleUser("joe")
      whenever(userRepository.findByUsername("JOE")).thenReturn(Optional.of(user))
      val newUserOpt = userService.getOrCreateUser("joe")
      assertThat(newUserOpt).hasValueSatisfying {
        assertThat(it.username).isEqualTo("joe")
      }
    }

    @Test
    fun `getOrCreateUser migrate from NOMIS`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(Optional.empty())
      whenever(nomisUserService.getNomisUserByUsername("joe"))
        .thenReturn(createSampleNomisUser(username = "joe"))
      whenever(userRepository.save<User>(any())).thenAnswer { it.arguments[0] }

      val newUser = userService.getOrCreateUser("joe")
      assertThat(newUser).hasValueSatisfying {
        assertThat(it.username).isEqualTo("joe")
      }
    }

    @Test
    fun `getOrCreateUser migrate from NOMIS with email`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(Optional.empty())
      whenever(nomisUserService.getNomisUserByUsername("joe"))
        .thenReturn(createSampleNomisUser(username = "joe", email = "a@b.justice.gov.uk"))
      whenever(userRepository.save<User>(any())).thenAnswer { it.arguments[0] }

      val newUser = userService.getOrCreateUser("joe")
      assertThat(newUser).hasValueSatisfying {
        assertThat(it.username).isEqualTo("joe")
        assertThat(it.email).isEqualTo("a@b.justice.gov.uk")
        assertThat(it.verified).isTrue
        assertThat(it.authSource).isEqualTo(nomis.name)
      }
    }
  }

  @Nested
  inner class GetOrCreateUsers {

    @Test
    fun `user exists and other not found`() {
      val user = createSampleUser("joe")
      whenever(userRepository.findByUsername("JOE")).thenReturn(Optional.of(user))
      val newUserOpt = userService.getOrCreateUsers(listOf("joe", "fred"))
      assertThat(newUserOpt).containsExactly(user)
    }

    @Test
    fun `migrate from NOMIS`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(Optional.empty())
      whenever(nomisUserService.getNomisUserByUsername("joe"))
        .thenReturn(createSampleNomisUser(username = "joe"))
      whenever(userRepository.save<User>(any())).thenAnswer { it.arguments[0] }

      val newUser = userService.getOrCreateUsers(listOf("joe"))
      assertThat(newUser).hasSize(1)
    }
  }

  @Nested
  inner class FindUser {
    @Test
    fun findUser() {
      val user = createUser()
      whenever(userRepository.findByUsername(anyString())).thenReturn(user)
      val found = userService.findUser("bob")
      assertThat(found).isSameAs(user)
      verify(userRepository).findByUsername("BOB")
    }
  }

  @Nested
  inner class GetUser {
    @Test
    fun `getUser found`() {
      val user = createUser()
      whenever(userRepository.findByUsername(anyString())).thenReturn(user)
      val found = userService.getUser("bob")
      assertThat(found).isSameAs(user.orElseThrow())
      verify(userRepository).findByUsername("BOB")
    }

    @Test
    fun `getUser not found`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      assertThatThrownBy { userService.getUser("bob") }.isInstanceOf(UsernameNotFoundException::class.java)
    }
  }

  @Nested
  inner class GetUserById {
    val userId: UUID = UUID.randomUUID()

    @Test
    fun `getUserById found`() {
      val user = createUser()
      whenever(userRepository.findById(userId)).thenReturn(user)

      val found = userService.getUserById(userId)

      assertThat(found).isSameAs(user.orElseThrow())
      verify(userRepository).findById(userId)
    }

    @Test
    fun `getUserById not found`() {
      whenever(userRepository.findById(userId)).thenReturn(Optional.empty())
      assertThatThrownBy { userService.getUserById(userId) }.isInstanceOf(UserNotFoundException::class.java)
    }
  }

  @Nested
  inner class HasVerifiedMfaMethod {
    @Test
    fun `hasVerifiedMfaMethod success`() {
      val user = createSampleUser(username = "joe", email = "someemail", verified = true)
      assertThat(userService.hasVerifiedMfaMethod(user)).isTrue
    }

    @Test
    fun `hasVerifiedMfaMethod no email`() {
      val user = createSampleUser(username = "joe", verified = true)
      assertThat(userService.hasVerifiedMfaMethod(user)).isFalse
    }

    @Test
    fun `hasVerifiedMfaMethod not verified`() {
      val user = createSampleUser(username = "joe", email = "someemail")
      assertThat(userService.hasVerifiedMfaMethod(user)).isFalse
    }
  }

  @Nested
  inner class IsSameAsCurrentVerifiedMobile {

    @Test
    fun `isSameAsCurrentVerifiedMobile not verified`() {
      val user = createSampleUser(mobile = "07700900001")
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedMobile("someuser", "")
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedMobile new different mobile number`() {
      val user = createSampleUser(mobile = "07700900001", mobileVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedMobile("someuser", "07700900000")
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedMobile new different mobile number with whitespace`() {
      val user = createSampleUser(mobile = "07700900001", mobileVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedMobile("someuser", "0770 090 0000")
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedMobile same mobile number`() {
      val user = createSampleUser(mobile = "07700900000", mobileVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedMobile("someuser", "07700900000")
      assertThat(returnValue).isTrue
    }

    @Test
    fun `isSameAsCurrentVerifiedMobile same mobile number with whitespace`() {
      val user = createSampleUser(mobile = "07700900000", mobileVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedMobile("someuser", "0770 090 0000")
      assertThat(returnValue).isTrue
    }
  }

  @Nested
  inner class isSameAsCurrentVerifiedEmail {
    @Test
    fun `isSameAsCurrentVerifiedEmail not verified primary email`() {
      val user = createSampleUser(email = "someemail", verified = false)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "someemail", User.EmailType.PRIMARY)
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedEmail new different email address primary email`() {
      val user = createSampleUser(email = "someemail", verified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "somenewemail", User.EmailType.PRIMARY)
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedEmail same address primary email`() {
      val user = createSampleUser(email = "someemail", verified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "someemail", User.EmailType.PRIMARY)
      assertThat(returnValue).isTrue
    }

    @Test
    fun `isSameAsCurrentVerifiedEmail not verified secondary email`() {
      val user = createSampleUser(secondaryEmail = "someemail")
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "someemail", User.EmailType.SECONDARY)
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedEmail new different email address secondary email`() {
      val user = createSampleUser(secondaryEmail = "someemail", secondaryEmailVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "somenewemail", User.EmailType.SECONDARY)
      assertThat(returnValue).isFalse
    }

    @Test
    fun `isSameAsCurrentVerifiedEmail same address secondary email`() {
      val user = createSampleUser(secondaryEmail = "someemail", secondaryEmailVerified = true)
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user))
      val returnValue = userService.isSameAsCurrentVerifiedEmail("someuser", "someemail", User.EmailType.SECONDARY)
      assertThat(returnValue).isTrue
    }
  }

  @Nested
  inner class FindPrisonUsersByFirstAndLastNames {
    @Test
    fun `no matches`() {
      whenever(nomisUserService.findPrisonUsersByFirstAndLastNames("first", "last")).thenReturn(listOf())
      whenever(authUserService.findAuthUsersByUsernames(listOf())).thenReturn(listOf())

      assertThat(userService.findPrisonUsersByFirstAndLastNames("first", "last")).isEmpty()
    }

    @Test
    fun `prison users only`() {

      whenever(authUserService.findAuthUsersByUsernames(listOf())).thenReturn(listOf())

      whenever(nomisUserService.findPrisonUsersByFirstAndLastNames("first", "last")).thenReturn(
        listOf(
          NomisUserSummaryDto("U1", "1", "F1", "l1", false, null, "u1@justice.gov.uk"),
          NomisUserSummaryDto("U2", "2", "F2", "l2", false, null, null),
          NomisUserSummaryDto("U3", "3", "F3", "l3", false, PrisonCaseload("MDI", "Moorland"), null)
        )
      )

      assertThat(userService.findPrisonUsersByFirstAndLastNames("first", "last"))
        .containsExactlyInAnyOrder(
          PrisonUserDto(
            username = "U1",
            email = "u1@justice.gov.uk",
            verified = true,
            userId = "1",
            firstName = "F1",
            lastName = "l1",
            activeCaseLoadId = null
          ),
          PrisonUserDto(
            username = "U2",
            email = null,
            verified = false,
            userId = "2",
            firstName = "F2",
            lastName = "l2",
            activeCaseLoadId = null
          ),
          PrisonUserDto(
            username = "U3",
            email = null,
            verified = false,
            userId = "3",
            firstName = "F3",
            lastName = "l3",
            activeCaseLoadId = "MDI"
          ),
        )
    }

    @Test
    fun `Prison users matched in auth`() {
      whenever(nomisUserService.findPrisonUsersByFirstAndLastNames("first", "last")).thenReturn(
        listOf(
          NomisUserSummaryDto("U1", "1", "F1", "l1", false, PrisonCaseload("MDI", "Moorland"), null),
          NomisUserSummaryDto("U2", "2", "F2", "l2", false, null, null),
          NomisUserSummaryDto("U3", "3", "F3", "l3", false, PrisonCaseload("MDI", "Moorland"), null)
        )
      )

      whenever(authUserService.findAuthUsersByUsernames(anyList())).thenReturn(
        listOf(
          createSampleUser(verified = true, source = nomis, username = "U1", email = "u1@b.com"),
          createSampleUser(verified = true, source = nomis, username = "U2", email = "u2@b.com"),
          createSampleUser(verified = false, source = nomis, username = "U3", email = "u3@b.com"),
        )
      )

      assertThat(userService.findPrisonUsersByFirstAndLastNames("first", "last"))
        .containsExactlyInAnyOrder(
          PrisonUserDto(
            username = "U1",
            email = "u1@b.com",
            verified = true,
            userId = "1",
            firstName = "F1",
            lastName = "l1",
            activeCaseLoadId = "MDI"
          ),
          PrisonUserDto(
            username = "U2",
            email = "u2@b.com",
            verified = true,
            userId = "2",
            firstName = "F2",
            lastName = "l2",
            activeCaseLoadId = null

          ),
          PrisonUserDto(
            username = "U3",
            email = "u3@b.com",
            verified = false,
            userId = "3",
            firstName = "F3",
            lastName = "l3",
            activeCaseLoadId = "MDI"
          ),
        )
    }

    @Test
    fun `Prison users partially matched in auth`() {

      whenever(nomisUserService.findPrisonUsersByFirstAndLastNames("first", "last")).thenReturn(
        listOf(
          NomisUserSummaryDto("U1", "1", "F1", "l1", false, PrisonCaseload("MDI", "Moorland"), null),
          NomisUserSummaryDto("U2", "2", "F2", "l2", false, null, "u2@justice.gov.uk"),
          NomisUserSummaryDto("U3", "3", "F3", "l3", false, null, "u3@justice.gov.uk"),
          NomisUserSummaryDto("U4", "4", "F4", "l4", false, PrisonCaseload("MDI", "Moorland"), null)
        )
      )

      whenever(authUserService.findAuthUsersByUsernames(anyList())).thenReturn(
        listOf(
          createSampleUser(verified = true, source = nomis, username = "U1", email = "u1@b.com"),
          // User U2 in auth, but no email - so search NOMIS for e-mail for this user
          createSampleUser(verified = true, source = nomis, username = "U2", email = null),
          // User U3 found in auth, but source is not nomis
          createSampleUser(verified = true, source = auth, username = "U3", email = "u3@b.com"),
        )
      )

      assertThat(userService.findPrisonUsersByFirstAndLastNames("first", "last"))
        .containsExactlyInAnyOrder(
          PrisonUserDto(
            username = "U1",
            email = "u1@b.com",
            verified = true,
            userId = "1",
            firstName = "F1",
            lastName = "l1",
            activeCaseLoadId = "MDI"
          ),
          PrisonUserDto(
            username = "U2",
            email = "u2@justice.gov.uk",
            verified = true,
            userId = "2",
            firstName = "F2",
            lastName = "l2",
            activeCaseLoadId = null
          ),
          PrisonUserDto(
            username = "U3",
            email = "u3@justice.gov.uk",
            verified = true,
            userId = "3",
            firstName = "F3",
            lastName = "l3",
            activeCaseLoadId = null
          ),
          PrisonUserDto(
            username = "U4",
            email = null,
            verified = false,
            userId = "4",
            firstName = "F4",
            lastName = "l4",
            activeCaseLoadId = "MDI"
          ),
        )
    }
  }

  @Nested
  inner class GetMasterUserPersonDetailsWithEmailCheck {
    private val loginDetails = createSampleUser("user", verified = true, email = "joe@fred.com")

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - auth user`() {
      val authUser =
        Optional.of(createSampleUser(username = "bob", verified = true, email = "joe@fred.com"))
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(authUser)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", auth, loginDetails)
      assertThat(details).isEqualTo(authUser)
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - auth user email not verified`() {
      val authUser =
        Optional.of(createSampleUser(username = "bob", verified = false, email = "joe@fred.com"))
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(authUser)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", auth, loginDetails)
      assertThat(details).isEmpty
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - auth user not matched`() {
      val authUser =
        Optional.of(createSampleUser(username = "bob", verified = true, email = "harold@henry.com"))
      whenever(authUserService.getAuthUserByUsername(anyString())).thenReturn(authUser)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", auth, loginDetails)
      assertThat(details).isEmpty
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - nomis user verified email in auth`() {
      val nomisUserInAuth =
        Optional.of(createSampleUser(username = "bob", verified = true, email = "joe@fred.com", source = nomis))
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      whenever(verifyEmailService.getEmail(anyString())).thenReturn(nomisUserInAuth)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", nomis, loginDetails)
      assertThat(details).isEqualTo(Optional.of(staffNomisApiUserAccountForBob))
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - nomis user not verified email in auth`() {
      val loginDetails = createSampleUser("user", verified = true, email = "b.h@somewhere.com")
      val nomisUserInAuth =
        Optional.of(createSampleUser(username = "bob", verified = false, email = "b.h@somewhere.com", source = nomis))
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      whenever(verifyEmailService.getEmail(anyString())).thenReturn(nomisUserInAuth)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", nomis, loginDetails)
      assertThat(details).isEqualTo(Optional.of(staffNomisApiUserAccountForBob))
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - nomis user`() {
      val loginDetails = createSampleUser("user", verified = true, email = "b.h@somewhere.com")
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", nomis, loginDetails)
      assertThat(details).isEqualTo(Optional.of(staffNomisApiUserAccountForBob))
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - nomis user not matched`() {
      whenever(nomisUserService.getNomisUserByUsername(anyString())).thenReturn(staffNomisApiUserAccountForBob)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", nomis, loginDetails)
      assertThat(details).isEmpty
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - delius user`() {
      whenever(deliusUserService.getDeliusUserByUsername(anyString())).thenReturn(deliusUserAccountForBob)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck(
        "user", delius,
        createSampleUser("user", verified = true, email = "a@b.com")
      )
      assertThat(details).isEqualTo(deliusUserAccountForBob)
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - delius user not matched`() {
      whenever(deliusUserService.getDeliusUserByUsername(anyString())).thenReturn(deliusUserAccountForBob)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", delius, loginDetails)
      assertThat(details).isEmpty
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - azuread user`() {
      whenever(azureUserService.getAzureUserByUsername(anyString())).thenReturn(azureUserAccount)
      val details = userService.getMasterUserPersonDetailsWithEmailCheck(
        "user", azuread,
        createSampleUser("user", verified = true, email = "joe.bloggs@justice.gov.uk")
      )
      assertThat(details).isEqualTo(azureUserAccount)
    }

    @Test
    fun `test getMasterUserPersonDetailsWithEmailCheck - none`() {
      val details = userService.getMasterUserPersonDetailsWithEmailCheck("user", none, loginDetails)
      assertThat(details).isEmpty
    }
  }

  @Nested
  inner class UserSearchMultipleSources {
    @Test
    fun `test search user with multiple auth sources `() {
      val unpaged = Pageable.unpaged()
      whenever(
        authUserService.findAuthUsers(
          anyString(),
          anyOrNull(),
          anyOrNull(),
          any(),
          anyString(),
          anyList(),
          any(),
          any()
        )
      )
        .thenReturn(Page.empty())

      userService.searchUsersInMultipleSourceSystems(
        "test", unpaged, "bob", AUTHORITY_INTEL_ADMIN, UserFilter.Status.ALL, listOf(nomis, auth)
      )

      verify(authUserService).findAuthUsers(
        "test",
        emptyList(),
        emptyList(),
        unpaged,
        "bob",
        AUTHORITY_INTEL_ADMIN,
        UserFilter.Status.ALL,
        listOf(nomis, auth)
      )
    }

    @Test
    fun `test search user with default auth source when not provided`() {
      val unpaged = Pageable.unpaged()
      whenever(
        authUserService.findAuthUsers(
          anyString(),
          anyOrNull(),
          anyOrNull(),
          any(),
          anyString(),
          anyList(),
          any(),
          any()
        )
      )
        .thenReturn(Page.empty())

      userService.searchUsersInMultipleSourceSystems(
        "test", unpaged, "bob", AUTHORITY_INTEL_ADMIN, UserFilter.Status.ALL, null
      )

      verify(authUserService).findAuthUsers(
        "test", emptyList(), emptyList(), unpaged, "bob", AUTHORITY_INTEL_ADMIN, UserFilter.Status.ALL, listOf(auth)
      )
    }
  }

  @Nested
  inner class CreateUsersWithEmailAndUserName {
    @Test
    fun `createUser with username, email & source`() {
      whenever(userRepository.findByUsername(anyString())).thenReturn(Optional.empty())
      whenever(userRepository.save<User>(any())).thenAnswer { it.arguments[0] }
      val createTokenRequest = CreateTokenRequest("joe", "joe@gov.uk", nomis, "joe", "Smith")
      val newUser = userService.createUser(createTokenRequest)
      assertThat(newUser).hasValueSatisfying {
        assertThat(it.username).isEqualTo("JOE")
        assertThat(it.email).isEqualTo("joe@gov.uk")
        assertThat(it.source).isEqualTo(nomis)
        assertThat(it.verified).isEqualTo(false)
        assertThat(it.firstName).isEqualTo("joe")
      }
    }

    @Test
    fun `Return existing user for createUser with user same details`() {
      val stubUser = User("joe", email = "joe@gov.uk", source = nomis)
      whenever(userRepository.findByUsername("JOE")).thenReturn(Optional.of(stubUser))
      whenever(userRepository.save<User>(any())).thenAnswer { it.arguments[0] }
      val createTokenRequest = CreateTokenRequest("joe", "joe@gov.uk", nomis, "joe", "Smith")
      val newUser = userService.createUser(createTokenRequest)
      assertThat(newUser).hasValueSatisfying {
        assertThat(it.username).isEqualTo("joe")
        assertThat(it.email).isEqualTo("joe@gov.uk")
        assertThat(it.source).isEqualTo(nomis)
        assertThat(it.verified).isEqualTo(false)
        assertThat(it.firstName).isEqualTo("joe")
      }
    }
  }

  private fun createUser() = Optional.of(createSampleUser(username = "someuser"))

  private val staffNomisApiUserAccountForBob: NomisUserPersonDetails
    get() =
      createSampleNomisUser(
        firstName = "bOb",
        lastName = "bloggs",
        username = "nomisuser",
        enabled = true,
        accountStatus = AccountStatus.OPEN
      )

  private val deliusUserAccountForBob =
    Optional.of(DeliusUserPersonDetails("deliusUser", "12345", "Delius", "Smith", "a@b.com", true, false, setOf()))

  private val azureUserAccount =
    Optional.of(
      AzureUserPersonDetails(
        ArrayList(),
        true,
        "D6165AD0-AED3-4146-9EF7-222876B57549",
        "Joe",
        "Bloggs",
        "joe.bloggs@justice.gov.uk",
        true,
        accountNonExpired = true,
        accountNonLocked = true
      )
    )

  companion object {
    private val AUTHORITY_INTEL_ADMIN: Set<GrantedAuthority> =
      setOf(SimpleGrantedAuthority("ROLE_INTEL_ADMIN"))
  }
}
