package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDataAccess implements UserDataAccess {
    private int nextId = 1;
    final private HashMap<Integer, UserData> users = new HashMap<>();

    public void addUser(UserData user) throws DataAccessException {
        for (UserData currUser : users.values()) {
            if (currUser.username().equals(user.username())) {
                throw new DataAccessException("already taken");
            }
        }
        users.put(nextId++, user);
    }

    public UserData getUser(String username) throws DataAccessException {
        UserData userToReturn = null;
        for (UserData currUser : users.values()) {
            if (currUser.username().equals(username)) {
                if (userToReturn != null) {
                    throw new DataAccessException("More than one user with same username found.");
                } else {
                    userToReturn = currUser;
                }
            }
        }
        return userToReturn;
    }

    public void deleteAllUsers() {
        users.clear();
    }

    public int getNumUsers() {
        return users.size();
    }
}
