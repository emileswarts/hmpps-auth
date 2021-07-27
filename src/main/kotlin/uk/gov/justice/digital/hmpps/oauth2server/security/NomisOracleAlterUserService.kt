package uk.gov.justice.digital.hmpps.oauth2server.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import java.sql.SQLException
import javax.sql.DataSource

@Service
@Profile("oracle")
class NomisOracleAlterUserService(
  @Qualifier("dataSource") dataSource: DataSource,
  private val staffUserAccountRepository: StaffUserAccountRepository,
  userRepository: UserRepository,
  verifyEmailService: VerifyEmailService,
) : NomisUserService(staffUserAccountRepository, userRepository, verifyEmailService) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  override fun changePassword(username: String?, password: String?) {
    try {
      staffUserAccountRepository.changePassword(username, password)
    } catch (e: JpaSystemException) {
      if (e.cause?.cause is SQLException) {
        val sqlException = e.cause?.cause as SQLException?
        if (sqlException!!.errorCode == 20087) {
          // password cannot be reused
          log.info("Password cannot be reused exception caught: {}", sqlException.message)
          throw ReusedPasswordException()
        }
        if (sqlException.errorCode == 20001) {
          // password validation failure - should be caught by the front end first
          log.error(
            "Password passed controller validation but failed oracle validation: {}",
            sqlException.message
          )
          throw PasswordValidationFailureException()
        }
      }
      log.error("Found error during changing password", e)
      throw e
    }
  }
}
