package uk.gov.justice.digital.hmpps.oauth2server.timed

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserRetries
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRetriesRepository
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeleteDisabledUsersServiceTest {
  private val userRepository: UserRepository = mock()
  private val userRetriesRepository: UserRetriesRepository = mock()
  private val telemetryClient: TelemetryClient = mock()

  @Captor
  private lateinit var mapCaptor: ArgumentCaptor<Map<String, String>>
  private val service: DeleteDisabledUsersService =
    DeleteDisabledUsersService(userRepository, userRetriesRepository, telemetryClient)

  @Test
  fun findAndDeleteDisabledUsers_Processed() {
    val users = listOf(createSampleUser(username = "user"), createSampleUser(username = "joe"))
    whenever(userRepository.findTop10ByLastLoggedInBeforeAndEnabledIsFalseOrderByLastLoggedIn(any()))
      .thenReturn(users)
    assertThat(service.processInBatches()).isEqualTo(2)
  }

  @Test
  fun findAndDeleteDisabledUsers_Deleted() {
    val user = createSampleUser(username = "user", id = UUID.randomUUID())
    val joe = createSampleUser(username = "joe", id = UUID.randomUUID())
    val users = listOf(user, joe)
    whenever(userRepository.findTop10ByLastLoggedInBeforeAndEnabledIsFalseOrderByLastLoggedIn(any()))
      .thenReturn(users)
    service.processInBatches()
    verify(userRepository).delete(user)
    verify(userRepository).delete(joe)
  }

  @Test
  fun findAndDeleteDisabledUsers_DeleteAll() {
    val user = createSampleUser(username = "user")
    user.createToken(UserToken.TokenType.RESET)
    val retry = UserRetries("user", 3)
    whenever(userRetriesRepository.findById(anyString())).thenReturn(Optional.of(retry))
    whenever(userRepository.findTop10ByLastLoggedInBeforeAndEnabledIsFalseOrderByLastLoggedIn(any()))
      .thenReturn(listOf(user))
    service.processInBatches()
    verify(userRetriesRepository).delete(retry)
  }

  @Test
  fun findAndDeleteDisabledUsers_Telemetry() {
    val users = listOf(createSampleUser(username = "user"), createSampleUser(username = "joe"))
    whenever(userRepository.findTop10ByLastLoggedInBeforeAndEnabledIsFalseOrderByLastLoggedIn(any()))
      .thenReturn(users)
    service.processInBatches()
    verify(telemetryClient, times(2)).trackEvent(
      ArgumentMatchers.eq("DeleteDisabledUsersProcessed"),
      mapCaptor.capture(),
      ArgumentMatchers.isNull()
    )
    assertThat(mapCaptor.allValues.map { it["username"] }).containsExactly("user", "joe")
  }
}
