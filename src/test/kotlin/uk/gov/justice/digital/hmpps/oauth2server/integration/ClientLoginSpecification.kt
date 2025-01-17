@file:Suppress("DEPRECATION", "ClassName")

package uk.gov.justice.digital.hmpps.oauth2server.integration

import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.nimbusds.jwt.JWTParser
import net.minidev.json.JSONArray
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.support.FindBy
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import uk.gov.justice.digital.hmpps.oauth2server.resource.AzureOIDCExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.RemoteClientExtension
import uk.gov.justice.digital.hmpps.oauth2server.resource.RemoteClientMockServer.Companion.clientBaseUrl
import uk.gov.justice.digital.hmpps.oauth2server.resource.TokenVerificationExtension.Companion.tokenVerificationApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Verify clients can login, be redirected back to their system and then logout again.
 * The token-verification spring profile needs to be enabled (as well as the dev profile) for these tests.  This is
 * done automatically in circle configuration for automated builds, but needs enabling when running these tests.  By
 * default the dev profile doesn't have it enabled so that other clients can use this project without issues.
 */
@ExtendWith(RemoteClientExtension::class)
class ClientLoginSpecification : AbstractNomisAndDeliusAuthSpecification() {

  @Page
  private lateinit var clientSummaryPage: ClientSummaryPage

  @Page
  private lateinit var clientMaintenancePage: ClientMaintenancePage

  @Page
  private lateinit var selectUserPage: SelectUserPage

  @Page
  private lateinit var selectUserNoRolesPage: SelectUserNoRolesPage

  @Page
  private lateinit var homePage: HomePage

  @Test
  fun `I can sign in from another client`() {
    clientSignIn("ITAG_USER", "password")
      .jsonPath(".user_name").isEqualTo("ITAG_USER")
      .jsonPath(".user_id").isEqualTo("1")
      .jsonPath(".sub").isEqualTo("ITAG_USER")
      .jsonPath(".auth_source").isEqualTo("nomis")
      .jsonPath(".name").doesNotExist()
  }

  @Test
  fun `I can sign in from another client - check access token`() {
    clientSignIn("ITAG_USER", "password")
      .jsonPath(".access_token").value<JSONArray> {
        val claims = JWTParser.parse(it[0].toString()).jwtClaimsSet
        assertThat(claims.getClaim("user_name")).isEqualTo("ITAG_USER")
        assertThat(claims.getClaim("name")).isNull()
        assertThat(claims.getClaim("user_id")).isEqualTo("1")
        assertThat(claims.getClaim("sub")).isEqualTo("ITAG_USER")
        assertThat(claims.getClaim("auth_source")).isEqualTo("nomis")
      }
  }

  @Test
  fun `I can sign in from a client with jwt fields name configured`() {
    clientSignIn("ITAG_USER", "password", "omicuser")
      .jsonPath(".user_name").doesNotExist()
      .jsonPath(".name").isEqualTo("Itag User")
      .jsonPath(".user_id").isEqualTo("1")
      .jsonPath(".sub").isEqualTo("ITAG_USER")
      .jsonPath(".auth_source").isEqualTo("nomis")
  }

  @Test
  fun `I can sign in from a client with jwt fields name configured - check access token`() {
    clientSignIn("ITAG_USER", "password", "omicuser")
      .jsonPath(".access_token").value<JSONArray> {
        val claims = JWTParser.parse(it[0].toString()).jwtClaimsSet
        // note that user_name still exists even though as comes from DefaultUserAuthenticationConverter instead
        assertThat(claims.getClaim("user_name")).isEqualTo("ITAG_USER")
        assertThat(claims.getClaim("name")).isEqualTo("Itag User")
        assertThat(claims.getClaim("user_id")).isEqualTo("1")
        assertThat(claims.getClaim("user_uuid")).isEqualTo("a04c70ee-51c9-4852-8d0d-130da5c85c42")
        assertThat(claims.getClaim("sub")).isEqualTo("ITAG_USER")
        assertThat(claims.getClaim("auth_source")).isEqualTo("nomis")
      }
  }

  @Test
  fun `I can sign in from another client and send token to verification service (requires token-verification spring profile)`() {
    val jwt = goTo(loginPage).loginAs("ITAG_USER", "password").parseJwt()

    clientAccess()
      .jsonPath(".sub").isEqualTo("ITAG_USER")
      .jsonPath(".access_token").value<JSONArray> {
        tokenVerificationApi.verify(
          postRequestedFor(urlPathEqualTo("/token"))
            .withQueryParam("authJwtId", equalTo(jwt.jwtid))
            .withRequestBody(equalTo(it[0].toString()))
        )
      }
  }

  @Test
  fun `I can sign in from another client as delius only user`() {
    clientSignIn("DELIUS_USER", "password")
      .jsonPath(".user_name").isEqualTo("DELIUS_USER")
      .jsonPath(".user_id").isEqualTo("2500077027")
      .jsonPath(".sub").isEqualTo("DELIUS_USER")
      .jsonPath(".auth_source").isEqualTo("delius")
  }

  @Test
  fun `I can sign in from another client as auth only user`() {
    clientSignIn("AUTH_USER")
      .jsonPath(".user_name").isEqualTo("AUTH_USER")
      .jsonPath(".user_id").isEqualTo("608955ae-52ed-44cc-884c-011597a77949")
      .jsonPath(".sub").isEqualTo("AUTH_USER")
      .jsonPath(".auth_source").isEqualTo("auth")
  }

  @Test
  fun `Sign in from another client adds redirect url to login page`() {
    clientAccess {
      assertThat(loginPage.isAtPage().url()).isEqualTo("sign-in?redirect_uri=$clientBaseUrl")
      loginPage.submitLogin(username = "AUTH_USER")
    }
      .jsonPath(".user_name").isEqualTo("AUTH_USER")
  }

  @Test
  fun `Sign in updates last accessed`() {
    val now = LocalDateTime.now()
    clientSignIn("AUTH_USER")
      .jsonPath(".user_name").isEqualTo("AUTH_USER")

    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("elite2apiclient")
    with(clientMaintenancePage) {
      isAtPage()
      val dateTime = LocalDateTime.parse(
        el("#elite2apiclient-last-accessed").text(),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
      )
      assertThat(dateTime).isAfterOrEqualTo(now.truncatedTo(ChronoUnit.MINUTES))
    }
  }

  @Test
  fun `I can sign in from another client as azure ad user with a nomis account`() {
    // The email is mapped to RO_USER in find_users_ro_demo.json
    azureClientSignIn("ro_user@some.justice.gov.uk")
      .jsonPath(".user_name").isEqualTo("RO_DEMO")
      .jsonPath(".user_id").isEqualTo("10")
      .jsonPath(".sub").isEqualTo("RO_DEMO")
      .jsonPath(".auth_source").isEqualTo("nomis")
      .jsonPath(".access_token").value<JSONArray> {
        val claims = JWTParser.parse(it[0].toString()).jwtClaimsSet
        assertThat(claims.getClaim("user_name")).isEqualTo("RO_DEMO")
        assertThat(claims.getClaim("authorities") as List<*>)
          .containsExactly("ROLE_PRISON", "ROLE_LICENCE_RO")
      }
  }

  @Test
  fun `I can sign in from another client as azure ad user with a nomis account and verified auth email`() {
    // The email is mapped to ITAG_USER in the nomis database
    azureClientSignIn("itag_user@digital.justice.gov.uk")
      .jsonPath(".user_name").isEqualTo("ITAG_USER")
      .jsonPath(".user_id").isEqualTo("1")
      .jsonPath(".sub").isEqualTo("ITAG_USER")
      .jsonPath(".auth_source").isEqualTo("nomis")
  }

  @Test
  fun `I can sign in from another client as azure ad user with an auth account`() {
    // The email is mapped to AUTH_GROUP_MANAGER in the nomis database
    azureClientSignIn("auth_group_manager@digital.justice.gov.uk")
      .jsonPath(".user_name").isEqualTo("AUTH_GROUP_MANAGER")
      .jsonPath(".user_id").isEqualTo("1f650f15-0993-4db7-9a32-5b930ff86035")
      .jsonPath(".sub").isEqualTo("AUTH_GROUP_MANAGER")
      .jsonPath(".auth_source").isEqualTo("auth")
      .jsonPath(".access_token").value<JSONArray> {
        val claims = JWTParser.parse(it[0].toString()).jwtClaimsSet
        assertThat(claims.getClaim("user_name")).isEqualTo("AUTH_GROUP_MANAGER")
        assertThat(claims.getClaim("authorities") as List<*>)
          .containsExactly("ROLE_AUTH_GROUP_MANAGER")
      }
  }

  @Test
  fun `Sign in as azure ad user with a disabled auth account takes user back to select service page`() {
    // The email is mapped to AUTH_GROUP_MANAGER in the nomis database
    azureClientSignInNoPrivilegesContinue("auth_disabled@digital.justice.gov.uk")
    homePage.isAtPage()
  }

  @Test
  fun `Sign in as azure ad user with multiple accounts`() {
    azureClientSignInChooseUser("Auth_Test@digital.Justice.gov.UK", "AUTH_CHANGE_TEST")
      .jsonPath(".user_name").isEqualTo("AUTH_CHANGE_TEST")
      .jsonPath(".user_id").isEqualTo("2e285ccd-dcfd-4497-9e22-d6e8e10a2b3f")
      .jsonPath(".sub").isEqualTo("AUTH_CHANGE_TEST")
      .jsonPath(".auth_source").isEqualTo("auth")
      .jsonPath(".access_token").value<JSONArray> {
        val claims = JWTParser.parse(it[0].toString()).jwtClaimsSet
        assertThat(claims.getClaim("user_name")).isEqualTo("AUTH_CHANGE_TEST")
      }
  }

  @Test
  fun `Sign in as a user with no privileges in service gives warning`() {
    startClientAccess("manage-user-accounts-ui")
    loginPage.isAtPage().submitLogin("AUTH_USER", "password123456")
    selectUserNoRolesPage.isAtPage()
      .checkHeadingText("You do not have permission to view Manage user accounts")
      .proceed()
    homePage.isAtPage()
  }

  @Test
  fun `I can redeem the access token for a refresh token`() {
    clientSignIn("AUTH_USER")
      .jsonPath(".refresh_token").value<JSONArray> {
        getRefreshToken(it[0].toString())
          .jsonPath(".user_name").isEqualTo("AUTH_USER")
          .jsonPath(".user_id").isEqualTo("608955ae-52ed-44cc-884c-011597a77949")
          .jsonPath(".sub").isEqualTo("AUTH_USER")
          .jsonPath(".auth_source").isEqualTo("auth")
      }
  }

  @Test
  fun `I can redeem the refresh token for an access token and send token to verification service (requires token-verification spring profile)`() {
    goTo(loginPage).loginAs("AUTH_USER")

    clientAccess()
      .jsonPath(".['refresh_token','access_token']").value<JSONArray> {
        val accessJwtId = JWTParser.parse((it[0] as Map<*, *>)["access_token"].toString()).jwtClaimsSet.jwtid
        val accessJwtIdWithSpaces = accessJwtId.replace("+", " ")

        getRefreshToken((it[0] as Map<*, *>)["refresh_token"].toString())
          .jsonPath(".sub").isEqualTo("AUTH_USER")
          .jsonPath(".access_token").value<JSONArray> { accessToken ->
            tokenVerificationApi.verify(
              postRequestedFor(urlPathEqualTo("/token/refresh"))
                .withQueryParam("accessJwtId", equalTo(accessJwtIdWithSpaces))
                .withRequestBody(equalTo(accessToken[0].toString()))
            )
          }
      }
  }

  @Test
  fun `I can logout as a client from another system`() {
    clientSignIn("AUTH_USER")
    goTo("/sign-out?redirect_uri=$clientBaseUrl&client_id=elite2apiclient")
    assertThat(driver.currentUrl).isEqualTo(clientBaseUrl)

    // check that they are now logged out
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")
    loginPage.isAt()
  }

  @Test
  fun `I can logout as a client from another system using the old logout URL`() {
    clientSignIn("AUTH_USER")
    goTo("/logout?redirect_uri=$clientBaseUrl&client_id=elite2apiclient")
    assertThat(driver.currentUrl).isEqualTo(clientBaseUrl)

    // check that they are now logged out
    val state = RandomStringUtils.random(6, true, true)
    goTo("/oauth/authorize?client_id=elite2apiclient&redirect_uri=$clientBaseUrl&response_type=code&state=$state")
    loginPage.isAt()
  }

  @Test
  fun `I can logout as a client from another system and send token to verification service (requires token-verification spring profile)`() {
    val authJwtId = goTo(loginPage).loginAs("AUTH_USER").parseJwt().jwtid

    goTo("/sign-out?redirect_uri=$clientBaseUrl&client_id=elite2apiclient")

    tokenVerificationApi.verify(
      deleteRequestedFor(urlPathEqualTo("/token"))
        .withQueryParam("authJwtId", equalTo(authJwtId))
    )
  }

  private fun azureClientSignIn(email: String): BodyContentSpec {
    AzureOIDCExtension.azureOIDC.stubToken(email)
    return clientAccess("azure-login-client") { loginPage.clickAzureOIDCLink() }
  }

  private fun azureClientSignInChooseUser(email: String, username: String): BodyContentSpec {
    AzureOIDCExtension.azureOIDC.stubToken(email)
    return clientAccess(
      "azure-login-client"
    ) {
      loginPage.clickAzureOIDCLink()
      selectUserPage.isAtPage().selectUser("auth", username)
    }
  }

  private fun azureClientSignInNoPrivilegesContinue(email: String) {
    AzureOIDCExtension.azureOIDC.stubToken(email)
    startClientAccess("azure-login-client")
    loginPage.clickAzureOIDCLink()
    selectUserNoRolesPage.isAtPage().proceed()
  }
}

@PageUrl("/oauth/authorize")
class SelectUserPage : AuthPage<SelectUserPage>("HMPPS Digital Services - Select user", "Select user") {
  @FindBy(css = "input[type='submit']")
  private lateinit var selectButton: FluentWebElement

  fun selectUser(authSource: String, username: String) {
    el("input[value='$authSource/$username']").click()
    selectButton.submit()
  }
}

@PageUrl("/oauth/authorize")
class SelectUserNoRolesPage : AuthPage<SelectUserNoRolesPage>(
  "HMPPS Digital Services - Missing permissions",
  "You do not have permission to view",
  headingStartsWith = true,
) {
  @FindBy(css = "a[data-qa='select']")
  private lateinit var continueButton: FluentWebElement

  fun proceed() {
    continueButton.click()
  }
}
