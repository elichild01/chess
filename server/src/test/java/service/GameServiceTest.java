package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
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
                Arguments.of(MySQLAuthDataAccess.class, MySQLGameDataAccess.class),
                Arguments.of(MemoryAuthDataAccess.class, MemoryGameDataAccess.class)
        );
    }

    static Stream<Arguments> dataAccessTypesWithUser() {
        return Stream.of(
                Arguments.of(MySQLUserDataAccess.class, MySQLAuthDataAccess.class, MySQLGameDataAccess.class),
                Arguments.of(MemoryUserDataAccess.class, MemoryAuthDataAccess.class, MemoryGameDataAccess.class)
        );
    }

    // create
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void createGame(Class<? extends AuthDataAccess> authDataAccessClass,
                             Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        AuthData auth;
        String presidente = "Presidente";
        try {
            auth = authDataAccess.createAuth(presidente);
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("user already logged in")) {
                auth = authDataAccess.retrieveAuthByUsername(presidente);
            } else {
                throw ex;
            }
        }
        CreateRequest request = new CreateRequest(auth.authToken(), "'s Juego");

        CreateResult result = service.create(request);
        assertNotNull(result);
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void createGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDataAccessClass,
                             Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        CreateRequest request = new CreateRequest("fake-auth-token", "forbidden-game");
        assertThrows(DataAccessException.class, () -> service.create(request));
    }

    // list
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void listAllGamesSizeIncrementsCorrectly(Class<? extends AuthDataAccess> authDataAccessClass,
                             Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        AuthData auth = authDataAccess.createAuth("Talmage");
        ListRequest request = new ListRequest(auth.authToken());
        ListResult result = service.list(request);

        assertNotNull(result);
        int numGamesBefore = result.games().size();
        String gameName = "Math";
        gameDataAccess.createGame(gameName);
        result = service.list(request);
        assertEquals(numGamesBefore + 1, result.games().size());
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void listGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDataAccessClass,
                                                      Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        ListRequest request = new ListRequest("fake-auth-token");
        assertThrows(DataAccessException.class, () -> service.list(request));
    }

    // join
    @ParameterizedTest
    @MethodSource("dataAccessTypesWithUser")
    public void joinGame(Class<? extends UserDataAccess> userDataAccessClass,
                         Class<? extends AuthDataAccess> authDataAccessClass,
                         Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        UserData userToJoin = new UserData("BYU students", "sure love their", "ice cream");
        addUserIfNotAlreadyInDatabase(userDataAccess, userToJoin);

        gameDataAccess.deleteAllGames();
        GameData game = gameDataAccess.createGame("CONE");
        AuthData auth = authDataAccess.createAuth(userToJoin.username());
        JoinRequest request = new JoinRequest(auth.authToken(), ChessGame.TeamColor.WHITE, game.gameID());

        service.join(request);
        Collection<GameData> allGames = gameDataAccess.listAllGames();
        GameData firstGame = allGames.toArray(new GameData[0])[0];

        assertEquals(userToJoin.username(), firstGame.whiteUsername());
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void joinGameUnauthorizedThrowsException(Class<? extends AuthDataAccess> authDataAccessClass,
                                                    Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        GameData game = gameDataAccess.createGame("fun-game");
        JoinRequest request = new JoinRequest("fake-auth-token", ChessGame.TeamColor.WHITE, game.gameID());
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void joinFakeGameThrowsException(Class<? extends AuthDataAccess> authDataAccessClass,
                                                    Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        AuthData auth = authDataAccess.createAuth("Jimmer");
        JoinRequest request = new JoinRequest(auth.authToken(), ChessGame.TeamColor.WHITE, 1234);
        assertThrows(DataAccessException.class, () -> service.join(request));
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypesWithUser")
    public void joinSpotAlreadyTakenThrowsException(Class<? extends UserDataAccess> userDataAccessClass,
                                                    Class<? extends AuthDataAccess> authDataAccessClass,
                                                    Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();
        GameService service = new GameService(authDataAccess, gameDataAccess);

        addUserIfNotAlreadyInDatabase(userDataAccess, new UserData("Jimmer", "Jimmer", "Jimmer"));
        addUserIfNotAlreadyInDatabase(userDataAccess, new UserData("Fredette", "Fredette", "Fredette"));
        AuthData auth1 = authDataAccess.createAuth("Jimmer");
        AuthData auth2 = authDataAccess.createAuth("Fredette");
        GameData game = gameDataAccess.createGame("every-game-is-Jimmer's-game");
        JoinRequest request1 = new JoinRequest(auth1.authToken(), ChessGame.TeamColor.WHITE, game.gameID());
        JoinRequest request2 = new JoinRequest(auth2.authToken(), ChessGame.TeamColor.WHITE, game.gameID());
        assertDoesNotThrow(() -> service.join(request1));
        assertThrows(DataAccessException.class, () -> service.join(request2));
    }

    private void addUserIfNotAlreadyInDatabase(UserDataAccess userDataAccess, UserData userData) throws DataAccessException {
        try {
            userDataAccess.addUser(userData);
        } catch (DataAccessException ex) {
            if (!ex.getMessage().equals("already taken")) {
                throw ex;
            }
        }
    }
}