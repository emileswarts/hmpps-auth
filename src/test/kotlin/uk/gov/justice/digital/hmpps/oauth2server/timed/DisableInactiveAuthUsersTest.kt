package uk.gov.justice.digital.hmpps.oauth2server.timed

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DisableInactiveAuthUsersTest {
  private val service: DisableInactiveAuthUsersService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private var disableInactiveAuthUsers = DisableInactiveAuthUsers(service, telemetryClient)

  @Test
  fun findAndDisableInactiveAuthUsers() {
    whenever(service.processInBatches()).thenReturn(0)
    disableInactiveAuthUsers.findAndDisableInactiveAuthUsers()
    verify(service).processInBatches()
  }
}
