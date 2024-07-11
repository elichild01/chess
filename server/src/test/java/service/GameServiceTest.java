package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requestresult.*;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    static Stream<Arguments> memAndSqlDAOs() {
        return Stream.of(
                Arguments.of(MemoryAuthDAO.class, MemoryGameDAO.class)
        );
    }

    // create
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void createGame(Class<? extends AuthDAO> authDAOClass,
                             Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Presidente");
        CreateRequest request = new CreateRequest(auth.authToken(), "'s Juego");

        CreateResult result = service.create(request);
        assertNotNull(result);
    }
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void createGameUnauthorizedThrowsException(Class<? extends AuthDAO> authDAOClass,
                             Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        CreateRequest request = new CreateRequest("fake-auth-token", "forbidden-game");
        assertThrows(DataAccessException.class, () -> service.create(request));
    }

    // list
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void listAllGamesReturnsZeroAndOneCorrectly(Class<? extends AuthDAO> authDAOClass,
                             Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Talmage");
        ListRequest request = new ListRequest(auth.authToken());
        ListResult result = service.list(request);

        assertNotNull(result);
        assertEquals(0, result.gameList().size());
        String gameName = "Math";
        gameDAOInstance.createGame(gameName);
        result = service.list(request);
        assertEquals(1, result.gameList().size());
        assertEquals(gameName, service.list(request).gameList().toArray(new GameData[0])[0].gameName());
    }
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void listGameUnauthorizedThrowsException(Class<? extends AuthDAO> authDAOClass,
                                                      Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        ListRequest request = new ListRequest("fake-auth-token");
        assertThrows(DataAccessException.class, () -> service.list(request));
    }

    // join
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void joinGame(Class<? extends AuthDAO> authDAOClass,
                             Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        GameData game = gameDAOInstance.createGame("love");
        AuthData auth = authDAOInstance.createAuth("BYU students");
        JoinRequest request = new JoinRequest(auth.authToken(), ChessGame.TeamColor.WHITE, game.gameID());

        assertDoesNotThrow(() -> service.join(request));
        Collection<GameData> allGames = gameDAOInstance.listAllGames();
        GameData firstGame = allGames.toArray(new GameData[0])[0];

        assertEquals("BYU students", firstGame.whiteUsername());
    }
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void joinGameUnauthorizedThrowsException(Class<? extends AuthDAO> authDAOClass,
                                                    Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        GameData game = gameDAOInstance.createGame("fun-game");
        JoinRequest request = new JoinRequest("fake-auth-token", ChessGame.TeamColor.WHITE, game.gameID());
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void joinFakeGameThrowsException(Class<? extends AuthDAO> authDAOClass,
                                                    Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Jimmer");
        JoinRequest request = new JoinRequest(auth.authToken(), ChessGame.TeamColor.WHITE, 1234);
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void spotAlreadyTakenThrowsException(Class<? extends AuthDAO> authDAOClass,
                                            Class<? extends GameDAO> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth1 = authDAOInstance.createAuth("Jimmer");
        AuthData auth2 = authDAOInstance.createAuth("Fredette");
        GameData game = gameDAOInstance.createGame("every-game-is-Jimmer's-game");
        JoinRequest request1 = new JoinRequest(auth1.authToken(), ChessGame.TeamColor.WHITE, game.gameID());
        JoinRequest request2 = new JoinRequest(auth2.authToken(), ChessGame.TeamColor.WHITE, game.gameID());
        assertDoesNotThrow(() -> service.join(request1));
        assertThrows(DataAccessException.class, () -> service.join(request2));
    }
}
