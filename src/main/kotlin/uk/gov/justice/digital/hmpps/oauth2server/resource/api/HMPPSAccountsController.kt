package uk.gov.justice.digital.hmpps.oauth2server.resource.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.User
import uk.gov.justice.digital.hmpps.oauth2server.delius.service.DeliusUserService
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.nomis.service.NomisUserApiService
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.HMPPSAccountType.Delius
import uk.gov.justice.digital.hmpps.oauth2server.resource.api.HMPPSAccountType.NOMIS
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService
import java.time.LocalDateTime

@RestController
class HMPPSAccountsController(private val service: HMPPSAccountsService) {
  @GetMapping("/api/accounts/multiple", produces = [APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Search for users with multiple accounts",
    description = "A  list of users that have accounts in more than one HMPPS system"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK"
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorDetail::class)
          )
        ]
      )
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCOUNT_RESEARCH')")
  fun searchForUsersWithMultipleAccounts(
    @Parameter(description = "page number to start searching NOMIS users from", required = true)
    @RequestParam(value = "page", required = false)
    page: Int?,
    @Parameter(description = "page count to retrieve for NOMIS users. Each page is 200 users", required = true)
    @RequestParam(value = "pageCount", required = false)
    pageCount: Int?
  ): List<HMPPSAccounts> =
    service.searchForUsersWithMultipleAccounts(page, pageCount)
}

@Service
class HMPPSAccountsService(
  private val nomisUserApiService: NomisUserApiService,
  private val userService: UserService,
  private val deliusUserService: DeliusUserService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun searchForUsersWithMultipleAccounts(page: Int?, pageCount: Int?): List<HMPPSAccounts> {
    val pageSize = 200
    val pages = nomisUserApiService.findAllActiveUsers(PageRequest.of(0, pageSize)).totalPages
    log.info("Found $pages pages of users")
    fun getAssociatedDeliusAccounts(email: String): List<HMPPSAccount> {
      return deliusUserService.getDeliusUsersByEmail(email).filter { it.isEnabled }.map { deliusAccount ->
        HMPPSAccount(
          accountType = Delius,
          username = deliusAccount.username,
          firstName = deliusAccount.firstName,
          lastName = deliusAccount.surname
        )
      }
    }

    val fromPage = page ?: 0
    val untilPage = minOf(pageCount?.let { fromPage + pageCount } ?: pages, pages)

    return (fromPage until untilPage).flatMap {
      nomisUserApiService.findAllActiveUsers(PageRequest.of(it, pageSize)).also { log.info("Requesting page $it") }
        .map { nomisUser ->
          nomisUser to userService.findUser(nomisUser.username)
        }
        .filter { (_, maybeUser) ->
          maybeUser
            .filter { user -> user.hasLoggedInRecently() }
            .filter { user -> user.email != null }
            .isPresent
        }
        .map { (nomisUser, user) -> (nomisUser to user.orElseThrow()) }
        .map { (nomisUser, user) ->
          HMPPSAccounts(
            accounts = listOf(
              HMPPSAccount(
                accountType = NOMIS,
                username = nomisUser.username,
                firstName = nomisUser.firstName,
                lastName = nomisUser.lastName
              )
            ),
            email = user.email!!
          )
        }
        .map { account ->
          account.copy(accounts = account.accounts + getAssociatedDeliusAccounts(account.email))
        }
        .filter { account -> account.hasMultipleAccounts() }
    }
  }
}

data class HMPPSAccounts(
  val accounts: List<HMPPSAccount>,
  val email: String
)

data class HMPPSAccount(
  val accountType: HMPPSAccountType,
  val username: String,
  val firstName: String,
  val lastName: String
)

enum class HMPPSAccountType {
  Delius,
  NOMIS
}

private fun User.hasLoggedInRecently(): Boolean {
  return lastLoggedIn.isAfter(
    LocalDateTime.now().minusMonths(3)
  )
}

private fun HMPPSAccounts.hasMultipleAccounts(): Boolean {
  return accounts.size > 1
}
