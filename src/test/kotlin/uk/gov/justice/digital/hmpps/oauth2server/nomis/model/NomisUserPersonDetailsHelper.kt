package uk.gov.justice.digital.hmpps.oauth2server.nomis.model

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
      email: String = "b.h@somewhere.com",
      enabled: Boolean = true,
    ): NomisApiUserPersonDetails = NomisApiUserPersonDetails(
      username = username,
      userId = userId,
      firstName = firstName,
      surname = lastName,
      email = email,
      enabled = enabled,
    )
  }
}
