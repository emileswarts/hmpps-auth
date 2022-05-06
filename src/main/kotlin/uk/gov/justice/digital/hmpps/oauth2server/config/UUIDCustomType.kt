package uk.gov.justice.digital.hmpps.oauth2server.config

import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.type.PostgresUUIDType
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Custom Hibernate type so User.id is correctly handled by all database types we want Auth to work with - SQLServer, Postgres, and H2.
 * Once we have migrated all environments to Postgres it can be deleted and the Type mapping dropped from User.
 * Adapted from https://zorq.net/b/2012/04/21/switching-hibernates-uuid-type-mapping-per-database
 * however it's no longer accessible so you need to use the Google cached page.
 */
class UUIDCustomType : AbstractSingleColumnStandardBasicType<UUID>(SQL_DESCRIPTOR, UUIDTypeDescriptor.INSTANCE) {
  companion object {
    private val log = LoggerFactory.getLogger(UUIDCustomType::class.java)
    private val SQL_DESCRIPTOR: SqlTypeDescriptor
      get() {
        val hibernateDialect: String? = System.getenv("SPRING_JPA_HIBERNATE_DIALECT")
        if (hibernateDialect == "org.hibernate.dialect.PostgreSQL10Dialect") {
          log.info("Database dialect is Postgres so using pg-uuid type for User.id")
          return PostgresUUIDType.PostgresUUIDSqlTypeDescriptor.INSTANCE
        }
        // otherwise use var char which is compatible with SQLServer and H2
        // uuid-char -> UUIDCharType -> varchar, see here https://github.com/hibernate/hibernate-orm/blob/7d30b57f15617f679a20aa1389c9385433e45b2c/hibernate-core/src/main/java/org/hibernate/type/StandardBasicTypes.java#L1125-L1130
        return VarcharTypeDescriptor.INSTANCE
      }
  }

  override fun getName(): String {
    return "uuid-custom"
  }
}
