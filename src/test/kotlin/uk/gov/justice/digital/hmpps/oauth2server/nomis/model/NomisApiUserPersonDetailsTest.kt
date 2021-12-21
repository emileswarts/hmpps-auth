package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

internal class NomisApiUserPersonDetailsTest {

  @Test
  fun `toUser verified true`() {
    val account = NomisApiUserPersonDetails(
      username = "username",
      userId = "userId",
      firstName = "firstName",
      surname = "lastName",
      activeCaseLoadId = "activeCaseLoadId",
      email = "email",
      accountStatus = AccountStatus.OPEN
    )
    Assertions.assertThat(account.toUser().verified).isTrue()
  }

  @Test
  fun `toUser verified false`() {
    val account = NomisApiUserPersonDetails(
      username = "username",
      userId = "userId",
      firstName = "firstName",
      surname = "lastName",
      activeCaseLoadId = "activeCaseLoadId",
      email = null,
      accountStatus = AccountStatus.OPEN
    )
    Assertions.assertThat(account.toUser().verified).isFalse()
  }

  @Test
  fun `getAuthorities appends role prefix and includes ROLE_PRISON`() {
    val account = NomisApiUserPersonDetails(
      username = "username",
      userId = "userId",
      firstName = "firstName",
      surname = "lastName",
      activeCaseLoadId = "activeCaseLoadId",
      email = "email",
      accountStatus = AccountStatus.OPEN,
      roles = setOf(SimpleGrantedAuthority("role1"))
    )
    Assertions.assertThat(account.authorities).containsExactly(SimpleGrantedAuthority("ROLE_ROLE1"), SimpleGrantedAuthority("ROLE_PRISON"))
  }
}
