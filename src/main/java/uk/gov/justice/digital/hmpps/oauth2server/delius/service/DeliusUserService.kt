package uk.gov.justice.digital.hmpps.oauth2server.delius.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.oauth2server.config.DeliusRoleMappings
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.DeliusUserPersonDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.UserDetails
import uk.gov.justice.digital.hmpps.oauth2server.delius.model.UserRole
import uk.gov.justice.digital.hmpps.oauth2server.security.DeliusAuthenticationServiceException
import java.util.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
open class DeliusUserService(@Qualifier("deliusApiRestTemplate") private val restTemplate: RestTemplate,
                             @Value("\${delius.enabled:false}") private val deliusEnabled: Boolean,
                             deliusRoleMappings: DeliusRoleMappings) {
  private val mappings: Map<String, List<String>> = deliusRoleMappings.mappings.mapKeys { it.key.toUpperCase().replace('.', '_') }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  open fun getDeliusUserByUsername(username: String): Optional<DeliusUserPersonDetails> {
    if (!deliusEnabled) {
      log.debug("Delius integration disabled, returning empty for {}", username)
      return Optional.empty()
    }

    return try {
      val userDetails = restTemplate.getForObject("/users/{username}/details", UserDetails::class.java, username)
      Optional.ofNullable(userDetails).map { u -> mapUserDetailsToDeliusUser(u, username) }
    } catch (e: HttpClientErrorException) {
      if (e.statusCode == HttpStatus.NOT_FOUND) {
        log.debug("User not found in delius due to {}", e.message)
      } else {
        log.warn("Unable to get delius user details for user {} due to {}", username, e.statusCode, e)
      }
      Optional.empty()
    } catch (e: Exception) {
      log.warn("Unable to retrieve details from Delius for user {} due to {}", username, e)
      when(e) {
        is ResourceAccessException, is HttpServerErrorException -> throw DeliusAuthenticationServiceException()
        else -> Optional.empty()
      }
    }
  }

  open fun authenticateUser(username: String, password: String): Boolean {
    if (!deliusEnabled) {
      log.debug("Delius integration disabled, returning empty for {}", username)
      return false
    }

    return try {
      restTemplate.postForEntity("/authenticate", AuthUser(username, password), String::class.java)
      true
    } catch (e: HttpClientErrorException) {
      if (e.statusCode == HttpStatus.UNAUTHORIZED) {
        log.debug("User not authorised in delius due to {}", e.message)
      } else {
        log.warn("Unable to authenticate user {}", username, e)
      }
      false
    } catch (e: Exception) {
      log.warn("Unable to authenticate user for user {}", username, e)
      false
    }
  }

  private fun mapUserDetailsToDeliusUser(userDetails: UserDetails, username: String): DeliusUserPersonDetails =
      DeliusUserPersonDetails(
          username = username.toUpperCase(),
          userId = userDetails.userId,
          firstName = userDetails.firstName,
          surname = userDetails.surname,
          email = userDetails.email.toLowerCase(),
          enabled = userDetails.enabled,
          roles = mapUserRolesToAuthorities(userDetails.roles))

  private fun mapUserRolesToAuthorities(userRoles: List<UserRole>): Collection<GrantedAuthority> =
      userRoles.mapNotNull { (name) -> mappings[name] }
          .flatMap { r -> r.map(::SimpleGrantedAuthority) }
          .toSet()

  open fun changePassword(username: String, password: String) {
    if (!deliusEnabled) {
      log.debug("Delius integration disabled, returning empty for {}", username)
      return
    }
    restTemplate.postForEntity("/users/{username}/password", AuthPassword(password), Void::class.java, username)
  }

  data class AuthUser(val username: String, val password: String)

  data class AuthPassword(val password: String)
}