package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDataAccessTest {
    private static final UserData existingUser = new UserData("existingUsername", "existingHashedPassword", "existingEmail");
    private static AuthData existingAuth = null;
    private static final UserData newUser = new UserData("newUsername", "newHashedPassword", "newEmail");


    private static Stream<Arguments> dataAccessTypes() {
        return Stream.of(
                Arguments.of(MySQLUserDataAccess.class, MySQLAuthDataAccess.class),
                Arguments.of(MemoryUserDataAccess.class, MemoryAuthDataAccess.class)
        );
    }

    // working skeleton
//    @ParameterizedTest
//    @MethodSource("dataAccessTypes")
//    void esqueleto(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
//        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
//        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
//        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);
//    }

    // createAuth
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void createAuthNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();

        // if newUser is already present due to data persistence, no worries
        try {
            userDataAccess.addUser(newUser);
        } catch (DataAccessException ex) {
            if (!ex.getMessage().equals("already taken")) {
                throw ex;
            }
        }
        AuthData result = authDataAccess.createAuth(newUser.username());
        assertNotNull(result.authToken());
        assertEquals(newUser.username(), result.username());
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLAuthDataAccess.class, MemoryAuthDataAccess.class})
    void createAuthNullUsernameThrowsException(Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        assertThrows(DataAccessException.class, () -> authDataAccess.createAuth(null));
    }

    // retrieveAuthByAuthToken
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void retrieveAuthByAuthTokenNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        AuthData retrievedAuth = authDataAccess.retrieveAuthByAuthToken(existingAuth.authToken());
        assertEquals(retrievedAuth, existingAuth);
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLAuthDataAccess.class, MemoryAuthDataAccess.class})
    void retrieveNonexistentAuthByAuthTokenReturnsNull(Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        AuthData retrievedAuth = authDataAccess.retrieveAuthByAuthToken("not-an-auth");
        assertNull(retrievedAuth);
    }

    // retrieveAuthByUsername
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void retrieveAuthByUsernameNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        AuthData retrievedAuth = authDataAccess.retrieveAuthByUsername(existingAuth.username());
        assertEquals(existingAuth, retrievedAuth);
    }
    @ParameterizedTest
    @ValueSource(classes = {MySQLAuthDataAccess.class, MemoryAuthDataAccess.class})
    void retrieveNonexistentAuthByUsernameReturnsNull(Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        AuthData retrievedAuth = authDataAccess.retrieveAuthByUsername("fake-username");
        assertNull(retrievedAuth);
    }

    // deleteAuth
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void deleteAuthNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        int numAuthsBefore = authDataAccess.retrieveNumAuths();
        authDataAccess.deleteAuth(existingAuth.authToken());
        AuthData retrievedAuth = authDataAccess.retrieveAuthByAuthToken(existingAuth.authToken());

        assertEquals(numAuthsBefore-1, authDataAccess.retrieveNumAuths());
        assertNull(retrievedAuth);
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void deleteNonexistentAuthDoesNotDelete(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        int numAuthsBefore = authDataAccess.retrieveNumAuths();
        authDataAccess.deleteAuth("not-an-auth");

        assertEquals(numAuthsBefore, authDataAccess.retrieveNumAuths());
    }

    // deleteAllAuths
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void deleteAllAuthsNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        authDataAccess.deleteAllAuths();
        AuthData retrievedAuth = authDataAccess.retrieveAuthByAuthToken(existingAuth.authToken());

        assertEquals(0, authDataAccess.retrieveNumAuths());
        assertNull(retrievedAuth);
    }

    // retrieveNumAuths
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void retrieveNumAuthsAtLeastOne(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        assertTrue(0 < authDataAccess.retrieveNumAuths());
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void retrieveNumAuthsNoAuths(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        addExistingUserAuthIfNotPresent(userDataAccess, authDataAccess);

        authDataAccess.deleteAllAuths();

        assertEquals(0, authDataAccess.retrieveNumAuths());
    }

    private void addExistingUserAuthIfNotPresent(UserDataAccess userDataAccess, AuthDataAccess authDataAccess) throws DataAccessException {
        try {
            userDataAccess.addUser(existingUser);
        } catch (DataAccessException ex) {
            if (!ex.getMessage().equals("already taken")) {
                throw ex;
            }
        }
        try {
            existingAuth = authDataAccess.createAuth(existingUser.username());
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("user already logged in")) {
                existingAuth = authDataAccess.retrieveAuthByUsername(existingUser.username());
            } else {
                throw ex;
            }
        }
    }
}
