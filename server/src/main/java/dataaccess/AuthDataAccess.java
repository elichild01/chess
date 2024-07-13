package dataaccess;

import model.AuthData;

public interface AuthDataAccess {
    AuthData createAuth(String username) throws DataAccessException;
    AuthData retrieveAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void deleteAllAuths() throws DataAccessException;
    int retrieveNumAuths() throws DataAccessException;
}
