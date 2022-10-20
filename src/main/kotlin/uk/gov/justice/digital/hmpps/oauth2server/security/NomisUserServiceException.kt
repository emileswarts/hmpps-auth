package uk.gov.justice.digital.hmpps.oauth2server.security

import org.springframework.security.authentication.InternalAuthenticationServiceException

class NomisUserServiceException(username: String) : InternalAuthenticationServiceException(
  "Unable to retrieve information for $username from NOMIS.  We are unable to connect to NOMIS or there is an issue with $username in NOMIS"
)
class NomisUnreachableServiceException(username: String) : InternalAuthenticationServiceException(
  "NOMIS Down - Unable to retrieve information for $username from NOMIS.  We are unable to connect to NOMIS or there is an issue with $username in NOMIS"
)
