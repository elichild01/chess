package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import dataaccess.UserDataAccess;
import requestresult.ClearResult;

public class ClearService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;
    private final GameDataAccess gameDataAccess;

    public ClearService(UserDataAccess userDataAccess, AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
        this.gameDataAccess = gameDataAccess;
    }

    public ClearResult clear() throws DataAccessException {
        userDataAccess.deleteAllUsers();
        authDataAccess.deleteAllAuths();
        gameDataAccess.deleteAllGames();
        return new ClearResult();
    }
}
