package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import requestresult.*;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDb;
    private final AuthDAO authDb;

    public GameService(AuthDAO authDb, GameDAO gameDb) {
        this.gameDb = gameDb;
        this.authDb = authDb;
    }

    public ListResult list(ListRequest request) throws DataAccessException {
        AuthData auth = authDb.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        Collection<GameData> games = gameDb.listAllGames();
        return new ListResult(games);
    }

    public CreateResult create(CreateRequest request) throws DataAccessException{
        return null;
    }

    public JoinResult join(JoinRequest request) throws DataAccessException {
        return null;
    }

}
