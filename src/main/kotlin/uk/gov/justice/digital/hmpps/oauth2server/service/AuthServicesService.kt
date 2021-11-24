package uk.gov.justice.digital.hmpps.oauth2server.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.GrantedAuthority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.OauthServiceRepository
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

@org.springframework.stereotype.Service
class AuthServicesService(private val oauthServiceRepository: OauthServiceRepository) {
  fun list(): List<Service> = oauthServiceRepository.findAllByOrderByName()

  fun listEnabled(): List<Service> = oauthServiceRepository.findAllByEnabledTrueOrderByName()

  fun listEnabled(authorities: Collection<GrantedAuthority>): List<Service> =
    listEnabled().filter { s ->
      s.roles.isEmpty() || authorities.any { a -> s.roles.contains(a.authority) }
    }

  fun getService(code: String): Service = oauthServiceRepository.findById(code)
    .orElseThrow { EntityNotFoundException("Entity $code not found") }

  fun loadServiceDetails(baseClientId: String): ServiceSummary? {
    val service = oauthServiceRepository.findByIdOrNull(baseClientId)
    val roles = service?.roles?.map { it.substringAfter("ROLE_") }?.sorted()?.joinToString("\n") ?: ""
    return ServiceSummary(
      code = service?.code ?: "",
      name = service?.name ?: "",
      description = service?.description ?: "",
      authorisedRoles = roles,
      url = service?.url ?: "",
      enabled = service?.enabled,
      email = service?.email ?: "",
    )
  }

  fun updateService(service: Service) {
    oauthServiceRepository.save(service)
  }

  fun addService(service: Service) {
    oauthServiceRepository.findById(service.code)
      .ifPresent { throw EntityExistsException("Entity ${service.code} already exists") }
    oauthServiceRepository.save(service)
  }

  fun removeService(code: String) {
    oauthServiceRepository.deleteById(code)
  }

  fun findService(code: String): Service? = oauthServiceRepository.findByIdOrNull(code)
}

data class ServiceSummary(
  val code: String?,
  val name: String?,
  val description: String?,
  var authorisedRoles: String?,
  val url: String?,
  val enabled: Boolean?,
  val email: String?,
)
