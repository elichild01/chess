package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

class UserDataAccessTest {
    private static UserData existingUser;
    private static UserData newUser;

    @BeforeAll
    public static void init() {
        String hashedExistingPassword = BCrypt.hashpw("existingPassword", BCrypt.gensalt());
        String hashedNewPassword = BCrypt.hashpw("newPassword", BCrypt.gensalt());
        existingUser = new UserData("existingUsername", hashedExistingPassword, "existingEmail");
        newUser = new UserData("newUsername", hashedNewPassword, "newEmail");
    }

    // getUser
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void getUserReturnsCorrectUsername(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();
        addExistingUserIfNotAlreadyAdded(database);
        UserData retrievedUser = database.getUser(existingUser.username());
        assertEquals(retrievedUser.username(), existingUser.username());
    }
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void getUserNonexistentUserReturnsNull(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();
        addExistingUserIfNotAlreadyAdded(database);
        UserData user = database.getUser("not-a-username");
        assertNull(user);
    }

    // addUser
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void addUser(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();

        try {
            database.addUser(newUser);
        } catch (DataAccessException ex) {
            assertEquals("already taken", ex.getMessage());
        }
        assertEquals(newUser.username(), database.getUser(newUser.username()).username());
    }
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void addUserDuplicateUsernameThrowsException(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();
        addExistingUserIfNotAlreadyAdded(database);
        assertThrows(DataAccessException.class, () -> database.addUser(existingUser));
        try {
            database.addUser(existingUser);
        } catch (DataAccessException ex) {
            assertInstanceOf(DataAccessException.class, ex);
            assertEquals(ex.getMessage(), "already taken");
        }
    }

    // deleteAllUsers
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void deleteAllUsers(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();
        addExistingUserIfNotAlreadyAdded(database);
        assertDoesNotThrow(database::deleteAllUsers);
        assertEquals(0, database.getNumUsers());
    }

    // getNumUsers
    @ParameterizedTest
    @ValueSource(classes={MySQLUserDataAccess.class, MemoryUserDataAccess.class})
    void getNumUsers(Class<? extends UserDataAccess> databaseClass) throws Exception {
        var database = databaseClass.getDeclaredConstructor().newInstance();
        addExistingUserIfNotAlreadyAdded(database);
        assertEquals(1, database.getNumUsers());
    }
    @Test
    void getNumUsersNullDatabaseThrowsNullException() {
        MySQLUserDataAccess sqlUserDataAccess = null;
        MemoryUserDataAccess memUserDataAccess = null;
        assertThrows(NullPointerException.class, () -> sqlUserDataAccess.getNumUsers());
        assertThrows(NullPointerException.class, () -> memUserDataAccess.getNumUsers());
    }

    private void addExistingUserIfNotAlreadyAdded(UserDataAccess database) throws DataAccessException {
        try {
            database.addUser(existingUser);
        } catch (DataAccessException ex) {
            if (!ex.getMessage().equals("already taken")) {
                throw ex;
            }
        }
    }
}