package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLUserDataAccessTest {

    private static UserData existingUser;
    private static UserData newUser;
    private static MySQLUserDataAccess database;

    @BeforeAll
    public static void init() throws DataAccessException {
        existingUser = new UserData("existingUsername", "existingPassword", "existingEmail");
        newUser = new UserData("newUsername", "newPassword", "newEmail");
        database = new MySQLUserDataAccess();
    }

    @BeforeEach
    public void addStarterUser() throws DataAccessException {
        try {
            database.addUser(existingUser);
        } catch (DataAccessException ex) {
            if (!ex.getMessage().equals("already taken")) {
                throw ex;
            }
        }
    }

    // getUser
    @Test
    void getUserReturnsCorrectUsername() throws DataAccessException {
        UserData retrievedUser = database.getUser(existingUser.username());
        assertEquals(retrievedUser.username(), existingUser.username());
    }
    @Test
    void getUserNonexistentUserReturnsNull() throws DataAccessException {
        UserData user = database.getUser("not-a-username");
        assertNull(user);
    }

    // addUser
    @Test
    void addUser() {
        assertDoesNotThrow(() -> database.addUser(newUser));
    }
    @Test
    void addUserDuplicateUsernameThrowsException() {
        assertThrows(DataAccessException.class, () -> database.addUser(existingUser));
    }

    // deleteAllUsers
    @Test
    void deleteAllUsers() throws DataAccessException {
        assertDoesNotThrow(() -> database.deleteAllUsers());
        assertEquals(0, database.getNumUsers());
    }

    // getNumUsers
    @Test
    void getNumUsers() {

    }
}