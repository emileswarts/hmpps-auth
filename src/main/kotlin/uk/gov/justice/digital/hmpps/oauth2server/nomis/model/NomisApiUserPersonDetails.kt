package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails

data class NomisApiUserPersonDetails(
  private val username: String,
  override val userId: String,
  override val firstName: String,
  val surname: String,
  val email: String?,
  private val enabled: Boolean = false,
  private val locked: Boolean = false,
  private val roles: Collection<GrantedAuthority?> = emptySet(),
) : UserPersonDetails {

  override fun getUsername(): String = username

  override val name: String
    get() = "$firstName $surname"

  override val isAdmin: Boolean = false

  override val authSource: String
    get() = "nomis"

  override fun toUser(): User =
    User(
      username = username,
      source = AuthSource.nomis,
      email = email,
      verified = true,
      enabled = enabled,
    )

  override fun eraseCredentials() {}

  override fun getAuthorities(): Collection<GrantedAuthority?> = roles.plus(SimpleGrantedAuthority("ROLE_PRISON"))

  override fun getPassword(): String = "password"

  override fun isAccountNonExpired(): Boolean = true

  override fun isAccountNonLocked(): Boolean = !locked

  override fun isCredentialsNonExpired(): Boolean = true

  override fun isEnabled(): Boolean = enabled
}
