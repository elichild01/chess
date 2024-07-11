package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import model.AuthData;
import model.GameData;
import requestresult.*;

import java.util.Collection;

public class GameService {
    private final GameDataAccess gameDataAccess;
    private final AuthDataAccess authDataAccess;

    public GameService(AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public ListResult list(ListRequest request) throws DataAccessException {
        AuthData auth = authDataAccess.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        Collection<GameData> games = gameDataAccess.listAllGames();
        return new ListResult(games);
    }

    public CreateResult create(CreateRequest request) throws DataAccessException{
        AuthData auth = authDataAccess.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        GameData game = gameDataAccess.createGame(request.gameName());
        return new CreateResult(game.gameID());
    }

    public JoinResult join(JoinRequest request) throws DataAccessException {
        AuthData auth = authDataAccess.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        gameDataAccess.joinGame(request.playerColor(), request.gameID(), auth.username());
        return new JoinResult();
    }
}
