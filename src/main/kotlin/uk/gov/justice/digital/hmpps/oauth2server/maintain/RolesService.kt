package uk.gov.justice.digital.hmpps.oauth2server.maintain

import com.microsoft.applicationinsights.TelemetryClient
import org.hibernate.Hibernate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.AdminType
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.RolesFilter
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.CreateRole
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleAdminTypeAmendment
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleDescriptionAmendment
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.RoleNameAmendment

@Service
@Transactional(transactionManager = "authTransactionManager", readOnly = true)
class RolesService(
  private val roleRepository: RoleRepository,
  private val telemetryClient: TelemetryClient,
) {

  @Transactional(transactionManager = "authTransactionManager")
  @Throws(RoleExistsException::class)
  fun createRole(username: String, createRole: CreateRole) {
    val roleCode = createRole.roleCode.trim().uppercase()
    val roleFromDb = roleRepository.findByRoleCode(roleCode)
    roleFromDb?.let { throw RoleExistsException(roleCode, "role code already exists") }

    val roleName = createRole.roleName.trim()
    val roleDescription = createRole.roleDescription?.trim()
    val adminType = createRole.adminType.addDpsAdmTypeIfRequired().toList()

    val role =
      Authority(roleCode = roleCode, roleName = roleName, roleDescription = roleDescription, adminType = adminType)
    roleRepository.save(role)

    // // if roleAdmin is DPS_LSA or DPS_ADM then create role in NOMIS
    // if (adminType.contains(AdminType.DPS_ADM) || adminType.contains(AdminType.DPS_LSA)) {
    //   // createRole in nomis
    // }

    telemetryClient.trackEvent(
      "RoleCreateSuccess",
      mapOf(
        "username" to username,
        "roleCode" to roleCode,
        "roleName" to roleName,
        "roleDescription" to roleDescription,
        "adminType" to adminType.toString()
      ),
      null
    )
  }

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

  @Transactional(transactionManager = "authTransactionManager")
  @Throws(RoleNotFoundException::class)
  fun updateRoleName(username: String, roleCode: String, roleAmendment: RoleNameAmendment) {
    val roleToUpdate = roleRepository.findByRoleCode(roleCode) ?: throw RoleNotFoundException("maintain", roleCode, "notfound")

    roleToUpdate.roleName = roleAmendment.roleName
    roleRepository.save(roleToUpdate)

    telemetryClient.trackEvent(
      "RoleNameUpdateSuccess",
      mapOf("username" to username, "roleCode" to roleCode, "newRoleName" to roleAmendment.roleName),
      null
    )
  }

  @Transactional(transactionManager = "authTransactionManager")
  @Throws(RoleNotFoundException::class)
  fun updateRoleDescription(username: String, roleCode: String, roleAmendment: RoleDescriptionAmendment) {
    val roleToUpdate = roleRepository.findByRoleCode(roleCode) ?: throw RoleNotFoundException("maintain", roleCode, "notfound")

    roleToUpdate.roleDescription = roleAmendment.roleDescription
    roleRepository.save(roleToUpdate)

    telemetryClient.trackEvent(
      "RoleDescriptionUpdateSuccess",
      mapOf("username" to username, "roleCode" to roleCode, "newRoleDescription" to roleAmendment.roleDescription),
      null
    )
  }

  @Transactional(transactionManager = "authTransactionManager")
  @Throws(RoleNotFoundException::class)
  fun updateRoleAdminType(username: String, roleCode: String, roleAmendment: RoleAdminTypeAmendment) {
    val roleToUpdate = roleRepository.findByRoleCode(roleCode) ?: throw RoleNotFoundException("maintain", roleCode, "notfound")
    val immutableAdminTypesInDb = roleToUpdate.adminType.filter { it != AdminType.DPS_LSA }

    roleToUpdate.adminType = (roleAmendment.adminType.addDpsAdmTypeIfRequired() + immutableAdminTypesInDb).toList()
    roleRepository.save(roleToUpdate)

    telemetryClient.trackEvent(
      "RoleAdminTypeUpdateSuccess",
      mapOf("username" to username, "roleCode" to roleCode, "newRoleAdminType" to roleToUpdate.adminType.toString()),
      null
    )
  }

  private fun Set<AdminType>.addDpsAdmTypeIfRequired() = (if (AdminType.DPS_LSA in this) (this + AdminType.DPS_ADM) else this)

  class RoleNotFoundException(val action: String, val role: String, val errorCode: String) :
    Exception("Unable to $action role: $role with reason: $errorCode")

  class RoleExistsException(val role: String, val errorCode: String) :
    Exception("Unable to create role: $role with reason: $errorCode")
}
