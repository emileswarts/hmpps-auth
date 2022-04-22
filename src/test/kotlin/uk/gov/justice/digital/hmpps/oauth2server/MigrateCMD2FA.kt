package uk.gov.justice.digital.hmpps.oauth2server

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.sql.ResultSet
import java.util.UUID

/**
 * Temporary migration program to copy check-my-diary 2FA details from the CMD database into auth
 */
class MigrateCMD2FA {

  companion object {

    // kc -n check-my-diary-prod port-forward port-forward-pod 5436:5432
    val cmdUrl: String = System.getenv("CMD_URL")
    val cmdUsername: String = System.getenv("CMD_USERNAME")
    val cmdPassword: String = System.getenv("CMD_PASSWORD")

    // kc -n hmpps-auth-preprod port-forward port-forward-pod 5433:5432
    val authUrl: String = System.getenv("AUTH_URL")
    val authUsername: String = System.getenv("AUTH_USERNAME")
    val authPassword: String = System.getenv("AUTH_PASSWORD")

    val cmd = JdbcTemplate(SingleConnectionDataSource(cmdUrl, cmdUsername, cmdPassword, false))
    val auth = JdbcTemplate(SingleConnectionDataSource(authUrl, authUsername, authPassword, false))
    val authUsersMap = mutableMapOf<String, User>()

    var inUsers = 0
    var inContact = 0

    @JvmStatic
    fun main(args: Array<String>) {
      try {
        doMigrate()
      } finally {
        cmd.dataSource?.connection?.close()
        auth.dataSource?.connection?.close()
        print("Closed connections. inUsers=$inUsers inContact=$inContact")
      }
    }

    private fun doMigrate() {

      auth.query(
        """select 
                u.user_id as "userId",
                u.username,
                COALESCE(u.email, '') as email,
                u.verified,
                u.mfa_preference as "mfaPreference"
              from users u""",
      ) { r, _ ->
        User(
          userId = UUID.fromString(r.getString("userId")),
          username = r.getString("username"),
          email = r.getString("email"),
          verified = r.getBoolean("verified"),
          mfaPreference = r.getString("mfaPreference"),
        )
      }
        .forEach { authUsersMap[it.username] = it!! }

      val authUserContactsMap = auth.query(
        """select user_id as "userId", type, details, verified from user_contact""",
      ) { r, _ ->
        Contact(
          UUID.fromString(r.getString("userId")),
          r.getString("type"),
          r.getString("details"),
          r.getBoolean("verified")
        )
      }
        .groupBy { it.userId }

      cmd.query(
        """select 
            "QuantumId",
            "EmailAddress",
            "Sms",
            "UseEmailAddress",
            "UseSms",
            "LastLoginDateTime"
          from "UserAuthentication" """,

      ) { cmdResult: ResultSet ->
        val quantumId = cmdResult.getString("QuantumId")
        val emailAddress = cmdResult.getString("EmailAddress")
        val sms = cmdResult.getString("Sms")
        val useEmailAddress = cmdResult.getBoolean("UseEmailAddress")
        val useSms = cmdResult.getBoolean("UseSms")
        val lastLoginDate = cmdResult.getDate("LastLoginDateTime")?.toLocalDate()

        authUsersMap[quantumId.uppercase()]?.also { authUser ->
          inUsers++

          // println(quantumId)

          authUserContactsMap[authUser.userId]?.also { authUserContacts ->
            inContact++

            // println("$quantumId  ${authUser.userId.toString()}")
            // showMainEmail(useEmailAddress, authUser, emailAddress, quantumId, authUserContacts)

            authUserContacts.forEach {
              if (it.type == "SECONDARY_EMAIL" && authUser.mfaPreference == "SECONDARY_EMAIL") {
                if (useEmailAddress && it.verified && emailAddress.uppercase() != it.details.uppercase()) {
                  println("$quantumId Secondary Email clash: CMD=$emailAddress, AUTH=${it.details} verified=${it.verified}, mfaPreference=${authUser.mfaPreference}, last CMD login=$lastLoginDate")
                }
              } else if (it.type == "MOBILE_PHONE" && authUser.mfaPreference == "TEXT") {
                if (useSms && it.verified && sms != it.details.replace("+44", "0")) {
                  println("$quantumId SMS clash: CMD=$sms, AUTH=${it.details}, verified=${it.verified}, mfaPreference=${authUser.mfaPreference}, last CMD login=$lastLoginDate")
                }
              }
            }
          }
        }
      }
    }

    private fun showMainEmail(
      useEmailAddress: Boolean,
      authUser: User,
      emailAddress: String,
      quantumId: String,
      authUserContacts: List<Contact>
    ) {
      if (useEmailAddress && authUser.mfaPreference == "EMAIL" && emailAddress.uppercase() != authUser.email.uppercase()) {
        print("$quantumId Main Email difference: CMD=$emailAddress, AUTH=${authUser.email}, mfaPreference=${authUser.mfaPreference}")
        val secondaryEmail = authUserContacts.filter { it.type == "SECONDARY_EMAIL" }
        if (secondaryEmail.isNotEmpty()) {
          val first = secondaryEmail.first()
          println("  SECONDARY_EMAIL exists=${first.details} verified=${first.verified} match=${first.details.uppercase() == emailAddress.uppercase()}")
        } else {
          println()
        }
      }
    }
  }
}

data class User(
  val userId: UUID,
  val username: String,
  val email: String,
  val verified: Boolean,
  val mfaPreference: String,
)

data class Contact(val userId: UUID, val type: String, val details: String, val verified: Boolean)
