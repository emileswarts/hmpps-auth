package uk.gov.justice.digital.hmpps.oauth2server.config

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class RedirectingLoginUrlAuthenticationEntryPointTest {
  private val entryPoint = RedirectingLoginUrlAuthenticationEntryPoint("/sign-in")

  private val request: HttpServletRequest = mock()
  private val response: HttpServletResponse = mock()

  @Test
  fun `no redirect uri leaves login url alone`() {
    whenever(request.scheme).thenReturn("https")
    whenever(request.serverName).thenReturn("localhost")
    whenever(request.serverPort).thenReturn(12345)
    whenever(response.encodeRedirectURL(anyString())).thenReturn("encoded")
    entryPoint.commence(request, response, null)
    verify(response).encodeRedirectURL("https://localhost:12345/sign-in")
    verify(response).sendRedirect("encoded")
  }

  @Test
  fun `redirect uri is added to login url`() {
    whenever(request.getParameter("redirect_uri")).thenReturn("some?parameter&value")
    whenever(request.scheme).thenReturn("https")
    whenever(request.serverName).thenReturn("localhost")
    whenever(request.serverPort).thenReturn(12345)
    whenever(response.encodeRedirectURL(anyString())).thenReturn("encoded")
    entryPoint.commence(request, response, null)
    verify(response).encodeRedirectURL("https://localhost:12345/sign-in?redirect_uri=some?parameter%26value")
    verify(response).sendRedirect("encoded")
  }
}
