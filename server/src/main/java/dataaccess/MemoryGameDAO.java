package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(String gameName) throws DataAccessException {
        String whiteUsername = "";
        String blackUsername = "";
        int gameId = nextId++;
        ChessGame game = new ChessGame();

        GameData gameData = new GameData(gameId, whiteUsername, blackUsername, gameName, game);
        games.put(gameId, gameData);
        return gameData;
    }

    public void deleteAllGames() {
        games.clear();
    }

    public Collection<GameData> listAllGames() {
        return new ArrayList<>(games.values());
    }
}
