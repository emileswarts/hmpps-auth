package uk.gov.justice.digital.hmpps.oauth2server.auth.repository

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.lang.NonNull
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import java.util.UUID

interface RoleRepository : CrudRepository<Authority, String>, JpaSpecificationExecutor<Authority> {
  @NonNull
  @Query("Select * from Roles r where r.admin_type LIKE %:adminType%", nativeQuery = true)
  fun findAllByOrderByRoleNameLike(@Param("adminType") adminType: String): List<Authority>
  fun findByRoleCode(roleCode: String?): Authority?

  @Query("select distinct r from User u join u.groups g join g.assignableRoles gar join gar.role r where u.username = ?1 order by r.roleName")
  fun findByGroupAssignableRolesForUsername(username: String?): Set<Authority>

  @Query("select distinct r from User u join u.groups g join g.assignableRoles gar join gar.role r where u.id = ?1 order by r.roleName")
  fun findByGroupAssignableRolesForUserId(userId: UUID?): Set<Authority>
}
