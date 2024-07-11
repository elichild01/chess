package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
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

    public ListResult list(ListRequest request) {
        return null;
    }

    public CreateResult create(CreateRequest request) {
        return null;
    }

    public JoinResult join(JoinRequest request) {
        return null;
    }

}
