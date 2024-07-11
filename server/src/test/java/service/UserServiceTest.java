package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requestresult.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    static Stream<Arguments> memAndSqlDAOs() {
        return Stream.of(
                Arguments.of(MemoryUserDAO.class, MemoryAuthDAO.class)
        );
    }

    // register
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void addSingleUserAddsUser(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);
        String username = "test";
        String password = "password";
        String email = "test@test.com";
        RegisterRequest request = new RegisterRequest(username, password, email);
        RegisterResult result = null;
        try {
            result = service.register(request);
        } catch (DataAccessException e) {
            fail(String.format("DataAccessException: %s", e.getMessage()));
        }
        assertEquals(username, result.userName());
        assertNotNull(result.authToken());
        assertEquals(1, service.getNumUsers());
    }

    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void addMultipleUsersGivesMultipleUsers(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);

        String[] usernames = {"Alice", "Bob", "Charlie"};
        String[] passwords = {"password", "12345", "my_password"};
        String[] emails = {"email@email.com", "test@test.com", "code@code.com"};
        for (int i = 0; i <= 2; i++) {
            service.register(new RegisterRequest(usernames[i], passwords[i], emails[i]));
        }
        assertEquals(3, service.getNumUsers());
    }

    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void addDuplicateUsernameThrowsException(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);

        RegisterRequest request = new RegisterRequest("myusername", "mypassword", "myemail@test.com");
        assertDoesNotThrow(() -> service.register(request));
        assertThrows(DataAccessException.class, () -> service.register(request));
    }

    // getNumUsers
    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    public void numUsersThrowsExceptionIfNullDatabase(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(null, authDAOInstance);
        assertThrows(NullPointerException.class, service::getNumUsers);
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    public void numUsersReturnsNumUsers(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        MemoryUserDAO users = new MemoryUserDAO();
        for (int i = 0; i <= 5; i++) {
            users.addUser(new UserData(String.format("user %d", i), String.format("pass %d", i),
                    String.format("email %d", i)));
            UserService currService = new UserService(users, authDAOInstance);
            assertEquals(i + 1, currService.getNumUsers());
        }
    }

    // login
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void loginExistingUser(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);

        String username = "go_cougars";
        String password = "white-and-blue";
        String email = "byufan@byu.edu";

        UserData user = new UserData(username, password, email);
        userDAOInstance.addUser(user);

        LoginRequest logRequest = new LoginRequest(username, password);
        LoginResult logResult = service.login(logRequest);

        assertEquals(username, logResult.username());
        assertNotNull(logResult.authToken());
    }

    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void loginNonexistentUser(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);

        String username = "go_cougars";
        String password = "white-and-blue";

        LoginRequest logRequest = new LoginRequest(username, password);
        assertThrows(DataAccessException.class, () -> service.login(logRequest));
    }

    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void loginWrongPassword(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(userDAOInstance, authDAOInstance);

        String username = "go_cougars";
        String password = "white-and-blue";
        String wrongPassword = "white-and-red";
        String email = "byufan@byu.edu";

        UserData user = new UserData(username, password, email);
        userDAOInstance.addUser(user);

        LoginRequest logRequest = new LoginRequest(username, wrongPassword);
        assertThrows(DataAccessException.class, () -> service.login(logRequest));
    }

    // logout
    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    public void standardLogoutSucceeds(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(null, authDAOInstance);

        AuthData auth = authDAOInstance.createAuth("Whoosh");

        LogoutResult result = service.logout(new LogoutRequest(auth.authToken()));
        LogoutResult emptyResult = new LogoutResult();
        assertEquals(result, emptyResult);
        assertNull(authDAOInstance.getAuth(auth.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    public void logoutBadAuthThrowsException(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        UserService service = new UserService(null, authDAOInstance);

        LogoutRequest request = new LogoutRequest("Shane");
        assertThrows(DataAccessException.class, () -> service.logout(request));
    }
}