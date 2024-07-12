package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import dataaccess.UserDataAccess;
import requestresult.ClearResult;

public class ClearService extends Service {
    private final UserDataAccess userDataAccess;
    private final GameDataAccess gameDataAccess;

    public ClearService(UserDataAccess userDataAccess, AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
        super(authDataAccess);
        this.userDataAccess = userDataAccess;
        this.gameDataAccess = gameDataAccess;
    }

    public ClearResult clear() throws DataAccessException {
        userDataAccess.deleteAllUsers();
        authDataAccess.deleteAllAuths();
        gameDataAccess.deleteAllGames();
        return new ClearResult();
    }
}
