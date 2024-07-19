package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.List;

public class MySQLGameDataAccess implements GameDataAccess {
    public GameData createGame(String gameName) {
        return null;
    }

    public void deleteAllGames() throws DataAccessException {

    }

    public Collection<GameData> listAllGames() throws DataAccessException {
        return List.of();
    }

    public void joinGame(ChessGame.TeamColor color, int gameID, String username) throws DataAccessException {

    }
}
