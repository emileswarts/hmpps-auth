package uk.gov.justice.digital.hmpps.oauth2server

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID

const val UPDATE_USER_CONTACT = """update user_contact set details=? where user_id = ? and type = ?"""
const val UPDATE_MFA_PREFERENCE = """update users set mfa_preference=? where user_id = ?"""

/**
 * Temporary migration program to copy check-my-diary 2FA details from the CMD database into auth
 */
class MigrateCMD2FA(
  val cmd: JdbcTemplate,
  val auth: JdbcTemplate,
  val insertIntoUsers: SimpleJdbcInsert,
  val insertIntoUserContact: SimpleJdbcInsert,
  val migrate: Boolean = false,
  val users: String = "",
  val pauseMillis: Long = 0L,
) {

  companion object {

    @JvmStatic
    fun main(args: Array<String>) {
      // kc -n check-my-diary-prod port-forward port-forward-pod 5436:5432
      val cmdUrl: String = System.getenv("CMD_URL")
      val cmdUsername: String = System.getenv("CMD_USERNAME")
      val cmdPassword: String = System.getenv("CMD_PASSWORD")

      // kc -n hmpps-auth-preprod port-forward port-forward-pod 5433:5432
      val authUrl: String = System.getenv("AUTH_URL")
      val authUsername: String = System.getenv("AUTH_USERNAME")
      val authPassword: String = System.getenv("AUTH_PASSWORD")

      val migrateEnv: String = System.getenv("MIGRATE")
      val users: String = System.getenv("USERS") // set as 'ALL' for all users, or comma separated
      val pauseMillis: Long = System.getenv("RATE_LIMIT_MILLIS")?.run { this.toLong() } ?: 200L

      val cmd = JdbcTemplate(SingleConnectionDataSource(cmdUrl, cmdUsername, cmdPassword, false))
      val auth = JdbcTemplate(SingleConnectionDataSource(authUrl, authUsername, authPassword, false))
      val insertIntoUsers = SimpleJdbcInsert(auth).withTableName("users")
        .usingColumns(
          "user_id",
          "username",
          "verified",
          "enabled",
          "source",
          "mfa_preference"
        )
      val insertIntoUserContact = SimpleJdbcInsert(auth).withTableName("user_contact")

      try {
        val c =
          MigrateCMD2FA(cmd, auth, insertIntoUsers, insertIntoUserContact, migrateEnv == "true", users, pauseMillis)
        c.initMaps()

        c.forAllCMDRows()
      } finally {
        cmd.dataSource?.connection?.close()
        auth.dataSource?.connection?.close()
        println("Closed connections.")
      }
    }
  }

  val authUsersMap = mutableMapOf<String, User>()
  var authUserContactsMap: Map<UUID, List<Contact>>? = null

  var inUsers = 0
  var inContact = 0

  fun initMaps() {
    auth.query(
      """select 
                    u.user_id as "userId",
                    u.username,
                    COALESCE(u.email, '') as email,
                    u.mfa_preference as "mfaPreference"
                  from users u""",
    ) { r, _ ->
      User(
        userId = UUID.fromString(r.getString("userId")),
        username = r.getString("username"),
        email = r.getString("email"),
        mfaPreference = r.getString("mfaPreference"),
      )
    }
      .forEach { authUsersMap[it.username] = it!! }

    authUserContactsMap = auth.query(
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

    println("populated maps.")
  }

  fun forAllCMDRows() {
    val userList = users
      .split(",")
      .joinToString(",", "(", ")") { "'${it.trim()}'" }
    val whereClause = if (users == "ALL") "" else """ where "QuantumId" in $userList"""
    cmd.query(
      """select 
                 "QuantumId",
                 "EmailAddress",
                 "Sms",
                 "UseEmailAddress",
                 "UseSms",
                 "LastLoginDateTime"
               from "UserAuthentication" $whereClause""",
      if (migrate) ::doMigrate else ::doReport,
    )
    println("inUsers=$inUsers inContact=$inContact")
  }

  fun showPreferences(quantumId: String) {
    cmd.query(
      """select snooze_until, coalesce(email,'') as email, coalesce(sms,'') as sms, comm_pref
          from user_preference where quantum_id = ?""",
      { r, _ ->
        Preferences(
          r.getDate("snooze_until")?.toLocalDate(),
          r.getString("email"),
          r.getString("sms"),
          r.getString("comm_pref")
        )
      },
      quantumId.uppercase()
    ).firstOrNull()?.also {
      if (it.hasData()) {
        println("  user_preference = $it")
      }
    }
  }

  fun doReport(cmdResult: ResultSet) {
    val quantumId = cmdResult.getString("QuantumId")
    val emailAddress = cmdResult.getString("EmailAddress")
    val sms = cmdResult.getString("Sms")
    val useEmailAddress = cmdResult.getBoolean("UseEmailAddress")
    val useSms = cmdResult.getBoolean("UseSms")
    val lastLoginDate = cmdResult.getDate("LastLoginDateTime")?.toLocalDate()

    if (authUsersMap[quantumId.uppercase()] == null) {
      println("missing from users: $quantumId")
    } else {
      val authUser = authUsersMap[quantumId.uppercase()]!!
      inUsers++

      authUserContactsMap!![authUser.userId]?.also { authUserContacts ->
        inContact++

        // println("$quantumId  ${authUser.userId.toString()}")
        // showMainEmail(useEmailAddress, authUser, emailAddress, quantumId, authUserContacts)

        authUserContacts.forEach {
          if (it.type == "SECONDARY_EMAIL" && authUser.mfaPreference == "SECONDARY_EMAIL") {
            if (useEmailAddress && it.verified && emailAddress.uppercase() != it.details.uppercase()) {
              println("$quantumId Secondary Email clash: CMD=$emailAddress, AUTH=${it.details}, mfaPreference=${authUser.mfaPreference}, last CMD login=$lastLoginDate")
              showPreferences(quantumId)
            }
          } else if (it.type == "MOBILE_PHONE" && authUser.mfaPreference == "TEXT") {
            if (useSms && it.verified && sms != it.details.replace("+44", "0")) {
              println("$quantumId SMS clash: CMD=$sms, AUTH=${it.details}, mfaPreference=${authUser.mfaPreference}, last CMD login=$lastLoginDate")
              showPreferences(quantumId)
            }
          }
        }
      }
    }
  }

  fun doMigrate(cmdResult: ResultSet) {
    val quantumId = cmdResult.getString("QuantumId")
    val emailAddress = cmdResult.getString("EmailAddress")
    val sms = cmdResult.getString("Sms")
    val useEmailAddress = cmdResult.getBoolean("UseEmailAddress")
    val useSms = cmdResult.getBoolean("UseSms")
    if (useEmailAddress || useSms) {

      authUsersMap[quantumId.uppercase()]?.also { authUser ->

        // give email precedence when both are true
        val mfaPreference = if (useEmailAddress) "SECONDARY_EMAIL" else "TEXT"
        val userId = authUser.userId
        val authUserContacts = authUserContactsMap!![userId]
        val smsAuth = authUserContacts?.firstOrNull { it.type == "MOBILE_PHONE" }?.run { details }
        val smsExistsInAuth = smsAuth != null
        val emailAuth = authUserContacts?.firstOrNull { it.type == "SECONDARY_EMAIL" }?.run { details }
        val emailExistsInAuth = emailAuth != null

        // Do any inserting first

        if (!emailExistsInAuth && emailAddress != null && emailAddress.isNotBlank()) {
          insertIntoUserContact.execute(
            mapOf(
              "user_id" to userId,
              "type" to "SECONDARY_EMAIL",
              "details" to emailAddress,
              "verified" to useEmailAddress
            )
          )
        }
        if (!smsExistsInAuth && sms != null && sms.isNotBlank()) {
          insertIntoUserContact.execute(
            mapOf(
              "user_id" to userId,
              "type" to "MOBILE_PHONE",
              "details" to sms,
              "verified" to useSms
            )
          )
        }

        // update carefully!

        val emailVerifiedAuth = authUserContacts?.any { it.type == "SECONDARY_EMAIL" && it.verified } ?: false
        val smsVerifiedAuth = authUserContacts?.any { it.type == "MOBILE_PHONE" && it.verified } ?: false

        if (useSms && smsExistsInAuth && !smsVerifiedAuth && sms != smsAuth) {
          auth.update(UPDATE_USER_CONTACT, sms, userId, "MOBILE_PHONE")
        }
        if (useEmailAddress && emailExistsInAuth && !emailVerifiedAuth && emailAddress != emailAuth) {
          auth.update(UPDATE_USER_CONTACT, emailAddress, userId, "SECONDARY_EMAIL")
        }
        if (authUser.mfaPreference != mfaPreference) {
          auth.update(UPDATE_MFA_PREFERENCE, mfaPreference, userId)
        }
        Thread.sleep(pauseMillis)
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

data class Preferences(val snoozeUntil: LocalDate?, val email: String, val sms: String, val commPref: String) {
  fun hasData() = email.isNotEmpty() || sms.isNotEmpty()
}

data class User(
  val userId: UUID,
  val username: String,
  val email: String,
  val mfaPreference: String,
)

data class Contact(val userId: UUID, val type: String, val details: String, val verified: Boolean)
