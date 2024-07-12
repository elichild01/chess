package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import model.AuthData;
import model.GameData;
import requestresult.*;

import java.util.Collection;

public class GameService extends Service {
    private final GameDataAccess gameDataAccess;

    public GameService(AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
        super(authDataAccess);
        this.gameDataAccess = gameDataAccess;
    }

    public ListResult list(ListRequest request) throws DataAccessException {
        authenticate(request.authToken());
        Collection<GameData> games = gameDataAccess.listAllGames();
        return new ListResult(games);
    }

    public CreateResult create(CreateRequest request) throws DataAccessException{
        authenticate(request.authToken());
        GameData game = gameDataAccess.createGame(request.gameName());
        return new CreateResult(game.gameID());
    }

    public JoinResult join(JoinRequest request) throws DataAccessException {
        AuthData auth = authenticate(request.authToken());
        gameDataAccess.joinGame(request.playerColor(), request.gameID(), auth.username());
        return new JoinResult();
    }
}
