package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public AuthData createAuth(String username) throws DataAccessException;
    public AuthData getAuth(String authToken) throws DataAccessException;
    public void deleteAuth(String authToken) throws DataAccessException;
    public void deleteAllAuths() throws DataAccessException;
    public int getNumAuths() throws DataAccessException;
}
