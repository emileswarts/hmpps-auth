package uk.gov.justice.digital.hmpps.oauth2server

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import java.sql.ResultSet
import java.util.UUID

class MigrateCMD2FATest {
  private val cmd: JdbcTemplate = mock()
  private val auth: JdbcTemplate = mock()
  private val insertIntoUsers: SimpleJdbcInsert = mock()
  private val insertIntoUserContact: SimpleJdbcInsert = mock()
  private val script = MigrateCMD2FA(cmd, auth, insertIntoUsers, insertIntoUserContact)

  val UUID1 = UUID.fromString("3b9969ca-c57a-11ec-9d64-0242ac120002")
  val UUID2 = UUID.fromString("9cb54de0-cad0-11ec-9d64-0242ac120002")
  val UUID3 = UUID.fromString("9cb5504c-cad0-11ec-9d64-0242ac120002")

  @Test
  fun `Do Nothing if no 2FA in CMD`() {
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", false, false)
    )

    verify(auth, never()).update(any<String>(), any<String>(), any<UUID>())

    verifyNoInteractions(auth)
    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Do not update contact if auth verified`() {
    script.authUsersMap.putAll(
      mapOf(
        "AAA11A" to User(UUID1, "AAA11A", "main1", "EMAIL"),
        "AAA11B" to User(UUID2, "AAA11B", "main2", "SECONDARY_EMAIL"),
        "AAA11C" to User(UUID3, "AAA11A", "main3", "TEXT")
      )
    )
    script.authUserContactsMap = mapOf(
      UUID1 to listOf(
        Contact(UUID1, "MOBILE_PHONE", "1111", true),
        Contact(UUID1, "SECONDARY_EMAIL", "email1", true),
      ),
      UUID2 to listOf(
        Contact(UUID2, "MOBILE_PHONE", "2222", true),
        Contact(UUID2, "SECONDARY_EMAIL", "email2", true),
      ),
      UUID3 to listOf(
        Contact(UUID3, "MOBILE_PHONE", "3333", true),
        Contact(UUID3, "SECONDARY_EMAIL", "email3", true),
      ),
    )

    script.doMigrate(
      withCMDResultSet("aaa11a", "sms-email", "1234567", true, true)
    )
    script.doMigrate(
      withCMDResultSet("aaa11b", "sms-email", "1234567", true, true)
    )
    script.doMigrate(
      withCMDResultSet("aaa11c", "sms-email", "1234567", true, true)
    )

    verify(auth).update("update users set mfa_preference=? where user_id = ?", "SECONDARY_EMAIL", UUID1)
    // pref of 2nd is already SECONDARY_EMAIL
    verify(auth).update("update users set mfa_preference=? where user_id = ?", "SECONDARY_EMAIL", UUID3)
    verifyNoMoreInteractions(auth)
    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Do not update contact if email or mob is the same`() {
    script.authUsersMap.putAll(
      mapOf(
        "AAA11A" to User(UUID1, "AAA11A", "main1", "EMAIL"),
        "AAA11B" to User(UUID2, "AAA11B", "main2", "SECONDARY_EMAIL"),
        "AAA11C" to User(UUID3, "AAA11A", "main3", "TEXT")
      )
    )
    script.authUserContactsMap = mapOf(
      UUID1 to listOf(
        Contact(UUID1, "MOBILE_PHONE", "1111", false),
        Contact(UUID1, "SECONDARY_EMAIL", "email1", false)
      ),
      UUID2 to listOf(
        Contact(UUID2, "MOBILE_PHONE", "2222", false),
        Contact(UUID2, "SECONDARY_EMAIL", "email2", false)
      ),
      UUID3 to listOf(
        Contact(UUID3, "MOBILE_PHONE", "3333", false),
        Contact(UUID3, "SECONDARY_EMAIL", "email3", false),
      ),
    )

    script.doMigrate(
      withCMDResultSet("aaa11a", "email1", "1111", true, true)
    )
    script.doMigrate(
      withCMDResultSet("aaa11b", "email2", "2222", true, true)
    )
    script.doMigrate(
      withCMDResultSet("aaa11c", "email3", "3333", true, true)
    )

    verify(auth).update("update users set mfa_preference=? where user_id = ?", "SECONDARY_EMAIL", UUID1)
    // pref of 2nd is already SECONDARY_EMAIL
    verify(auth).update("update users set mfa_preference=? where user_id = ?", "SECONDARY_EMAIL", UUID3)
    verifyNoMoreInteractions(auth)
    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Ignore when nothing in auth tables`() {
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", true, false)
    )

    verifyNoInteractions(auth)
    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Insert when only user in auth tables`() {
    script.authUsersMap.put("AAA11A", User(UUID1, "AAA11A", "other", "EMAIL"))
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", false, true)
    )

    verify(auth).update("update users set mfa_preference=? where user_id = ?", "TEXT", UUID1)
    verify(insertIntoUsers, never()).executeAndReturnKeyHolder(any<Map<String, *>>())
    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "SECONDARY_EMAIL",
        "details" to "email2",
        "verified" to false
      )
    )
    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "MOBILE_PHONE",
        "details" to "0123456",
        "verified" to true
      )
    )
    verifyNoMoreInteractions(insertIntoUserContact)
  }

  /**
   * add or update mobile no, set to TEXT,
   Add 2ndary email if not present and in CMD
   */
  @Test
  fun `Existing with CMD useSMS update with email`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf(
      UUID1 to listOf(
        Contact(UUID1, "MOBILE_PHONE", "9999", false),
        Contact(UUID1, "SECONDARY_EMAIL", "other", false),
      )
    )

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", false, true)
    )

    verify(auth).update(UPDATE_USER_CONTACT, "0123456", UUID1, "MOBILE_PHONE")
    verify(auth).update(UPDATE_MFA_PREFERENCE, "TEXT", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Existing with CMD useSMS update, no email`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf(
      UUID1 to listOf(
        Contact(UUID1, "MOBILE_PHONE", "9999", false),
        Contact(UUID1, "SECONDARY_EMAIL", "other", false),
      )
    )

    script.doMigrate(
      withCMDResultSet("aaa11a", null, "0123456", false, true)
    )

    verify(auth).update(UPDATE_USER_CONTACT, "0123456", UUID1, "MOBILE_PHONE")
    verify(auth).update(UPDATE_MFA_PREFERENCE, "TEXT", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Existing with CMD useSMS insert`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", false, true)
    )

    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "SECONDARY_EMAIL",
        "details" to "email2",
        "verified" to false
      )
    )
    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "MOBILE_PHONE",
        "details" to "0123456",
        "verified" to true
      )
    )
    verifyNoMoreInteractions(insertIntoUserContact)

    verify(auth).update(UPDATE_MFA_PREFERENCE, "TEXT", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
  }

  /**
   * add or update 2ndary email, set to SECONDARY_EMAIL,
   Add no if not present and in CMD
   */
  @Test
  fun `Existing with CMD useEmailAddress update`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf(
      UUID1 to listOf(
        Contact(UUID1, "MOBILE_PHONE", "9999", false),
        Contact(UUID1, "SECONDARY_EMAIL", "other", false),
      )
    )

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", true, false)
    )

    verify(auth).update(UPDATE_USER_CONTACT, "email2", UUID1, "SECONDARY_EMAIL")
    verify(auth).update(UPDATE_MFA_PREFERENCE, "SECONDARY_EMAIL", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
    verifyNoInteractions(insertIntoUserContact)
  }

  @Test
  fun `Existing with CMD useEmailAddress insert with sms`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", "0123456", true, false)
    )

    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "SECONDARY_EMAIL",
        "details" to "email2",
        "verified" to true
      )
    )
    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "MOBILE_PHONE",
        "details" to "0123456",
        "verified" to false
      )
    )
    verifyNoMoreInteractions(insertIntoUserContact)

    verify(auth).update(UPDATE_MFA_PREFERENCE, "SECONDARY_EMAIL", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
  }

  @Test
  fun `Existing with CMD useEmailAddress insert, no sms`() {
    script.authUsersMap.putAll(
      mapOf("AAA11A" to User(UUID1, "AAA11A", "email1", "EMAIL"))
    )
    script.authUserContactsMap = mapOf()

    script.doMigrate(
      withCMDResultSet("aaa11a", "email2", null, true, false)
    )

    verify(insertIntoUserContact).execute(
      mapOf(
        "user_id" to UUID1,
        "type" to "SECONDARY_EMAIL",
        "details" to "email2",
        "verified" to true
      )
    )
    verifyNoMoreInteractions(insertIntoUserContact)

    verify(auth).update(UPDATE_MFA_PREFERENCE, "SECONDARY_EMAIL", UUID1)
    verifyNoMoreInteractions(auth)

    verifyNoInteractions(insertIntoUsers)
  }

  private fun withCMDResultSet(
    quantumId: String,
    emailAddress: String?,
    sms: String?,
    useEmailAddress: Boolean,
    useSms: Boolean,
  ): ResultSet {
    val cmdResult: ResultSet = mock()
    whenever(cmdResult.getString("QuantumId")).thenReturn(quantumId)
    whenever(cmdResult.getString("EmailAddress")).thenReturn(emailAddress)
    whenever(cmdResult.getString("Sms")).thenReturn(sms)
    whenever(cmdResult.getBoolean("UseEmailAddress")).thenReturn(useEmailAddress)
    whenever(cmdResult.getBoolean("UseSms")).thenReturn(useSms)
    return cmdResult
  }
}
