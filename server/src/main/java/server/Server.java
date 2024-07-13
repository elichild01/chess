package server;

import com.google.gson.Gson;
import dataaccess.*;
import requestresult.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.Map;

public class Server {
    private final GameService gameService;
    private final UserService userService;
    private final ClearService clearService;
    int successStatus = 200;
    DatabaseType currDatabaseType = DatabaseType.MEMORY;

    public enum DatabaseType {
        MEMORY,
        SQL
    }

    public Server() {
        UserDataAccess userDataAccess;
        AuthDataAccess authDataAccess;
        GameDataAccess gameDataAccess;

        switch (currDatabaseType) {
            case MEMORY:
                userDataAccess = new MemoryUserDataAccess();
                authDataAccess = new MemoryAuthDataAccess();
                gameDataAccess = new MemoryGameDataAccess();
                break;
            case SQL: // I promise this is not dead code but future compatibility.
                userDataAccess = null;
                authDataAccess = null;
                gameDataAccess = null;
                break;
            default:
                throw new RuntimeException("Database type not supported.");
        }

        this.userService = new UserService(userDataAccess, authDataAccess);
        this.gameService = new GameService(authDataAccess, gameDataAccess);
        this.clearService = new ClearService(userDataAccess, authDataAccess, gameDataAccess);
    }

    public Server(UserService userService, GameService gameService, ClearService clearService) {
        this.userService = userService;
        this.gameService = gameService;
        this.clearService = clearService;
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::list);
        Spark.post("/game", this::create);
        Spark.put("/game", this::join);
        Spark.exception(DataAccessException.class, this::exceptionHandler);
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        int statusCode = switch (ex.getMessage()) {
            case "bad request" -> 400;
            case "unauthorized" -> 401;
            case "already taken" -> 403;
            default -> 500;
        };
        res.status(statusCode);
        res.body(new Gson().toJson(Map.of("message", String.format("Error: %s", ex.getMessage()))));
    }

    private Object register(Request req, Response res) throws DataAccessException {
        RegisterRequest request = new Gson().fromJson(req.body(), RegisterRequest.class);
        RegisterResult result = userService.register(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object list(Request req, Response res) throws DataAccessException {
        ListRequest request = new ListRequest(req.headers("authorization"));
        ListResult result = gameService.list(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        ClearResult result = clearService.clear();
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object login(Request req, Response res) throws DataAccessException {
        LoginRequest request = new Gson().fromJson(req.body(), LoginRequest.class);
        LoginResult result = userService.login(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        LogoutRequest request = new LogoutRequest(req.headers("authorization"));
        LogoutResult result = userService.logout(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object create(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        CreateRequest request = new Gson().fromJson(req.body(), CreateRequest.class);

        request = new CreateRequest(authToken, request.gameName());
        CreateResult result = gameService.create(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }

    private Object join(Request req, Response res) throws DataAccessException {
        JoinRequest request = new Gson().fromJson(req.body(), JoinRequest.class);
        String authToken = req.headers("authorization");
        request = new JoinRequest(authToken, request.playerColor(), request.gameID());
        JoinResult result = gameService.join(request);
        res.status(successStatus);
        return new Gson().toJson(result);
    }
}