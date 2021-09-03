package uk.gov.justice.digital.hmpps.oauth2server.maintain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.RolesFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository

@Service
@Transactional(transactionManager = "authTransactionManager", readOnly = true)
class RolesService(
  private val roleRepository: RoleRepository,
) {

  fun getAllRoles(
    pageable: Pageable
  ): Page<Authority> {

    val rolesFilter = RolesFilter(
      roleCodes = null,
    )

    return roleRepository.findAll(rolesFilter, pageable)
  }
}
