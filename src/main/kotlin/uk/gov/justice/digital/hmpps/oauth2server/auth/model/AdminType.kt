package uk.gov.justice.digital.hmpps.oauth2server.auth.model

enum class AdminType(val description: String) {
  DPS_LSA("DPS Local System Administrator"),
  DPS_ADM("DPS Central Administrator"),
  EXT_ADM("External Administrator"),
}
