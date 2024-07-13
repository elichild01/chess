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
        nullCheck(request.username());
        nullCheck(request.password());
        nullCheck(request.email());

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDataAccess.addUser(newUser);

        AuthData newAuth = authDataAccess.createAuth(request.username());

        return new RegisterResult(request.username(), newAuth.authToken());
    }

    public int getNumUsers() throws DataAccessException {
        return this.userDataAccess.getNumUsers();
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        nullCheck(request);
        nullCheck(request.username());
        nullCheck(request.password());

        // check for wrong username/password combo
        UserData user = this.userDataAccess.getUser(request.username(), request.password());
        if (user == null) {
            throw new DataAccessException("unauthorized");
        }

        // perform log-in
        AuthData auth = this.authDataAccess.createAuth(user.username());
        return new LoginResult(user.username(), auth.authToken());
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        nullCheck(request);

        // perform log-out
        authenticate(request.authToken());
        authDataAccess.deleteAuth(request.authToken());
        return new LogoutResult();
    }
}
