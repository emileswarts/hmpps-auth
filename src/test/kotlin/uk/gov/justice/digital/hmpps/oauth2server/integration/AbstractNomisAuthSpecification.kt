package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension

@ExtendWith(NomisExtension::class)
open class AbstractNomisAuthSpecification : AbstractAuthSpecification()
