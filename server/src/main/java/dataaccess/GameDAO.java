package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName);
    void deleteAllGames() throws DataAccessException;
    Collection<GameData> listAllGames() throws DataAccessException;
}
