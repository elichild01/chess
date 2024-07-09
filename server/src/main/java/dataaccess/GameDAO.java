package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public GameData createGame(String gameName) throws DataAccessException;
    public void deleteAllGames() throws DataAccessException;
    public Collection<GameData> listAllGames() throws DataAccessException;
}
