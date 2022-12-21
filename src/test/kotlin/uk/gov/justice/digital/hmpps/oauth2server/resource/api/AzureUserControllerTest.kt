package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.azure.AzureUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.azure.service.AzureUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.UserDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.util.Collections.emptyList
import java.util.Optional
import java.util.UUID

class AzureUserControllerTest {
  private val azureUserService: AzureUserService = mock()
  private val userService: UserService = mock()
  private val azureUserController = AzureUserController(userService, azureUserService)

  @Test
  fun user_azureUser() {
    setupFindUserCallForAzure()
    val user = azureUserController.user(UUID.randomUUID())
    assertThat(user).usingRecursiveComparison().isEqualTo(
      UserDetail(
        USER_ID,
        true,
        "Azure User",
        AuthSource.azuread,
        null,
        null,
        "azureuser@justice.gov.uk",
        UUID.fromString(USER_ID),
      )
    )
  }

  @Test
  fun user_userNotFound() {
    whenever(azureUserService.getAzureUserByUsername(anyString())).thenReturn(Optional.empty())

    assertThatThrownBy { azureUserController.user(UUID.randomUUID()) }
      .isInstanceOf(UsernameNotFoundException::class.java)
  }

  private fun setupFindUserCallForAzure() {
    whenever(azureUserService.getAzureUserByUsername(anyString())).thenReturn(azurePersonDetails)
    whenever(userService.getOrCreateUser(anyString())).thenReturn(Optional.of(createAzureUser()))
  }

  private val azurePersonDetails: Optional<AzureUserPersonDetails>
    get() = Optional.of(
      AzureUserPersonDetails(
        emptyList(),
        true,
        "917D4BDC-F86F-4756-B828-0BED8865EFB3",
        "Azure",
        "User",
        "azureuser@justice.gov.uk",
        credentialsNonExpired = true,
        accountNonExpired = true,
        accountNonLocked = true
      )
    )

  private fun createAzureUser() = createSampleUser(
    id = UUID.fromString(USER_ID),
    username = USER_ID,
    email = "email",
    verified = true,
    firstName = "Azure",
    lastName = "User",
    enabled = true,
    source = AuthSource.azuread,
    authorities = setOf(),
  )

  companion object {
    private const val USER_ID = "917D4BDC-F86F-4756-B828-0BED8865EFB3"
  }
}
