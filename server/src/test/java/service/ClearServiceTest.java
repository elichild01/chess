package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ClearServiceTest {

    static Stream<Arguments> dataAccessTypes() {
        return Stream.of(
                Arguments.of(MemoryUserDataAccess.class, MemoryAuthDataAccess.class, MemoryGameDataAccess.class)
        );
    }

    // register
    @ParameterizedTest
    @MethodSource("dataAccessTypes")
    public void clear(Class<? extends UserDataAccess> userDataAccessClass, Class<? extends AuthDataAccess> authDataAccessClass,
                      Class<? extends GameDataAccess> gameDataAccessClass) throws Exception {
        var userDataAccess = userDataAccessClass.getDeclaredConstructor().newInstance();
        var authDataAccess = authDataAccessClass.getDeclaredConstructor().newInstance();
        var gameDataAccess = gameDataAccessClass.getDeclaredConstructor().newInstance();

        userDataAccess.addUser(new UserData("cosmo", "the", "Cougar"));
        authDataAccess.createAuth("cosmo");
        gameDataAccess.createGame("Cosmo's Game.");

        assertNotEquals(0, userDataAccess.getNumUsers());
        assertNotEquals(0, authDataAccess.retrieveNumAuths());
        assertNotEquals(0, userDataAccess.getNumUsers());

        ClearService service = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
        service.clear();

        assertEquals(0, userDataAccess.getNumUsers());
        assertEquals(0, authDataAccess.retrieveNumAuths());
        assertEquals(0, userDataAccess.getNumUsers());
    }
}