package dataaccess;

import model.AuthData;

public class MySQLAuthDataAccess implements AuthDataAccess {
    public AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    public AuthData retrieveAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {

    }

    public void deleteAllAuths() throws DataAccessException {

    }

    public int retrieveNumAuths() throws DataAccessException {
        return 0;
    }
}
