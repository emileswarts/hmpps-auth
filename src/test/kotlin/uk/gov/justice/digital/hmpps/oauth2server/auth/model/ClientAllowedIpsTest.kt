package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ClientAllowedIpsTest {

  @Nested
  inner class AllowedIpsWithNewLines {

    @Test
    fun empty() {
      assertThat(ClientAllowedIps("id").allowedIpsWithNewlines).isEmpty()
    }

    @Test
    fun `replaces space with new line`() {
      assertThat(ClientAllowedIps("id", listOf("127.0.0.1", "127.0.0.3")).allowedIpsWithNewlines).isEqualTo(
        "127.0.0.1\n127.0.0.3"
      )
    }
  }
}
