package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameDataAccessTest {

    @BeforeAll
    static void init() {
        new GameDataAccessTest();
    }

    // createGame
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void createGameNormal(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        int numGamesBefore = gameDataAccess.listAllGames().size();
        gameDataAccess.createGame("newGame");

        assertEquals(numGamesBefore+1, gameDataAccess.listAllGames().size());
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void createGameNullGameNameThrowsException(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        assertThrows(DataAccessException.class, () -> gameDataAccess.createGame(null));
    }

    // deleteAllGames
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void deleteAllGames(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        gameDataAccess.deleteAllGames();

        assertEquals(0, gameDataAccess.listAllGames().size());
    }

    // listAllGames
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void listAllGamesNormal(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        GameData newGame = gameDataAccess.createGame("newGame");
        Collection<GameData> allGames = gameDataAccess.listAllGames();
        assertTrue(allGames.contains(newGame));
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void listAllGamesReturnsEmptyListIfNoGames(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        gameDataAccess.deleteAllGames();
        Collection<GameData> allGames = gameDataAccess.listAllGames();

        assertEquals(new ArrayList<>(), allGames);
    }

    // joinGame
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void joinGameNormal(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        gameDataAccess.deleteAllGames();
        GameData gameToJoin = gameDataAccess.createGame("gameToJoin");
        String username = "userWantingIn";
        gameDataAccess.joinGame(ChessGame.TeamColor.BLACK, gameToJoin.gameID(), username);
        GameData retrievedGame = (GameData)gameDataAccess.listAllGames().toArray()[0];

        assertEquals(username, retrievedGame.blackUsername());
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLGameDataAccess.class, MemoryGameDataAccess.class})
    void joinGameSpotAlreadyTaken(Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        GameDataAccess gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        gameDataAccess.deleteAllGames();
        GameData gameToJoin = gameDataAccess.createGame("gameToJoin");
        String user1 = "userWantingIn";
        String user2 = "secondUserWantingIn";
        gameDataAccess.joinGame(ChessGame.TeamColor.BLACK, gameToJoin.gameID(), user1);

        assertThrows(DataAccessException.class, () -> gameDataAccess.joinGame(ChessGame.TeamColor.BLACK, gameToJoin.gameID(), user2));
    }
}
