package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.Test

class ClientServiceSpecification : AbstractNomisAuthSpecification() {
  @Page
  private lateinit var clientSummaryPage: ClientSummaryPage

  @Page
  private lateinit var clientMaintenancePage: ClientMaintenancePage

  @Page
  private lateinit var clientMaintenanceAddPage: ClientMaintenanceAddPage

  @Page
  private lateinit var servicesMaintenancePage: ServicesMaintenancePage

  @Page
  private lateinit var servicesMaintenanceAddPage: ServicesMaintenanceAddPage

  @Test
  fun `I can add client service details to an existing client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-add-test-client")
    clientMaintenancePage.isAtPage()
      .serviceChange()

    servicesMaintenanceAddPage.isAtPage()
      .addClientServiceDetails(
        "service-add-test-client",
        "service add test client",
        "test client",
        "ROLE_SOME\nROLE_THING",
        "service-deployment",
        "some@email.com",
        true
      )
      .save()

    clientMaintenancePage.isAtPage()
      .checkServiceDetails()

    // now delete service so test is re-runnable
    goTo("ui/services/service-add-test-client/delete")
    goTo(clientSummaryPage).editClient("service-add-test-client")
    clientMaintenancePage.checkServiceDetails(
      name = "",
      description = "",
      authorisedRoles = "",
      url = "",
      email = "",
      enabled = "No"
    )
  }

  @Test
  fun `I can add client service details to an existing client with rotated credentials`() {
    // create new client
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient(client = "client")
    clientMaintenanceAddPage.isAtPage()
      .edit("clientId", "new-client")
      .selectCheckboxOption("authorization_code")
      .save()

    // duplicate client
    goTo(clientSummaryPage).editClient("new-client")
    clientMaintenancePage.isAtPage()
      .duplicate()

    // remove initial credentials
    goTo("/ui/clients/new-client/delete")

    // check create service link uses the base client id
    goTo(clientSummaryPage).editClient("new-client")
    clientMaintenancePage.isAtPage().serviceChange()
    servicesMaintenanceAddPage.isAtPage().checkCode("new-client")

    // now remove so test is re-runnable
    goTo("/ui/clients/new-client-1/delete")
    clientSummaryPage.isAtPage().checkClientDoesntExist("new-client")
    clientSummaryPage.isAtPage().checkClientDoesntExist("new-client-1")
  }

  @Test
  fun `I can update service details for an existing client`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("service-edit-test-client")
    clientMaintenancePage.isAtPage()
      .checkServiceDetails(
        name = "test service",
        description = "test service for testing",
        authorisedRoles = "FRED_ROLE",
        url = "/auth/account-details?redirect_uri=/",
        email = "",
        enabled = "No"
      )
      .serviceChange()

    servicesMaintenancePage.isAtPage()
      .editEnabled(true)
      .editRoles("ROLE_SOME\nROLE_THING\nROLE_THREE")
      .save()

    clientMaintenancePage.isAtPage()
      .checkServiceDetails(
        name = "test service",
        description = "test service for testing",
        authorisedRoles = "SOME THING THREE",
        url = "/auth/account-details?redirect_uri=/",
        email = "",
        enabled = "Yes"
      )

    // now change detail back so test is re-runnable
    clientMaintenancePage
      .serviceChange()
    servicesMaintenancePage.isAtPage()
      .editEnabled(false)
      .editRoles("ROLE_FRED_ROLE")
      .save()
  }

  @Test
  fun `I can update client service details for an existing client with rotated credentials`() {
    // create new client
    goTo(loginPage).loginAs("ITAG_USER_ADM", "password123456")
    goTo(clientSummaryPage).editClient(client = "client")
    clientMaintenanceAddPage.isAtPage()
      .edit("clientId", "new-client")
      .selectCheckboxOption("authorization_code")
      .save()

    // duplicate client
    goTo(clientSummaryPage).editClient("new-client")
    clientMaintenancePage.isAtPage()
      .duplicate()

    // remove initial credentials
    goTo("/ui/clients/new-client/delete")

    // check create service link uses the base client id
    goTo(clientSummaryPage).editClient("new-client")
    clientMaintenancePage.isAtPage().serviceChange()
    servicesMaintenanceAddPage.isAtPage().checkCode("new-client")

    // now remove so test is re-runnable
    goTo("/ui/clients/new-client-1/delete")
    clientSummaryPage.isAtPage().checkClientDoesntExist("new-client")
    clientSummaryPage.isAtPage().checkClientDoesntExist("new-client-1")
  }

  @Test
  fun `service details are displayed in input field for client with existing service details`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")

    goTo(clientSummaryPage).editClient("manage-user-accounts-ui")
    clientMaintenancePage.isAtPage()
      .checkServiceDetails(
        name = "Manage user accounts",
        description = "",
        authorisedRoles = "AUTH_GROUP_MANAGER CREATE_USER KW_MIGRATION MAINTAIN_ACCESS_ROLES MAINTAIN_ACCESS_ROLES_ADMIN MAINTAIN_OAUTH_USERS",
        url = "http://localhost:3001/",
        email = "",
        enabled = "Yes"
      )
      .serviceChange()

    servicesMaintenancePage.isAtPage()
      .checkDetails(
        code = "manage-user-accounts-ui",
        name = "Manage user accounts",
        description = "",
        authorisedRoles = "ROLE_KW_MIGRATION\nROLE_MAINTAIN_ACCESS_ROLES\nROLE_MAINTAIN_ACCESS_ROLES_ADMIN\nROLE_MAINTAIN_OAUTH_USERS\nROLE_AUTH_GROUP_MANAGER\nROLE_CREATE_USER",
        url = "http://localhost:3001/",
        email = "",
        enabled = "true"
      )
  }
}
