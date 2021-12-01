package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.oauth2server.resource.DeliusExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.NomisExtension

@ExtendWith(DeliusExtension::class, NomisExtension::class)
open class AbstractNomisAndDeliusAuthSpecification : AbstractAuthSpecification()
