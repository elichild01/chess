package dataaccess;

import model.AuthData;

public interface AuthDataAccess {
    AuthData createAuth(String username) throws DataAccessException;
    AuthData retrieveAuthByAuthToken(String authToken) throws DataAccessException;
    AuthData retrieveAuthByUsername(String username) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void deleteAllAuths() throws DataAccessException;
    int retrieveNumAuths() throws DataAccessException;
}
