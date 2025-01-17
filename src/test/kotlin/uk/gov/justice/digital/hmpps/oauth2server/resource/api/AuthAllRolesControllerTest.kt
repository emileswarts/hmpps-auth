package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService
import uk.gov.justice.digital.hmpps.oauth2server.model.AuthUserRole

class AuthAllRolesControllerTest {
  private val authUserRoleService: AuthUserRoleService = mock()
  private val controller = AuthAllRolesController(authUserRoleService)

  @Test
  fun allRoles() {
    val auth1 = Authority("FRED", "FRED")
    val auth2 = Authority("GLOBAL_SEARCH", "Global Search", "Allow user to search globally for a user")
    whenever(authUserRoleService.allRoles).thenReturn(listOf(auth1, auth2))
    val response = controller.allRoles()
    assertThat(response).containsOnly(AuthUserRole(auth1), AuthUserRole(auth2))
  }
}
