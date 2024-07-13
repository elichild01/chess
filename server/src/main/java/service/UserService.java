package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccess;
import model.AuthData;
import model.UserData;
import requestresult.*;

public class UserService extends Service {
    private final UserDataAccess userDataAccess;

    public UserService(UserDataAccess userDataAccess, AuthDataAccess authDataAccess) {
        super(authDataAccess);
        this.userDataAccess = userDataAccess;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        nullCheck(request);
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
        nullCheck(request);
        if (request.username() == null) {
            throw new DataAccessException("bad request");
        }
        UserData user = this.userDataAccess.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = this.authDataAccess.createAuth(user.username());
        return new LoginResult(user.username(), auth.authToken());
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
//        nullCheck(request);
        authenticate(request.authToken());
        authDataAccess.deleteAuth(request.authToken());
        return new LogoutResult();
    }
}
