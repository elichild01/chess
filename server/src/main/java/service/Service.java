package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public abstract class Service {
    protected final AuthDataAccess authDataAccess;

    protected Service(AuthDataAccess authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public AuthData authenticate(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("bad request");
        }
        AuthData auth = authDataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        return auth;
    }
}
