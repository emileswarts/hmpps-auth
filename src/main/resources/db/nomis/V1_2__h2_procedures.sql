CREATE ALIAS lock_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void lockUser(Connection conn, String username) throws SQLException {
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var statement = conn.prepareStatement(lockSql);
    statement.setString(1, "LOCKED");
    statement.setString(2, username);
    statement.executeUpdate();
}
$$;

CREATE ALIAS unlock_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void unlockUser(Connection conn, String username) throws SQLException {
    // no action required as h2 does not have concept of locking / unlocking users
}
$$;

CREATE ALIAS change_password AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void changePassword(Connection conn, String username, String password) throws SQLException {
    final var changePasswordSql = String.format("ALTER USER %s SET password ?", username);
    final var statement = conn.prepareStatement(changePasswordSql);
    statement.setString(1, username);
    statement.executeUpdate();

    // also need to set the account to OPEN here replicating the oracle setting password removing the expiry
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var lockStatement = conn.prepareStatement(lockSql);
    lockStatement.setString(1, "OPEN");
    lockStatement.setString(2, username);
    lockStatement.executeUpdate();
}
$$;
