package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.UserData;
import requestresult.*;

public class UserService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;

    public UserService(UserDataAccess userDataAccess, AuthDataAccess authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // add user (exception raised iff username already in use
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("bad request");
        }
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDataAccess.addUser(newUser);

        // create auth
        AuthData newAuth = authDataAccess.createAuth(request.username());

        return new RegisterResult(request.username(), newAuth.authToken());
    }

    public int getNumUsers() throws DataAccessException {
        return this.userDataAccess.getNumUsers();
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = this.userDataAccess.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = this.authDataAccess.createAuth(user.username());
        return new LoginResult(user.username(), auth.authToken());
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        AuthData auth = authDataAccess.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        authDataAccess.deleteAuth(request.authToken());
        return new LogoutResult();
    }
}
