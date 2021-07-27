CREATE SCHEMA IF NOT EXISTS oms_utils;
CREATE ALIAS oms_utils.lock_user AS $$
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

CREATE ALIAS oms_utils.unlock_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void unlockUser(Connection conn, String username) throws SQLException {
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var lockStatement = conn.prepareStatement(lockSql);
    lockStatement.setString(1, "OPEN");
    lockStatement.setString(2, username);
    lockStatement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.change_user_password AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void changePassword(Connection conn, String username, String password) throws SQLException {
    final var changePasswordSql = String.format("ALTER USER %s SET password ?", username);
    final var statement = conn.prepareStatement(changePasswordSql);
    statement.setString(1, username);
    statement.executeUpdate();

    final var statusSql = "SELECT account_status from dba_users WHERE username = ?";
    final var statusStatement = conn.prepareStatement(statusSql);
    statusStatement.setString(1, username);
    final var resultSet = statusStatement.executeQuery();
    if (resultSet.next()) {
        final var accountStatus = resultSet.getString(1);
        if (accountStatus.equals("EXPIRED")) {
            final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
            final var lockStatement = conn.prepareStatement(lockSql);
            lockStatement.setString(1, "OPEN");
            lockStatement.setString(2, username);
            lockStatement.executeUpdate();
        }
    }
    resultSet.close();
}
$$;
