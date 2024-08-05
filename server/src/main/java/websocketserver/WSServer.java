package websocketserver;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;

import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import requestresult.ListRequest;
import service.ClearService;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;

@WebSocket
public class WSServer {

    private final ConnectionManager connections;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;


    public WSServer(UserService userService, GameService gameService, ClearService clearService) {
        this.connections = new ConnectionManager();
        this.userService = userService;
        this.gameService = gameService;
        this.clearService = clearService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        var command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> makeMove(session, message);
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    private void connect(Session session, UserGameCommand command) throws IOException {
        String username;
        try {
            username = getUsernameFromAuth(command.getAuthToken());
        } catch (DataAccessException ex) {
            String notification = String.format("Unable to join game. Error: %s", ex.getMessage());
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, notification);
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        connections.add(username, session, command.getGameID());

        GameData game = retrieveGameFromDatabase(session, command);

        if (game != null) {
            // notifyRootUser LOAD_GAME message
            LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
            session.getRemote().sendString(new Gson().toJson(loadGameMessage));

            // notify other clients of connection
            String gameRole;
            if (game.whiteUsername().equals(username)) {
                gameRole = "playing white.";
            } else if (game.blackUsername().equals(username)) {
                gameRole = "playing black.";
            } else {
                gameRole = "as an observer.";
            }
            String notification = String.format("%s has joined the game %s", username, gameRole);
            NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);

            connections.broadcast(username, notificationMessage, game.gameID());
        }
    }

    private void makeMove(Session session, String message) throws IOException, DataAccessException {
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);

        // retrieve game, username from database
        GameData gameData;
        String username;
        try {
            username = getUsernameFromAuth(command.getAuthToken());
            gameData = retrieveGameFromDatabase(session, command);
            if (gameData == null) { throw new DataAccessException("game not found"); }
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        // ensure we are actually playing the game
        ChessGame.TeamColor thisPlayerColor = getThisPlayerColor(username, gameData);
        if (thisPlayerColor == null) {
            String errorDescription = String.format("%s is not one of the game players.", username);
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", errorDescription));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

//        Server verifies the validity of the move.
//        Game is updated to represent the move in the database.
        try {
            if (gameData.game().getBoard().getPiece(command.getMove().getStartPosition()).getTeamColor() != thisPlayerColor) {
                throw new InvalidMoveException("Can't move other player's piece.");
            }
            gameData.game().makeMove(command.getMove());
            gameService.update(gameData);
        } catch (InvalidMoveException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    String.format("Move could not be made. Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    String.format("Database could not be updated. Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

//        Server sends a LOAD_GAME message to all clients in the game (including the root client) with an updated game.
        try {
            LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
            connections.broadcast("", loadGameMessage, gameData.gameID());
        } catch (IOException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

//        Server sends a Notification message to all other clients in that game informing them what move was made.
        try {
            String moveDescription = String.format("%s has made move %s", username, command.getMove());
            NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveDescription);
            connections.broadcast(username, notificationMessage, gameData.gameID());
        } catch (IOException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

//        If the move results in check, checkmate or stalemate the server sends a Notification message to all clients.
        ChessGame.TeamColor otherPlayerColor = getOtherPlayerColor(username, gameData);
        String otherPlayerUsername = getOtherPlayerUsername(username, gameData);
        if (gameData.game().isInCheckmate(otherPlayerColor)) {
            gameData.game().endGame();
            gameService.update(gameData);
            String notificationDescription = String.format("%s has checkmated %s! Game over.", username, otherPlayerUsername);
            NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
            connections.broadcast("", notificationMessage, gameData.gameID());
            return;
        }
        if (gameData.game().isInStalemate(otherPlayerColor)) {
            gameData.game().endGame();
            gameService.update(gameData);
            String notificationDescription = String.format("%s and %s are in stalemate! Game over.", username, otherPlayerUsername);
            NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
            connections.broadcast("", notificationMessage, gameData.gameID());
            return;
        }
        if (gameData.game().isInCheck(otherPlayerColor)) {
            String notificationDescription = String.format("%s has put %s in check!", username, otherPlayerUsername);
            NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
            connections.broadcast("", notificationMessage, gameData.gameID());
        }
    }

    private void leave(Session session, UserGameCommand command) throws IOException, DataAccessException {
        // retrieve game, username from database
        String username;
        GameData gameData;
        try {
            username = getUsernameFromAuth(command.getAuthToken());
            gameData = retrieveGameFromDatabase(session, command);
            if (gameData == null) { throw new DataAccessException("game not found"); }
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        // remove user from game locally and in database
        ChessGame.TeamColor currUserColor = getThisPlayerColor(username, gameData);
        String otherPlayerUsername = getOtherPlayerUsername(username, gameData);
        GameData updatedGame;
        if (currUserColor == ChessGame.TeamColor.WHITE) {
            updatedGame = new GameData(gameData.gameID(), null, otherPlayerUsername, gameData.gameName(), gameData.game());
        } else {
            updatedGame = new GameData(gameData.gameID(), otherPlayerUsername, null, gameData.gameName(), gameData.game());
        }
        gameService.update(updatedGame);

        // notify all users
        String notificationDescription = String.format("%s has left the game.", username);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
        connections.broadcast(username, notificationMessage, gameData.gameID());
    }

    private void resign(Session session, UserGameCommand command) throws IOException, DataAccessException {
        // retrieve game, username from database
        String username;
        GameData gameData;
        try {
            username = getUsernameFromAuth(command.getAuthToken());
            gameData = retrieveGameFromDatabase(session, command);
            if (gameData == null) { throw new DataAccessException("game not found"); }
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        // ensure we are actually playing the game
        ChessGame.TeamColor thisPlayerColor = getThisPlayerColor(username, gameData);
        if (thisPlayerColor == null) {
            String errorDescription = String.format("%s is not one of the game players.", username);
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", errorDescription));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        // ensure the game isn't already over
        if (gameData.game().isGameOver()) {
            String errorDescription = String.format("Game %s has already ended and cannot be resigned.", gameData.gameName());
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", errorDescription));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

        gameData.game().endGame();
        gameService.update(gameData);
        String otherPlayerUsername = getOtherPlayerUsername(username, gameData);
        String notificationDescription = String.format("%s has resigned to %s! Game over.", username, otherPlayerUsername);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
        connections.broadcast("", notificationMessage, gameData.gameID());
    }

    private String getUsernameFromAuth(String authToken) throws DataAccessException {
        AuthData authData = userService.authenticate(authToken);
        return authData.username();
    }

    private GameData retrieveGameFromDatabase(Session session, UserGameCommand command) throws IOException {
        Collection<GameData> gameList;
        try {
            gameList = gameService.list(new ListRequest(command.getAuthToken())).games();
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            throw new IOException(ex.getMessage());
        }
        for (GameData currGame : gameList) {
            if (currGame.gameID() == command.getGameID()) {
                return currGame;
            }
        }
        ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: no game found with gameID %d", command.getGameID()));
        session.getRemote().sendString(new Gson().toJson(errorMessage));
        return null;
    }

    private ChessGame.TeamColor getOtherPlayerColor(String myUsername, GameData gameData) throws IOException {
        if (myUsername.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else if (myUsername.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else {
            throw new IOException("Move attempted by username not in game.");
        }
    }

    private ChessGame.TeamColor getThisPlayerColor(String myUsername, GameData gameData) {
        if (myUsername.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (myUsername.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }

    private String getOtherPlayerUsername(String myUsername, GameData gameData) throws IOException {
        if (myUsername.equals(gameData.whiteUsername())) {
            return gameData.blackUsername();
        } else if (myUsername.equals(gameData.blackUsername())) {
            return gameData.whiteUsername();
        } else {
            throw new IOException("Move attempted by username not in game.");
        }
    }
}
