package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    void addUser(UserData user) throws DataAccessException;
    void deleteAllUsers() throws DataAccessException;
    int getNumUsers() throws DataAccessException;
}
