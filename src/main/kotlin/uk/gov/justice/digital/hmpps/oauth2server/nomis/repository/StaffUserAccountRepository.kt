package uk.gov.justice.digital.hmpps.oauth2server.nomis.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.NomisUserPersonDetails

@Suppress("SqlResolve")
interface StaffUserAccountRepository : CrudRepository<NomisUserPersonDetails, String> {
  @Query(
    value = """select distinct s.*, '' as SPARE4 from STAFF_USER_ACCOUNTS s
    inner join INTERNET_ADDRESSES i on i.owner_id = s.staff_id and owner_class = 'STF'
    where internet_address_class = 'EMAIL' and i.internet_address = ?""",
    nativeQuery = true
  )
  fun findAllNomisUsersByEmailAddress(email: String): List<NomisUserPersonDetails>

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
