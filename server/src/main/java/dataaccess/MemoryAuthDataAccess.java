package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.UUID;

public class MemoryAuthDataAccess implements AuthDataAccess {
    private final HashSet<AuthData> auths = new HashSet<>();

    public AuthData createAuth(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("bad request");
        }

        // check if user already logged in
        for (AuthData auth : auths) {
            if (auth.username().equals(username)) {
                throw new DataAccessException("user already logged in");
            }
        }

        // generate AuthData with unique new authToken
        AuthData proposedAuth;
        do {
            proposedAuth = new AuthData(UUID.randomUUID().toString(), username);
        } while (auths.contains(proposedAuth));

        // store and return result
        auths.add(proposedAuth);
        return proposedAuth;
    }

    public AuthData retrieveAuthByAuthToken(String authToken) {
        for (AuthData currAuth : auths) {
            if (currAuth.authToken().equals(authToken)) {
                return currAuth;
            }
        }
        return null;
    }

    public AuthData retrieveAuthByUsername(String username) {
        for (AuthData currAuth : auths) {
            if (currAuth.username().equals(username)) {
                return currAuth;
            }
        }
        return null;
    }

    public void deleteAuth(String authToken) {
        auths.removeIf(currAuth -> currAuth.authToken().equals(authToken));
    }

    public void deleteAllAuths() {
        auths.clear();
    }

    public int retrieveNumAuths() {
        return auths.size();
    }
}
