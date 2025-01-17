@file:Suppress("DEPRECATION", "ClassName", "SpringMVCViewInspection")

package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.common.util.OAuth2Utils.USER_OAUTH_APPROVAL
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint
import org.springframework.web.bind.support.SessionStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserHelper.Companion.createSampleUser
import uk.gov.justice.digital.hmpps.oauth2server.security.AuthSource
import uk.gov.justice.digital.hmpps.oauth2server.security.UserDetailsImpl
import uk.gov.justice.digital.hmpps.oauth2server.security.UserRetriesService
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import uk.gov.justice.digital.hmpps.oauth2server.service.MfaClientService
import java.util.Optional
import java.util.UUID

internal class UserSelectorAndMfaAuthorizationEndpointTest {
  private val authorizationEndpoint: AuthorizationEndpoint = mock()
  private val userService: UserService = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val userRetriesService: UserRetriesService = mock()
  private val mfaClientService: MfaClientService = mock()
  private val endpoint = UserSelectorAndMfaAuthorizationEndpoint(
    authorizationEndpoint,
    userService,
    userRetriesService,
    telemetryClient,
    mfaClientService
  )
  private val authentication: OAuth2Authentication = mock()
  private val sessionStatus: SessionStatus = mock()
  private val view: View = mock()

  @Nested
  inner class authorize {
    @Test
    fun `test authorize no users set`() {
      val authModelAndView = ModelAndView("view")
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)

      val modelAndView = endpoint.authorize(mutableMapOf(), mapOf(), sessionStatus, authentication)

      assertThat(modelAndView).isSameAs(authModelAndView)
    }

    @Test
    fun `test authorize require mfa`() {
      val authModelAndView = ModelAndView("view", mapOf("requireMfa" to true))
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)

      val modelAndView = endpoint.authorize(mutableMapOf(), mapOf(), sessionStatus, authentication)

      assertThat(modelAndView.viewName).isEqualTo("redirect:/service-mfa-send-challenge")
      assertThat(modelAndView.model).isEmpty()
    }

    @Test
    fun `test authorize require mfa single user selected`() {
      val authModelAndView = ModelAndView(
        "view",
        mutableMapOf(
          "users" to listOf(createSampleUser(username = "user1", source = AuthSource.auth)),
          "requireMfa" to true
        )
      )
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)

      val modelAndView = endpoint.authorize(mutableMapOf(), mapOf(), sessionStatus, authentication)

      assertThat(modelAndView.viewName).isEqualTo("redirect:/service-mfa-send-challenge")
      assertThat(modelAndView.model).containsExactlyInAnyOrderEntriesOf(mapOf(USER_OAUTH_APPROVAL to "auth/user1"))
    }

    @Test
    fun `test authorize multiple users found`() {
      val authModelAndView = ModelAndView(
        "view",
        mutableMapOf(
          "users" to listOf(
            createSampleUser(username = "user1"),
            createSampleUser(username = "user2"),
          )
        )
      )
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)

      val modelAndView = endpoint.authorize(authModelAndView.model, mapOf(), sessionStatus, authentication)

      assertThat(modelAndView.viewName).isEqualTo("userSelector")
      assertThat(modelAndView.model).isEqualTo(authModelAndView.model)
    }

    @Test
    fun `test authorize empty users`() {
      val authModelAndView = ModelAndView("view", mutableMapOf("users" to listOf<String>()))
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)

      val modelAndView =
        endpoint.authorize(mutableMapOf(), mapOf(), sessionStatus, authentication)

      assertThat(modelAndView.viewName).isEqualTo("userSelectorNoRoles")
      assertThat(modelAndView.model).isEmpty()
    }

    @Test
    fun `test authorize exactly one user`() {
      val authModelAndView = ModelAndView(
        "view",
        mutableMapOf("users" to listOf(createSampleUser(username = "user1", source = AuthSource.auth)))
      )
      whenever(authorizationEndpoint.authorize(any(), any(), any(), any())).thenReturn(authModelAndView)
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )

      val model = mutableMapOf<String, Any>()
      val modelAndView = endpoint.authorize(model, mapOf(), sessionStatus, authentication)

      assertThat(modelAndView.view).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(
        mutableMapOf("user_oauth_approval" to "auth/user1"),
        model,
        sessionStatus,
        authentication
      )
    }
  }

  @Nested
  inner class approveOrDeny {
    @Test
    fun `test approveOrDeny no approval specified`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        authentication
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf(), model, sessionStatus, authentication)
    }

    @Test
    fun `test approveOrDeny no authentication specified`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), isNull())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        null
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf(), model, sessionStatus, null)
    }

    @Test
    fun `test approveOrDeny user not found`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      val loginDetails =
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      whenever(authentication.principal).thenReturn(loginDetails)

      val approvalParameters = mutableMapOf("user_oauth_approval" to "none/bloggs")
      val model = mutableMapOf<String, Any>("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(approvalParameters, model, sessionStatus, authentication)

      assertThat(approveView).isSameAs(view)
      verify(userService).getMasterUserPersonDetailsWithEmailCheck("bloggs", AuthSource.none, loginDetails)
      verify(authorizationEndpoint).approveOrDeny(approvalParameters, model, sessionStatus, authentication)
    }

    @Test
    fun `test approveOrDeny user mapped`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      val loginDetails =
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      whenever(authentication.principal).thenReturn(loginDetails)
      val credentials = "some credentials"
      whenever(authentication.credentials).thenReturn(credentials)
      val authorities = setOf(Authority("ROLE_COMMUNITY", "Role Community"))
      val user =
        createSampleUser(
          username = "authuser",
          id = UUID.randomUUID(),
          firstName = "joe",
          lastName = "bloggs",
          authorities = authorities,
          source = AuthSource.auth
        )
      whenever(userService.getMasterUserPersonDetailsWithEmailCheck(anyString(), any(), any())).thenReturn(
        Optional.of(user)
      )

      val approvalParameters = mutableMapOf("user_oauth_approval" to "none/bloggs")
      val model = mutableMapOf<String, Any>("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(approvalParameters, model, sessionStatus, authentication)

      assertThat(approveView).isSameAs(view)
      verify(userService).getMasterUserPersonDetailsWithEmailCheck("bloggs", AuthSource.none, loginDetails)
      verify(authorizationEndpoint).approveOrDeny(
        eq(mutableMapOf("user_oauth_approval" to "true")),
        eq(model),
        eq(sessionStatus),
        check {
          val token = it as UsernamePasswordAuthenticationToken
          assertThat(token.authorities).containsExactlyElementsOf(authorities)
          assertThat(token.credentials).isSameAs(credentials)
          assertThat(token.principal).isEqualTo(
            UserDetailsImpl(
              "authuser",
              "joe bloggs",
              authorities,
              AuthSource.auth.source,
              user.userId,
              "jwtId"
            )
          )
        }
      )
    }

    @Test
    fun `test approveOrDeny create event`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())

      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(authentication.credentials).thenReturn("some credentials")
      val user = createSampleUser(
        username = "authuser",
        id = UUID.randomUUID(),
        firstName = "joe",
        lastName = "bloggs",
        authorities = setOf(Authority("ROLE_COMMUNITY", "Role Community")),
        source = AuthSource.auth
      )
      whenever(userService.getMasterUserPersonDetailsWithEmailCheck(anyString(), any(), any())).thenReturn(
        Optional.of(user)
      )

      endpoint.approveOrDeny(
        mutableMapOf("user_oauth_approval" to "none/bloggs"),
        mutableMapOf("authorizationRequest" to authorizationRequest),
        sessionStatus,
        authentication
      )

      verify(telemetryClient).trackEvent(
        "UserForAccessToken",
        mapOf("loginuserid" to "userid", "loginusername" to "name", "username" to "authuser", "auth_source" to "auth"),
        null
      )
    }

    @Test
    fun `test approveOrDeny record login`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      whenever(authentication.credentials).thenReturn("some credentials")
      val user = createSampleUser(
        username = "authuser",
        id = UUID.randomUUID(),
        firstName = "joe",
        lastName = "bloggs",
        authorities = setOf(Authority("ROLE_COMMUNITY", "Role Community")),
        source = AuthSource.auth
      )
      whenever(userService.getMasterUserPersonDetailsWithEmailCheck(anyString(), any(), any())).thenReturn(
        Optional.of(user)
      )

      endpoint.approveOrDeny(
        mutableMapOf("user_oauth_approval" to "none/bloggs"),
        mutableMapOf("authorizationRequest" to authorizationRequest),
        sessionStatus,
        authentication
      )

      verify(userRetriesService).resetRetriesAndRecordLogin(user)
    }

    @Test
    fun `test client needs mfa but mfa not passed`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), isNull())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      whenever(mfaClientService.clientNeedsMfa(any(), isNull())).thenReturn(true)
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        null
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf(), model, sessionStatus, null)
    }

    @Test
    fun `test client needs mfa`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), isNull())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      whenever(mfaClientService.clientNeedsMfa(any(), isNull())).thenReturn(true)
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        null
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf(), model, sessionStatus, null)
    }

    @Test
    fun `test client needs mfa and mfa passed`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      whenever(mfaClientService.clientNeedsMfa(any(), any())).thenReturn(true)
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        authentication
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf("user_oauth_approval" to "true"), model, sessionStatus, authentication)
    }

    @Test
    fun `test client needs mfa on untrusted network and mfa passed`() {
      whenever(authorizationEndpoint.approveOrDeny(any(), any(), any(), any())).thenReturn(view)
      val authorizationRequest = AuthorizationRequest("bob", setOf())
      whenever(mfaClientService.clientNeedsMfa(any(), any())).thenReturn(true)
      whenever(authentication.principal).thenReturn(
        UserDetailsImpl("user", "name", setOf(), AuthSource.azuread.name, "userid", "jwtId", passedMfa = true)
      )
      val model: MutableMap<String, Any> = mutableMapOf("authorizationRequest" to authorizationRequest)
      val approveView = endpoint.approveOrDeny(
        mutableMapOf(),
        model,
        sessionStatus,
        authentication
      )
      assertThat(approveView).isSameAs(view)
      verify(authorizationEndpoint).approveOrDeny(mapOf("user_oauth_approval" to "true"), model, sessionStatus, authentication)
    }
  }
}
