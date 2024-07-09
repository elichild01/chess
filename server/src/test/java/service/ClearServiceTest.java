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

    static Stream<Arguments> memAndSqlDAOs() {
        return Stream.of(
                Arguments.of(MemoryUserDAO.class, MemoryAuthDAO.class, MemoryGameDAO.class)
        );
    }

    // register
    @ParameterizedTest
    @MethodSource("memAndSqlDAOs")
    public void clear(Class<? extends UserDAO> userDAOClass, Class<? extends AuthDAO> authDAOClass,
                                      Class<? extends GameDAO> gameDAOClass) throws Exception {
        var userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
        var authDAOInstance = authDAOClass.getDeclaredConstructor().newInstance();
        var gameDAOInstance = gameDAOClass.getDeclaredConstructor().newInstance();

        userDAOInstance.addUser(new UserData("cosmo", "the", "Cougar"));
        authDAOInstance.createAuth("cosmo");
        gameDAOInstance.createGame("Cosmo's Game.");

        assertNotEquals(0, userDAOInstance.getNumUsers());
        assertNotEquals(0, authDAOInstance.getNumAuths());
        assertNotEquals(0, userDAOInstance.getNumUsers());

        ClearService service = new ClearService(userDAOInstance, authDAOInstance, gameDAOInstance);
        service.clear();

        assertEquals(0, userDAOInstance.getNumUsers());
        assertEquals(0, authDAOInstance.getNumAuths());
        assertEquals(0, userDAOInstance.getNumUsers());
    }
}