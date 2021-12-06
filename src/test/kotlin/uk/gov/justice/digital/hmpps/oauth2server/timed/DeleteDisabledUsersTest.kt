package uk.gov.justice.digital.hmpps.oauth2server.timed

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeleteDisabledUsersTest {
  private val service: DeleteDisabledUsersService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val deleteDisabledUsers: DeleteDisabledUsers = DeleteDisabledUsers(service, telemetryClient)

  @Test
  fun findAndDeleteDisabledUsers() {
    whenever(service.processInBatches()).thenReturn(0)
    deleteDisabledUsers.findAndDeleteDisabledUsers()
    verify(service).processInBatches()
  }
}
