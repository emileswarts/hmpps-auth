package uk.gov.justice.digital.hmpps.oauth2server.timed

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class NotifyPreDisableAuthUsersTest {
  private val notifyPreDisableAuthUsersService: NotifyPreDisableAuthUsersService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private var notifyPreDisableAuthUsers =
    NotifyPreDisableAuthUsers(notifyPreDisableAuthUsersService, telemetryClient, true)
  private var notifyPreDisableAuthUsersDisabled =
    NotifyPreDisableAuthUsers(notifyPreDisableAuthUsersService, telemetryClient, false)

  @Test
  fun findAndNotifyPreDisableInactiveAuthUsers() {
    whenever(notifyPreDisableAuthUsersService.processInBatches()).thenReturn(0)
    notifyPreDisableAuthUsers.findAndNotifyPreDisableInactiveAuthUsers()
    verify(notifyPreDisableAuthUsersService).processInBatches()
  }

  @Test
  fun findAndNotifyPreDisableInactiveAuthUsersDisabled() {
    notifyPreDisableAuthUsersDisabled.findAndNotifyPreDisableInactiveAuthUsers()
    verifyNoInteractions(notifyPreDisableAuthUsersService)
  }
}
