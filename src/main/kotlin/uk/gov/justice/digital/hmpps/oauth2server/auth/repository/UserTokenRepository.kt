package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken
import java.util.Optional
import java.util.UUID

interface UserTokenRepository : CrudRepository<UserToken, String>, JpaSpecificationExecutor<UserToken> {
  fun findByUserId(userId: UUID?): Optional<UserToken>
}
