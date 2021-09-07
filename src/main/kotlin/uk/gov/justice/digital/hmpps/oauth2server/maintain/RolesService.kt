package uk.gov.justice.digital.hmpps.oauth2server.maintain

import org.hibernate.Hibernate
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

  @Throws(RoleNotFoundException::class)
  fun getRoleDetail(roleCode: String): Authority {
    val role = roleRepository.findByRoleCode(roleCode) ?: throw RoleNotFoundException("get", roleCode, "notfound")
    Hibernate.initialize(role.adminType)
    return role
  }
  class RoleNotFoundException(val action: String, val role: String, val errorCode: String) :
    Exception("Unable to $action role: $role with reason: $errorCode")
}
