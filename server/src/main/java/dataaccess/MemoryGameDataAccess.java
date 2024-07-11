package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDataAccess implements GameDataAccess {
    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(String gameName) {
        String whiteUsername = "";
        String blackUsername = "";
        int gameID = nextId++;
        ChessGame game = new ChessGame();

        GameData gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, game);
        games.put(gameID, gameData);
        return gameData;
    }

    public void deleteAllGames() {
        games.clear();
    }

    public Collection<GameData> listAllGames() {
        return new ArrayList<>(games.values());
    }

    public void joinGame(ChessGame.TeamColor color, int gameID, String username) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("bad request");
        }
        String userToReplace = color == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();
        if (userToReplace != null && !userToReplace.isEmpty()) {
            throw new DataAccessException("already taken");
        }
        GameData updatedGame;
        if (color == ChessGame.TeamColor.WHITE) {
            updatedGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
        } else {
            updatedGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
        }
        games.put(gameID, updatedGame);
    }
}
