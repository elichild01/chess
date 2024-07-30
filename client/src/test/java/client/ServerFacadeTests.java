package client;

import chess.ChessGame;
import model.UserData;
import org.junit.jupiter.api.*;
import requestresult.*;
import server.Server;
import serverfacade.ServerFacade;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final UserData existingUser = new UserData("existingUsername", "existingPassword", "existingEmail");
    private static String existingAuth;
    private static final UserData newUser = new UserData("newUsername", "newPassword", "newEmail");

    @BeforeAll
    public static void init() throws IOException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
        facade.clear();
        facade.register(existingUser.username(), existingUser.password(), existingUser.email());
        existingAuth = facade.login(existingUser.username(), existingUser.password()).authToken();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // login
    @Test
    public void loginNormal() throws IOException {
        assertDoesNotThrow(() -> facade.login(existingUser.username(), existingUser.password()));
        LoginResult result = facade.login(existingUser.username(), existingUser.password());
        assertTrue(result.authToken().length() >= 10);
    }
    @Test
    public void loginBadPassword() {
        assertThrows(IOException.class, () -> facade.login(existingUser.username(), newUser.password()));
    }

    // register
    @Test
    public void registerNormal() throws IOException {
        RegisterResult result = facade.register(newUser.username(), newUser.password(), newUser.email());
        assertEquals(newUser.username(), result.username());
        assertTrue(result.authToken().length() > 10);
    }
    @Test
    public void registerExistingUser() {
        assertThrows(IOException.class, () -> facade.register(existingUser.username(), existingUser.password(), existingUser.email()));
    }

    // logout
    @Test
    public void logoutNormal() throws IOException {
        LoginResult loginResult = facade.login(existingUser.username(), existingUser.password());
        assertDoesNotThrow(() -> facade.logout(loginResult.authToken()));
    }
    @Test
    public void logoutFakeAuth() {
        assertThrows(IOError.class, () -> facade.logout("fake-auth-token"));
    }

    // list
    @Test
    public void listNormal() throws IOException {
        ListResult result = facade.list(existingAuth);
        assertInstanceOf(Collection.class, result.games());
    }
    @Test
    public void listBadAuth() {
        assertThrows(IOException.class, () -> facade.list("fake-auth"));
    }

    // create
    @Test
    public void createNormal() throws IOException {
        CreateResult result = facade.create(existingAuth, "newGame");
        assertTrue(result.gameID() >= 1);
    }
    @Test
    public void createBadAuth() {
        assertThrows(IOException.class, () -> facade.create("bad-auth", "newGame"));
    }

    // join
    @Test
    public void joinNormal() {
        assertDoesNotThrow(() -> facade.join(existingAuth, ChessGame.TeamColor.WHITE, 1));
    }
    @Test
    public void joinNonexistentGame() {
        assertThrows(IOException.class, () -> facade.join(existingAuth, ChessGame.TeamColor.WHITE, -1));
    }

    // clear
    @Test
    public void clear() {
        assertDoesNotThrow(() -> facade.clear());
    }
}
