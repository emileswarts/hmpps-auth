package uk.gov.justice.digital.hmpps.oauth2server.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource.nomis

internal class ServiceUnavailableThreadLocalTest {

  @Test
  fun `threadLocal not set`() {
    assertThat(ServiceUnavailableThreadLocal.service).isNull()
  }

  @Test
  fun `threadLocal set with nomis value and then cleared`() {
    ServiceUnavailableThreadLocal.addService(nomis)
    assertThat(ServiceUnavailableThreadLocal.service).contains(nomis)
    ServiceUnavailableThreadLocal.clear()
    assertThat(ServiceUnavailableThreadLocal.service).isNull()
  }

  @Test
  fun `threatLocal null containsAuthSource returns false`() {
    assertThat(ServiceUnavailableThreadLocal.service).isNull()

    assertThat(ServiceUnavailableThreadLocal.containsAuthSource(nomis)).isFalse
    ServiceUnavailableThreadLocal.clear()
  }

  @Test
  fun `threatLocal containsAuthSource nomis value returns true`() {
    ServiceUnavailableThreadLocal.addService(nomis)
    assertThat(ServiceUnavailableThreadLocal.service).contains(nomis)

    assertThat(ServiceUnavailableThreadLocal.containsAuthSource(nomis)).isTrue
    ServiceUnavailableThreadLocal.clear()
  }
}
