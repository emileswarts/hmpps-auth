package uk.gov.justice.digital.hmpps.oauth2server.timed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthCodeRepository
import java.time.LocalDateTime

class RemoveExpiredAuthCodesServiceTest {
  val repository: OauthCodeRepository = mock()
  val removeExpiredAuthCodesService = RemoveExpiredAuthCodesService(repository)

  @Test
  fun removeExpiredAuthCodes() {
    removeExpiredAuthCodesService.removeExpiredAuthCodes()
    verify(repository).deleteByCreatedDateBefore(
      check {
        val now = LocalDateTime.now()
        assertThat(it).isBetween(now.minusDays(1).minusMinutes(1), now.minusDays(1).plusMinutes(1))
      }
    )
  }
}
