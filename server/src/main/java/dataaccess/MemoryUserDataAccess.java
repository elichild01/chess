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

    public UserData getUser(String username) {
        for (UserData user : users.values()) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void deleteAllUsers() {
        users.clear();
    }

    public int getNumUsers() {
        return users.size();
    }
}
