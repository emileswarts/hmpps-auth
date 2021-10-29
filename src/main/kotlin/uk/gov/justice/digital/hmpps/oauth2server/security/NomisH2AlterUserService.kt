package uk.gov.justice.digital.hmpps.oauth2server.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import javax.sql.DataSource

@Service
@Profile("!oracle")
class NomisH2AlterUserService(
  @Qualifier("dataSource") dataSource: DataSource,
  private val passwordEncoder: PasswordEncoder,
  private val staffUserAccountRepository: StaffUserAccountRepository,
  verifyEmailService: VerifyEmailService,
  userRepository: UserRepository,
  nomisUserApiService: NomisUserApiService,
  @Value("\${nomis.enabled:false}") nomisEnabled: Boolean,
) : NomisUserService(staffUserAccountRepository, userRepository, verifyEmailService, nomisUserApiService, nomisEnabled) {

  private val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

  @Transactional
  override fun changePasswordInternal(username: String, password: String) {
    staffUserAccountRepository.changePassword(username, password)

    // also update h2 password table so that we have access to the hash.
    val hashedPassword = passwordEncoder.encode(password)
    jdbcTemplate.update("UPDATE sys.user$ SET spare4 = ? WHERE name = ?", hashedPassword, username)
  }
}
