package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

import uk.gov.justice.digital.hmpps.oauth2server.nomis.model.AccountStatus.OPEN

class NomisUserPersonDetailsHelper {

  companion object {
    fun createSampleNomisUser(
      username: String = "bob",
      userId: String = "1",
      firstName: String = "Bob",
      lastName: String = "Harris",
      activeCaseLoadId: String? = "BXI",
      email: String = "b.h@somewhere.com",
      accountStatus: AccountStatus = OPEN,
      enabled: Boolean = true,
      locked: Boolean = false,
      admin: Boolean = false,
      active: Boolean = true,
      staffStatus: String? = "ACTIVE"
    ): NomisUserPersonDetails = NomisUserPersonDetails(
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
      locked = locked,
      admin = admin,
      active = active,
      staffStatus = staffStatus,
    )
  }
}
