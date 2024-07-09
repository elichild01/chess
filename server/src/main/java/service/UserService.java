package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requestresult.*;

public class UserService {
    private final UserDAO userDb;
    private final AuthDAO authDb;

    public UserService(UserDAO userDb, AuthDAO authDb) {
        this.userDb = userDb;
        this.authDb = authDb;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // add user (exception raised iff username already in use
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDb.addUser(newUser);

        // create auth
        AuthData newAuth = authDb.createAuth(request.username());

        return new RegisterResult(request.username(), newAuth.authToken());
    }

    public int getNumUsers() throws DataAccessException {
        return this.userDb.getNumUsers();
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = this.userDb.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = this.authDb.createAuth(user.username());
        return new LoginResult(user.username(), auth.authToken());
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        AuthData auth = authDb.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        authDb.deleteAuth(request.authToken());
        return new LogoutResult();
    }
}
