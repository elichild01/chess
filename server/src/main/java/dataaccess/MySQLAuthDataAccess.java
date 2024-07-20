package dataaccess;

import model.AuthData;

import java.sql.SQLException;
import java.util.UUID;

public class MySQLAuthDataAccess implements AuthDataAccess {

    public MySQLAuthDataAccess() throws DataAccessException {
        DatabaseManager.configureDatabase();
    }

    public AuthData createAuth(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("bad request");
        }
        // generate AuthData with unique new authToken
        AuthData proposedAuth;
        do {
            proposedAuth = new AuthData(UUID.randomUUID().toString(), username);
        } while (retrieveAuthByAuthToken(proposedAuth.authToken()) != null);

        int userID = getUserIDFromUsername(username);

        // store and return result
        String statement = "INSERT INTO auths (userid, authtoken) VALUES (?, ?)";
        DatabaseManager.executeUpdate(statement, userID, proposedAuth.authToken());
        return proposedAuth;
    }

    public AuthData retrieveAuthByAuthToken(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT userid, authtoken FROM auths WHERE authtoken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int userID = rs.getInt("userid");
                        String username = getUsernameFromUserID(userID);
                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public AuthData retrieveAuthByUsername(String username) throws DataAccessException {
        int userID = getUserIDFromUsername(username);

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT userid, authtoken FROM auths WHERE userid=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, userID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String authToken = rs.getString("authtoken");
                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auths WHERE authtoken=?";
        DatabaseManager.executeUpdate(statement, authToken);
    }

    public void deleteAllAuths() throws DataAccessException {
        String statement = "DELETE FROM auths";
        DatabaseManager.executeUpdate(statement);
    }

    public int retrieveNumAuths() throws DataAccessException {
        String statement = "SELECT COUNT(userid) AS NUM_AUTHS FROM auths";

        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("NUM_AUTHS");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    private int getUserIDFromUsername(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT userid FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("userid");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return -1;
    }

    private String getUsernameFromUserID(int userID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM users WHERE userid=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, userID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }
}
