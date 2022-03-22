package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.justice.digital.hmpps.oauth2server.config.AllowedIpException

class AuthIpSecurityTest {
  @Nested
  inner class IpCheck {
    @Test
    fun testStandardV4IP() {
      val request = MockHttpServletRequest()
      request.remoteAddr = "127.0.0.1"
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      val check = testClass.check(request)
      assertThat(check).isTrue
    }

    @Test
    fun testRemoteAddressWithPort() {
      val request = MockHttpServletRequest()
      request.remoteAddr = "82.34.12.11:13321"
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      val check = testClass.check(request)
      assertThat(check).isTrue
    }

    @Test
    fun testRemoteAddressWithPortNoInAllowlist() {
      val request = MockHttpServletRequest()
      request.remoteAddr = "82.34.12.11:13321"
      val testClass = AuthIpSecurity(setOf("82.34.12.10/32", "82.34.12.12/32"))
      val check = testClass.check(request)
      assertThat(check).isFalse
    }

    @Test
    fun testIpV6Address() {
      val request = MockHttpServletRequest()
      request.remoteAddr = "0:0:0:0:0:0:0:1"
      val testClass = AuthIpSecurity(setOf("0:0:0:0:0:0:0:1", "127.0.0.1/32"))
      val check = testClass.check(request)
      assertThat(check).isTrue
    }
  }

  @Nested
  inner class ClientIpCheck {
    @Test
    fun testStandardV4IP() {
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      assertThatCode {
        testClass.validateClientIpAllowed("127.0.0.1", listOf("127.0.0.1"))
      }.doesNotThrowAnyException()
    }

    @Test
    fun testRemoteAddressNotInAllowlist() {
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      assertThatThrownBy {
        testClass.validateClientIpAllowed(
          "82.34.12.11",
          listOf("82.34.12.10/32", "82.34.12.12/32")
        )
      }
        .isInstanceOf(
          AllowedIpException::class.java
        ).hasMessage("Unable to issue token as request is not from ip within allowed list")
    }

    @Test
    fun testIpV6Address() {
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      val check = testClass.validateClientIpAllowed("0:0:0:0:0:0:0:1", listOf("0:0:0:0:0:0:0:1", "127.0.0.1/32"))
      assertThatCode {
        testClass.validateClientIpAllowed("0:0:0:0:0:0:0:1", listOf("0:0:0:0:0:0:0:1", "127.0.0.1/32"))
      }.doesNotThrowAnyException()
    }

    @Test
    fun testRemoteIpV6AddressNotInAllowlist() {
      val testClass = AuthIpSecurity(setOf("0.0.0.0/0"))
      assertThatThrownBy {
        testClass.validateClientIpAllowed(
          "0:0:0:0:0:0:0:1",
          listOf("0:0:0:0:0:0:0:2", "82.34.12.10/32", "82.34.12.12/32")
        )
      }
        .isInstanceOf(
          AllowedIpException::class.java
        ).hasMessage("Unable to issue token as request is not from ip within allowed list")
    }
  }
}
