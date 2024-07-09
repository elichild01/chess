package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import requestresult.ClearResult;

public class ClearService {
    private final UserDAO userDb;
    private final AuthDAO authDb;
    private final GameDAO gameDb;

    public ClearService(UserDAO userDb, AuthDAO authDb, GameDAO gameDb) {
        this.userDb = userDb;
        this.authDb = authDb;
        this.gameDb = gameDb;
    }

    public ClearResult clear() throws DataAccessException {
        userDb.deleteAllUsers();
        authDb.deleteAllAuths();
        gameDb.deleteAllGames();
        return new ClearResult();
    }
}
