package dataaccess;

import model.UserData;


import java.sql.*;



public class MySQLUserDataAccess implements UserDataAccess {

    public MySQLUserDataAccess() throws DataAccessException {
        DatabaseManager.configureDatabase();
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, hashedpassword FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void addUser(UserData user) throws DataAccessException {
        // FIXME: need to check and ensure user is unique here

        String statement = "INSERT INTO users (username, hashedpassword, email) VALUES (?, ?, ?)";
        DatabaseManager.executeUpdate(statement, user.username(), user.email(), user.password());
    }

    public void deleteAllUsers() throws DataAccessException {
        String statement = "DELETE * FROM users";
        DatabaseManager.executeUpdate(statement);
    }

    public int getNumUsers() throws DataAccessException {
        String statement = "SELECT COUNT(username) AS NUM_USERS FROM users";

        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("NUM_USERS");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var hashedPassword = rs.getString("hashedpassword");
        var email = rs.getString("email");
        return new UserData(username, hashedPassword, email);
    }
}