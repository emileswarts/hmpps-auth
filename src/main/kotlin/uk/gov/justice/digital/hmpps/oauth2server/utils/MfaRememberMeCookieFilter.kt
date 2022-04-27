package uk.gov.justice.digital.hmpps.oauth2server.utils

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.justice.digital.hmpps.oauth2server.resource.MfaRememberMeCookieHelper
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(2)
class MfaRememberMeCookieFilter(
  private val mfaRememberMeCookieHelper: MfaRememberMeCookieHelper,
) : OncePerRequestFilter() {

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val cookieValue = mfaRememberMeCookieHelper.readValueFromCookie(request).orElse(null)
    MfaRememberMeContext.token = cookieValue
    filterChain.doFilter(request, response)
    MfaRememberMeContext.token = null
  }

  /** Only filter for oauth authorize requests **/
  override fun shouldNotFilter(request: HttpServletRequest): Boolean =
    !request.servletPath.startsWith("/oauth/authorize")
}

@Component
object MfaRememberMeContext {
  private val tokenThreadLocal = ThreadLocal<String>()

  var token: String?
    get() = tokenThreadLocal.get()
    set(value) = tokenThreadLocal.set(value)
}
