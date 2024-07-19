package dataaccess;

import model.UserData;

public interface UserDataAccess {
    UserData getUser(String username) throws DataAccessException;
    void addUser(UserData user) throws DataAccessException;
    void deleteAllUsers() throws DataAccessException;
    int getNumUsers() throws DataAccessException;
}