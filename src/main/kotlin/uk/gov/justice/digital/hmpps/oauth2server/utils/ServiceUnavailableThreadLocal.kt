package uk.gov.justice.digital.hmpps.oauth2server.utils

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import kotlin.concurrent.getOrSet

@Component
object ServiceUnavailableThreadLocal {
  private val ServiceUnavailableThreadLocal = ThreadLocal<MutableSet<AuthSource?>>()

  val service: MutableSet<AuthSource?>?
    get() = ServiceUnavailableThreadLocal.get()

  fun addService(authSource: AuthSource) {
    val service = (ServiceUnavailableThreadLocal.getOrSet { mutableSetOf() })
    service.add(authSource)
  }

  fun containsAuthSource(authSource: AuthSource): Boolean {
    return ServiceUnavailableThreadLocal.get()?.contains(authSource) ?: false
  }

  fun clear() {
    ServiceUnavailableThreadLocal.remove()
  }
}
