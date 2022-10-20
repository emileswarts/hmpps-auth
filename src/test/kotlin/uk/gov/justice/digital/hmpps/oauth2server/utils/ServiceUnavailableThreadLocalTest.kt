package uk.gov.justice.digital.hmpps.oauth2server.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource

internal class ServiceUnavailableThreadLocalTest {

  @Test
  fun `threadLocal not set`() {
    assertThat(ServiceUnavailableThreadLocal.service).isNull()
  }

  @Test
  fun `threadLocal set with nomis value and then cleared`() {
    ServiceUnavailableThreadLocal.addService(AuthSource.nomis)
    assertThat(ServiceUnavailableThreadLocal.service).contains(AuthSource.nomis)
    ServiceUnavailableThreadLocal.clear()
    assertThat(ServiceUnavailableThreadLocal.service).isNull()
  }
}
