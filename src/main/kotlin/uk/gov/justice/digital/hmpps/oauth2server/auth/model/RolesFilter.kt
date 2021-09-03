package uk.gov.justice.digital.hmpps.oauth2server.auth.model

import com.google.common.collect.ImmutableList
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class RolesFilter(val roleCodes: List<String>? = null) : Specification<Authority> {

  override fun toPredicate(
    root: Root<Authority>,
    query: CriteriaQuery<*>,
    criteriaBuilder: CriteriaBuilder
  ): Predicate? {
    val andBuilder = ImmutableList.builder<Predicate>()

    if (!roleCodes.isNullOrEmpty()) {
      val rolePredicate =
        root.join<Any, Any>("authorities").get<Any>("roleCode").`in`(roleCodes.map { it.trim().uppercase() })
      andBuilder.add(rolePredicate)
    }

    query.distinct(true)
    return criteriaBuilder.and(*andBuilder.build().toTypedArray())
  }
}
