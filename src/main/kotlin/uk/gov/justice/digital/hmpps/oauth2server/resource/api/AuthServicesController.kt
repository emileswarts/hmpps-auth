package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Service
import uk.gov.justice.digital.hmpps.oauth2server.service.AuthServicesService

@RestController
@Tag(name = "/api/services", description = "Auth Services Controller")
class AuthServicesController(private val authServicesService: AuthServicesService) {
  @GetMapping("/api/services")
  @Operation(summary = "Get all enabled services.")
  fun services(): List<AuthService> = authServicesService.listEnabled().map { AuthService(it) }

  @GetMapping("/api/services/me")
  @Operation(summary = "Get my services.")
  fun myServices(
    @Schema(accessMode = READ_ONLY)
    authentication: Authentication
  ): List<AuthService> =
    authServicesService.listEnabled(authentication.authorities).map { AuthService(it) }

  @GetMapping("/api/services/{serviceCode}")
  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_OAUTH_USERS', 'ROLE_AUTH_GROUP_MANAGER')")
  @Operation(summary = "Get service details by service code.")
  fun serviceByCode(@PathVariable serviceCode: String): AuthService {
    val service = authServicesService.findService(serviceCode)
    service?.let {
      return AuthService(it)
    } ?: throw ServiceNotFoundException("Service with code $serviceCode does not exist")
  }
}

@Schema(description = "Digital Services")
data class AuthService(
  @Schema(required = true, example = "prison-staff-hub")
  val code: String,

  @Schema(required = true, example = "Digital Prison Services")
  val name: String,

  @Schema(
    required = false,
    description = "Description of service, often blank",
    example = "View and Manage Offenders in Prison (Old name was NEW NOMIS)"
  )
  val description: String?,

  @Schema(
    required = false,
    description = "Contact information, can be blank",
    example = "feedback@digital.justice.gov.uk"
  )
  val contact: String?,

  @Schema(
    required = false,
    description = "URL of service",
    example = "https://digital-dev.prison.service.justice.gov.uk"
  )
  val url: String?,
) {
  constructor(s: Service) : this(s.code, s.name, s.description, s.email, s.url)
}

class ServiceNotFoundException(val error: String) : Exception(error)
