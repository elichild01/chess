package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDataAccessTest {
    private static final UserData existingUser = new UserData("existingUsername", "existingHashedPassword", "existingEmail");
    private static final AuthData existingAuth = new AuthData("existingAuth", "existingUsername");
    private static final UserData newUser = new UserData("newUsername", "newHashedPassword", "newEmail");
    private static final AuthData newAuth = new AuthData("newAuth", "newUsername");


    private static Stream<Arguments> dataAccessTypes() {
        return Stream.of(
                Arguments.of(MySQLUserDataAccess.class, MySQLAuthDataAccess.class),
                Arguments.of(MemoryUserDataAccess.class, MemoryAuthDataAccess.class)
        );
    }

    // working skeleton
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void esqueleto(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
    }

    // createAuth
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void createAuthNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();

        userDataAccess.addUser(newUser);
        AuthData result = authDataAccess.createAuth(newUser.username());
        assertNotNull(result.authToken());
        assertEquals(newUser.username(), result.username());
    }
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void createAuthNullUsernameThrowsException(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();

        assertThrows(DataAccessException.class, () -> authDataAccess.createAuth(null));
    }

    // retrieveAuth
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    void retrieveAuthNormal(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass) throws Exception {
        AuthDataAccess authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        UserDataAccess userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        // FIXME: No assert yet
    }

    // deleteAuth

    // deleteAllAuths

    // retrieveNumAuths
}
