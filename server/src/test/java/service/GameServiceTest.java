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
    static Stream<Arguments> dataAccessTypes() {
        return Stream.of(
                Arguments.of(MemoryAuthDataAccess.class, MemoryGameDataAccess.class)
        );
    }

    // create
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void createGame(Class<? extends AuthDataAccess> authDAOClass,
                             Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Presidente");
        CreateRequest request = new CreateRequest(auth.authToken(), "'s Juego");

        CreateResult result = service.create(request);
        assertNotNull(result);
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void createGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDAOClass,
                             Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        CreateRequest request = new CreateRequest("fake-auth-token", "forbidden-game");
        assertThrows(DataAccessException.class, () -> service.create(request));
    }

    // list
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void listAllGamesReturnsZeroAndOneCorrectly(Class<? extends AuthDataAccess> authDAOClass,
                             Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Talmage");
        ListRequest request = new ListRequest(auth.authToken());
        ListResult result = service.list(request);

        assertNotNull(result);
        assertEquals(0, result.games().size());
        String gameName = "Math";
        gameDAOInstance.createGame(gameName);
        result = service.list(request);
        assertEquals(1, result.games().size());
        assertEquals(gameName, service.list(request).games().toArray(new GameData[0])[0].gameName());
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void listGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDAOClass,
                                                      Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        ListRequest request = new ListRequest("fake-auth-token");
        assertThrows(DataAccessException.class, () -> service.list(request));
    }

    // join
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void joinGame(Class<? extends AuthDataAccess> authDAOClass,
                             Class<? extends GameDataAccess> gameDAOClass) throws Exception {
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
    @MethodSource("dataAccessTypes")
    public void joinGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDAOClass,
                                                    Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        GameData game = gameDAOInstance.createGame("fun-game");
        JoinRequest request = new JoinRequest("fake-auth-token", ChessGame.TeamColor.WHITE, game.gameID());
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void joinFakeGameThrowsException(Class<? extends AuthDataAccess> authDAOClass,
                                                    Class<? extends GameDataAccess> gameDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDAOInstance, gameDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Jimmer");
        JoinRequest request = new JoinRequest(auth.authToken(), ChessGame.TeamColor.WHITE, 1234);
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void joinSpotAlreadyTakenThrowsException(Class<? extends AuthDataAccess> authDAOClass,
                                            Class<? extends GameDataAccess> gameDAOClass) throws Exception {
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