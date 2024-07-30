package client;

import model.UserData;
import org.junit.jupiter.api.*;
import requestresult.LoginResult;
import server.Server;
import serverfacade.ServerFacade;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final UserData existingUser = new UserData("existingUsername", "existingPassword", "existingEmail");
    private static final UserData newUser = new UserData("newUsername", "newPassword", "newEmail");

    @BeforeAll
    public static void init() throws IOException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
        facade.clear();
        facade.register(existingUser.username(), existingUser.password(), existingUser.email());
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

    // logout

    // list

    // create

    // join

    // clear

}
