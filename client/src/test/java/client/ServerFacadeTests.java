package client;

import chess.ChessGame;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final UserData EXISTING_USER = new UserData("existingUsername", "existingPassword", "existingEmail");
    private static String existingAuth;
    private static final UserData NEW_USER = new UserData("newUsername", "newPassword", "newEmail");

    @BeforeAll
    public static void init() throws IOException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
        facade.clear();
        facade.register(EXISTING_USER.username(), EXISTING_USER.password(), EXISTING_USER.email());
        existingAuth = (String)facade.login(EXISTING_USER.username(), EXISTING_USER.password()).get("authToken");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // login
    @Test
    public void loginNormal() throws IOException {
        assertDoesNotThrow(() -> facade.login(EXISTING_USER.username(), EXISTING_USER.password()));
        Map<String, Object> result = facade.login(EXISTING_USER.username(), EXISTING_USER.password());
        assertTrue(((String)result.get("authToken")).length() >= 10);
    }
    @Test
    public void loginBadPassword() throws IOException {
        var result = facade.login(EXISTING_USER.username(), NEW_USER.password());
        assertTrue(result.containsKey("message"));
    }

    // register
    @Test
    public void registerNormal() throws IOException {
        var result = facade.register(NEW_USER.username(), NEW_USER.password(), NEW_USER.email());
        assertEquals(NEW_USER.username(), result.get("username"));
        assertTrue(((String)result.get("authToken")).length() > 10);
    }
    @Test
    public void registerExistingUser() throws IOException {
        facade.register(EXISTING_USER.username(), EXISTING_USER.password(), EXISTING_USER.email());
        var result = facade.register(EXISTING_USER.username(), EXISTING_USER.password(), EXISTING_USER.email());
        assertTrue(result.containsKey("message"));
    }

    // logout
    @Test
    public void logoutNormal() throws IOException {
        var result = facade.login(EXISTING_USER.username(), EXISTING_USER.password());
        assertDoesNotThrow(() -> facade.logout((String)result.get("authToken")));
    }
    @Test
    public void logoutFakeAuth() throws IOException {
        var result = facade.logout("fake-auth-token");
        assertTrue(result.containsKey("message"));
    }

    // list
    @Test
    public void listNormal() throws IOException {
        existingAuth = (String)facade.login(EXISTING_USER.username(), EXISTING_USER.password()).get("authToken");
        facade.create(existingAuth, "funGame");
        var result = facade.list(existingAuth);
        assertInstanceOf(Collection.class, result.games());
    }
    @Test
    public void listBadAuth() throws IOException {
        var result = facade.list("fake-auth");
        assertNotNull(result.message());
    }

    // create
    @Test
    public void createNormal() throws IOException {
        var result = facade.create(existingAuth, "newGame");
        assertTrue(Math.round((double)result.get("gameID")) >= 1);
    }
    @Test
    public void createBadAuth() throws IOException {
        var result = facade.create("bad-auth", "newGame");
        assertTrue(result.containsKey("message"));
    }

    // join
    @Test
    public void joinNormal() {
        assertDoesNotThrow(() -> facade.join(existingAuth, ChessGame.TeamColor.WHITE, 1));
    }
    @Test
    public void joinNonexistentGame() throws IOException {
        var result = facade.join(existingAuth, ChessGame.TeamColor.WHITE, -1);
        assertTrue(result.containsKey("message"));
    }

    // clear
    @Test
    public void clear() {
        assertDoesNotThrow(() -> facade.clear());
    }
}
