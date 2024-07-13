package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public abstract class Service {
    protected final AuthDataAccess authDataAccess;

    protected Service(AuthDataAccess authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    protected AuthData authenticate(String authToken) throws DataAccessException {
        nullCheck(authToken);

        AuthData auth = authDataAccess.retrieveAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        return auth;
    }

    protected void nullCheck(Object obj) throws DataAccessException {
        if (obj == null) {
            throw new DataAccessException("bad request");
        }
    }
}
