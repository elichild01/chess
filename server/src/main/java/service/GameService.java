package service;

import dataaccess.GameDAO;
import model.GameData;
import requestresult.*;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDb;

    public GameService(GameDAO gameDb) {
        this.gameDb = gameDb;
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
