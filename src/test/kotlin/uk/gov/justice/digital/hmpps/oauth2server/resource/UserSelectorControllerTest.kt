@file:Suppress("DEPRECATION", "ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class UserSelectorControllerTest {
  @Nested
  inner class selectUser {
    @Test
    fun `selectUser mfa true`() {
      val forward = UserSelectorController().selectUser(true, "delius/bob")
      assertThat(forward.viewName).isEqualTo("redirect:/service-mfa-send-challenge")
      assertThat(forward.model).containsExactlyEntriesOf(mapOf("user_oauth_approval" to "delius/bob"))
    }

    @Test
    fun `selectUser mfa not set`() {
      val forward = UserSelectorController().selectUser(null, "auth/joe")
      assertThat(forward.viewName).isEqualTo("forward:/oauth/authorize")
    }

    @Test
    fun `selectUser mfa false`() {
      val forward = UserSelectorController().selectUser(false, "nomis/harry")
      assertThat(forward.viewName).isEqualTo("forward:/oauth/authorize")
    }
  }
}
