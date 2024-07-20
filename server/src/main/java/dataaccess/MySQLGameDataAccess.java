package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDataAccess implements GameDataAccess {

    public MySQLGameDataAccess() throws DataAccessException {
        DatabaseManager.configureDatabase();
    }

    public GameData createGame(String gameName) throws DataAccessException {
        // create game in memory and database
        ChessGame game = new ChessGame();
        String gameJSON = new Gson().toJson(game, ChessGame.class);
        String statement = "INSERT INTO games (gamejson) VALUES (?)";
        DatabaseManager.executeUpdate(statement, gameJSON);

        // figure out what gameID was generated (most recent gameID for that gameName)
        int gameID;
        try (var conn = DatabaseManager.getConnection()) {
            String query = "SELECT gameid FROM games WHERE gamename=? ORDER BY gameid DESC";
            try (var ps = conn.prepareStatement(query)) {
                ps.setString(1, gameName);
                try (var rs = ps.executeQuery()) {
                    gameID = rs.getInt("gameid");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        // return results
        return new GameData(gameID, null, null, gameName, game);
    }

    public void deleteAllGames() throws DataAccessException {
        String statement = "DELETE FROM games";
        DatabaseManager.executeUpdate(statement);
    }

    public Collection<GameData> listAllGames() throws DataAccessException {
        Collection<GameData> allGames = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            String query = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(query)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int gameID = rs.getInt("gameid");
                        String whiteUsername = rs.getString("whiteusername");
                        String blackUsername = rs.getString("blackusername");
                        String gameName = rs.getString("gamename");
                        String gameJSON = rs.getString("gamejson");
                        ChessGame game = new Gson().fromJson(gameJSON, ChessGame.class);
                        allGames.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return allGames;
    }

    public void joinGame(ChessGame.TeamColor color, int gameID, String username) throws DataAccessException {
        // retrieve game if it exists
        GameData gameData;
        try (var conn = DatabaseManager.getConnection()) {
            String query = "SELECT * FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(query)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("whiteusername");
                        String blackUsername = rs.getString("blackusername");
                        String gameName = rs.getString("gamename");
                        String gameJSON = rs.getString("gamejson");
                        ChessGame game = new Gson().fromJson(gameJSON, ChessGame.class);
                        gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                    } else {
                        throw new DataAccessException("bad request");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        // check if place already taken
        String userToReplace = color == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
        if (userToReplace != null && !userToReplace.isEmpty()) {
            throw new DataAccessException("already taken");
        }

        // update game with username joined
        String updateStatement;
        if (color == ChessGame.TeamColor.WHITE) {
            updateStatement = "UPDATE games SET whiteusername=? WHERE gameid=?";

        } else {
            updateStatement = "UPDATE games SET blackusername=? WHERE gameid=?";
        }
        DatabaseManager.executeUpdate(updateStatement, username, gameID);
    }
}
