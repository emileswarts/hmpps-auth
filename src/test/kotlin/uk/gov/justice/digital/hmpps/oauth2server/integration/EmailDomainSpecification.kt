package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentList
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.FindBy

class EmailDomainSpecification : AbstractAuthSpecification() {

  @Page
  internal lateinit var emailDomainsPage: EmailDomainsPage

  @Page
  internal lateinit var addEmailDomainPage: AddEmailDomainPage

  @Page
  internal lateinit var deleteEmailDomainConfirmPage: DeleteEmailDomainConfirmPage

  @Test
  fun `I can access email domains view page`() {
    goTo(loginPage).loginAs("AUTH_DEVELOPER", "password123456")
    goTo(emailDomainsPage).isAt()

    emailDomainsPage.confirmContent()
  }

  @Test
  fun `I can add and remove an email domain`() {
    val domainName = "aaa.com"
    goTo(loginPage).loginAs("AUTH_DEVELOPER", "password123456")
    goTo(emailDomainsPage).isAt()

    assertFalse(emailDomainsPage.domainExists(domainName))

    emailDomainsPage.navigateToAddEmailDomain()
    addEmailDomainPage.confirmContent()
    addEmailDomainPage.addEmailDomain(domainName, "aaa")
    emailDomainsPage.isAt()

    assertTrue(emailDomainsPage.domainExists(domainName))

    emailDomainsPage.navigateToDeleteConfirmPageFor(domainName)
    deleteEmailDomainConfirmPage.isAt()
    deleteEmailDomainConfirmPage.confirmDelete()

    emailDomainsPage.isAt()
    assertFalse(emailDomainsPage.domainExists(domainName))
  }

  @Test
  fun `I am advised of validation errors when adding email domain name`() {
    goTo(loginPage).loginAs("AUTH_DEVELOPER", "password123456")
    goTo(emailDomainsPage).isAt()

    emailDomainsPage.navigateToAddEmailDomain()
    addEmailDomainPage.addEmailDomain("", "")
    addEmailDomainPage.confirmErrorsPresent(
      "email domain name must be supplied",
      "email domain name must be between 6 and 100 characters in length (inclusive)",
    )
  }
}

@PageUrl("/email-domains/{id}")
class DeleteEmailDomainConfirmPage : AuthPage<DeleteEmailDomainConfirmPage> (
  "HMPPS Digital Services - Delete Email Domain",
  "Delete Email Domain"
) {

  @FindBy(id = "submit")
  private lateinit var submit: FluentWebElement

  fun confirmDelete() {
    submit.click()
  }
}

@PageUrl("/email-domains/form")
class AddEmailDomainPage : AuthPage<AddEmailDomainPage>(
  "HMPPS Digital Services - New Email Domain Form",
  "Add new Email Domain"
) {

  @FindBy(id = "name")
  private lateinit var nameInput: FluentWebElement

  @FindBy(id = "description")
  private lateinit var descriptionInput: FluentWebElement

  @FindBy(id = "submit")
  private lateinit var submit: FluentWebElement

  @FindBy(id = "cancel")
  private lateinit var cancel: FluentWebElement

  @FindBy(id = "error-detail")
  private lateinit var errors: FluentWebElement

  fun confirmErrorsPresent(vararg expectedErrors: String) {
    assertTrue(errors.displayed())
    for (error in expectedErrors) {
      assertTrue(errors.html().contains(error))
    }
  }

  fun confirmContent() {
    assertTrue(nameInput.displayed())
    assertTrue(descriptionInput.displayed())
    assertTrue(submit.displayed())
    assertTrue(cancel.displayed())
  }

  fun addEmailDomain(name: String, description: String?) {
    nameInput.fill().withText(name)
    descriptionInput.fill().withText(description)
    submit.click()
  }
}

@PageUrl("/email-domains")
class EmailDomainsPage : AuthPage<EmailDomainsPage>(
  "HMPPS Digital Services - Email Domains",
  "Allowed Email Domain List"
) {

  @FindBy(className = "govuk-table__header")
  private lateinit var tableHeader: FluentWebElement

  @FindBy(css = "input[type='submit']")
  private lateinit var addEmailDomainButton: FluentWebElement

  fun confirmContent(
    expectedTableHeader: String = "Domain",
    rowsMin: Int = 10,
    rowsMax: Int = 120,
  ) {
    assertEquals(expectedTableHeader, tableHeader.element.text)
    assertThat(rows).hasSizeGreaterThanOrEqualTo(rowsMin)
    assertThat(rows).hasSizeLessThanOrEqualTo(rowsMax)
    assertTrue(addEmailDomainButton.displayed())
  }

  fun navigateToDeleteConfirmPageFor(domainName: String) {
    findTableRowContaining(domainName)?.el(By.cssSelector("a[href*='/auth/email-domains/']"))?.click()
  }

  fun domainExists(domainName: String): Boolean {
    return findTableRowContaining(domainName)?.element?.text?.contains(domainName) ?: false
  }

  private fun findTableRowContaining(domainName: String): FluentWebElement? {
    return rows?.firstOrNull { it.element.text.contains(domainName) }
  }

  fun navigateToAddEmailDomain() {
    addEmailDomainButton.click()
  }

  private val rows: FluentList<FluentWebElement>?
    get() = find("table tbody tr")
}
