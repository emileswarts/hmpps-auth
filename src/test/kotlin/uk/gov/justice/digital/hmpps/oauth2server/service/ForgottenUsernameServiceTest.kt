package uk.gov.justice.digital.hmpps.oauth2server.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserService
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetailsHelper.Companion.createSampleNomisUser
import uk.gov.justice.digital.hmpps.oauth2server.security.NomisUserService
import uk.gov.service.notify.NotificationClientApi

class ForgottenUsernameServiceTest {
  private val nomisUserService: NomisUserService = mock()
  private val authUserService: AuthUserService = mock()
  private val deliusUserService: DeliusUserService = mock()
  private val notificationClientApi: NotificationClientApi = mock()
  private val service = ForgottenUsernameService(
    deliusUserService,
    authUserService,
    nomisUserService,
    notificationClientApi,
    "emailTemplate",
    "emailNotFoundTemplate"
  )

  @Test
  fun `verify all auth nomis and delius are called`() {
    service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
  }

  @Test
  fun `Username not found for email address notify called with email not found template`() {
    service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail(
      "emailNotFoundTemplate",
      "a.user@justice.gov.uk",
      mapOf<String, Any>(),
      null
    )
  }

  @Test
  fun `username not found if auth user has not verified email address`() {
    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(
      listOf(
        UserHelper.createSampleUser(
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = false,
        )
      )
    )
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    assertThat(username).isEmpty()
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail(
      "emailNotFoundTemplate",
      "a.user@justice.gov.uk",
      mapOf<String, Any>(),
      null
    )
  }

  @Test
  fun `one username found for auth user with verified email address`() {
    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(
      listOf(
        UserHelper.createSampleUser(
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        )
      )
    )
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "first",
      "username" to listOf("firstlast"),
      "signinUrl" to "someurl/",
      "single" to "yes",
      "multiple" to "no"
    )

    assertThat(username).isEqualTo(listOf("firstlast"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `one username found for nomis user`() {
    val nomisUser = createSampleNomisUser(
      username = "username1",
      firstName = "Bob",
      lastName = "Smith",
      email = "a.user@justice.gov.uk",
    )
    whenever(nomisUserService.getNomisUsersByEmail(anyString())).thenReturn(listOf(nomisUser))
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "Bob",
      "username" to listOf("username1"),
      "signinUrl" to "someurl/",
      "single" to "yes",
      "multiple" to "no"
    )

    assertThat(username).isEqualTo(listOf("username1"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `one username found for Delius user`() {
    val deliusUser = DeliusUserPersonDetails("username2", "id", "user", "name", "email@email.com", true)
    whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "user",
      "username" to listOf("username2"),
      "signinUrl" to "someurl/",
      "single" to "yes",
      "multiple" to "no"
    )

    assertThat(username).isEqualTo(listOf("username2"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `multiple username found for auth user with verified email address`() {
    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(
      listOf(
        UserHelper.createSampleUser(
          username = "user1",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        ),
        UserHelper.createSampleUser(
          username = "user2",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        )
      )
    )
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "first",
      "username" to listOf("user1", "user2"),
      "signinUrl" to "someurl/",
      "single" to "no",
      "multiple" to "yes"
    )

    assertThat(username).isEqualTo(listOf("user1", "user2"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `single username found for auth user with verified and non verified email address`() {
    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(
      listOf(
        UserHelper.createSampleUser(
          username = "user1",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        ),
        UserHelper.createSampleUser(
          username = "user2",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = false,
        )
      )
    )
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "first",
      "username" to listOf("user1"),
      "signinUrl" to "someurl/",
      "single" to "yes",
      "multiple" to "no"
    )

    assertThat(username).isEqualTo(listOf("user1"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `single username found for auth user with enabled and not enabled accounts`() {
    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(
      listOf(
        UserHelper.createSampleUser(
          username = "user1",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        ),
        UserHelper.createSampleUser(
          username = "user2",
          email = "a.user@justice.gov.uk",
          enabled = false,
          verified = false,
        )
      )
    )
    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "first",
      "username" to listOf("user1"),
      "signinUrl" to "someurl/",
      "single" to "yes",
      "multiple" to "no"
    )

    assertThat(username).isEqualTo(listOf("user1"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }

  @Test
  fun `multiple usernames found for nomis, delius and auth user with verified email address`() {
    val authUser =
      listOf(
        UserHelper.createSampleUser(
          username = "user1",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        ),
        UserHelper.createSampleUser(
          username = "user2",
          email = "a.user@justice.gov.uk",
          enabled = true,
          verified = true,
        )
      )

    val nomisUser = createSampleNomisUser(
      username = "username1",
      userId = "",
      firstName = "Bob",
      lastName = "Smith",
      email = "a.user@justice.gov.uk",
    )
    val deliusUser = DeliusUserPersonDetails("username2", "id", "user", "name", "email@email.com", true)

    whenever(authUserService.findAuthUsersByEmail("a.user@justice.gov.uk")).thenReturn(authUser)
    whenever(nomisUserService.getNomisUsersByEmail(anyString())).thenReturn(listOf(nomisUser))
    whenever(deliusUserService.getDeliusUsersByEmail(anyString())).thenReturn(listOf(deliusUser))

    val username = service.forgottenUsername("a.user@justice.gov.uk", "someurl/forgotten-username")

    val map = mapOf(
      "firstName" to "first",
      "username" to listOf("user1", "user2", "username1", "username2"),
      "signinUrl" to "someurl/",
      "single" to "no",
      "multiple" to "yes"
    )

    assertThat(username).isEqualTo(listOf("user1", "user2", "username1", "username2"))
    verify(deliusUserService).getDeliusUsersByEmail("a.user@justice.gov.uk")
    verify(authUserService).findAuthUsersByEmail("a.user@justice.gov.uk")
    verify(nomisUserService).getNomisUsersByEmail("a.user@justice.gov.uk")
    verify(notificationClientApi).sendEmail("emailTemplate", "a.user@justice.gov.uk", map, null)
  }
}
