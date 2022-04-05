package uk.gov.justice.digital.hmpps.oauth2server.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentList
import org.fluentlenium.core.domain.FluentWebElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.FindBy

class EmailDomainSpecification : AbstractAuthSpecification() {

  @Page
  internal lateinit var emailDomainsPage: EmailDomainsPage

  @Test
  fun `I can access email domains view page`() {
    goTo(loginPage).loginAs("AUTH_ADM", "password123456")
    goTo(emailDomainsPage).isAt()

    emailDomainsPage.confirmContent()
  }
}

@PageUrl("/email-domains")
class EmailDomainsPage : AuthPage<EmailDomainsPage>(
  "HMPPS Digital Services - Email Domains",
  "Email Domain List - view only"
) {

  @FindBy(className = "govuk-table__header")
  private lateinit var tableHeader: FluentWebElement

  fun confirmContent(
    expectedTableHeader: String = "Domain",
    rowsMin: Int = 10,
    rowsMax: Int = 120,
  ) {
    assertEquals(expectedTableHeader, tableHeader.element.text)
    assertThat(rows).hasSizeGreaterThanOrEqualTo(rowsMin)
    assertThat(rows).hasSizeLessThanOrEqualTo(rowsMax)
  }

  private val rows: FluentList<FluentWebElement>?
    get() = find("table tbody tr")
}
