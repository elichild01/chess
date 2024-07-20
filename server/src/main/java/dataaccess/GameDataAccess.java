package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException;
    void deleteAllGames() throws DataAccessException;
    Collection<GameData> listAllGames() throws DataAccessException;
    void joinGame(ChessGame.TeamColor color, int gameID, String username) throws DataAccessException;
}
