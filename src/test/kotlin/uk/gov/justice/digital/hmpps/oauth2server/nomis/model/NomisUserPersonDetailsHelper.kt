package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.OPEN

class NomisUserPersonDetailsHelper {

  companion object {
    fun createSampleNomisUser(
      profile: String = "TAG_GENERAL",
      staff: Staff = Staff(firstName = "bob", status = "ACTIVE", lastName = "Smith", staffId = 1),
      username: String = "bob",
      accountStatus: String = "OPEN",
      activeCaseLoadId: String? = null
    ): NomisUserPersonDetails {
      val detail = AccountDetail("user", accountStatus, profile, null)
      val personDetails = NomisUserPersonDetails(username = username, staff = staff, accountDetail = detail)
      personDetails.activeCaseLoadId = activeCaseLoadId
      return personDetails
    }

    fun createSampleNomisApiUser(
      username: String = "bob",
      userId: String = "1",
      firstName: String = "Bob",
      lastName: String = "Harris",
      activeCaseLoadId: String = "BXI",
      email: String = "b.h@somewhere.com",
      accountStatus: AccountStatus = OPEN,
      enabled: Boolean = true,
      locked: Boolean = false
    ): NomisApiUserPersonDetails = NomisApiUserPersonDetails(
      username = username,
      userId = userId,
      firstName = firstName,
      surname = lastName,
      activeCaseLoadId = activeCaseLoadId,
      email = email,
      enabled = enabled,
      accountStatus = accountStatus,
      accountNonLocked = true,
      credentialsNonExpired = true,
      locked = locked
    )
  }
}
