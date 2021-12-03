package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.EmailDomain
import java.util.UUID

interface EmailDomainRepository : CrudRepository<EmailDomain, UUID>
