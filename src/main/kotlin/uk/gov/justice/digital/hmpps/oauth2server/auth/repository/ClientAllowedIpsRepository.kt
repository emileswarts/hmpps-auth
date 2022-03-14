package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.ClientAllowedIps

interface ClientAllowedIpsRepository : CrudRepository<ClientAllowedIps, String> {

  fun deleteByBaseClientId(baseClientId: String)
}
