package uk.gov.justice.digital.hmpps.oauth2server.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.repository.StaffUserAccountRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService
import java.sql.SQLException

@Service
@Profile("oracle")
class NomisOracleAlterUserService(
  private val staffUserAccountRepository: StaffUserAccountRepository,
  userRepository: UserRepository,
  verifyEmailService: VerifyEmailService,
  nomisUserApiService: NomisUserApiService,
  @Value("\${nomis.enabled:false}") private val nomisEnabled: Boolean,
) : NomisUserService(staffUserAccountRepository, userRepository, verifyEmailService, nomisUserApiService, nomisEnabled) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  override fun changePasswordInternal(username: String, password: String) {
    // if nomis api enabled then don't want it changed twice
    if (nomisEnabled) return

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
