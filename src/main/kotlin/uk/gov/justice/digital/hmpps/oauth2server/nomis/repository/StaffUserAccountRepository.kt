package uk.gov.justice.digital.hmpps.oauth2server.nomis.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails

@Suppress("SqlResolve")
interface StaffUserAccountRepository : CrudRepository<NomisUserPersonDetails, String> {
  @Modifying
  @Query(value = "call oms_utils.change_user_password(:username, :password)", nativeQuery = true)
  fun changePassword(username: String?, password: String?)

  @Modifying
  @Query(value = "call oms_utils.unlock_user(:username)", nativeQuery = true)
  fun unlockUser(username: String?)

  @Modifying
  @Query(value = "call oms_utils.lock_user(:username)", nativeQuery = true)
  fun lockUser(username: String?)
}
