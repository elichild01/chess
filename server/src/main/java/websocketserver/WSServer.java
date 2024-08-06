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


    public WSServer(UserService userService, GameService gameService) {
        this.connections = new ConnectionManager();
        this.userService = userService;
        this.gameService = gameService;
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
            NotificationMessage notificationMessage = getConnectionDescriptionMessage(game, username);

            connections.broadcast(username, notificationMessage, game.gameID());
        }
    }

    private static NotificationMessage getConnectionDescriptionMessage(GameData game, String username) {
        String gameRole;
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            gameRole = "playing white.";
        } else if (game.blackUsername() != null && game.blackUsername().equals(username)) {
            gameRole = "playing black.";
        } else {
            gameRole = "as an observer.";
        }
        String notification = String.format("%s has joined the game %s", username, gameRole);
        return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);
    }

    private void makeMove(Session session, String message) throws IOException, DataAccessException {
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);

        GameData gameData = getGameFromDatabaseHandlingExceptions(session, command);
        String username = getUsernameFromDatabaseHandlingExceptions(session, command);

        ChessGame.TeamColor thisPlayerColor = getPlayerColor(username, gameData);
        try {
            ensureActuallyPlayingGame(session, username, gameData, thisPlayerColor);
        } catch (IOException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
            return;
        }

//        Server verifies the validity of the move.
//        Game is updated to represent the move in the database.
        try {
            if (gameData.game().getBoard().getPiece(command.getMove().getStartPosition()).getTeamColor() != thisPlayerColor) {
                throw new InvalidMoveException("Move out of turn.");
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
        ChessGame.TeamColor otherPlayerColor = getPlayerColor(username, gameData) == ChessGame.TeamColor.WHITE ?
                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
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
        GameData gameData = getGameFromDatabaseHandlingExceptions(session, command);
        String username = getUsernameFromDatabaseHandlingExceptions(session, command);

        // remove user from game locally and in database if user is player
        ChessGame.TeamColor currUserColor = getPlayerColor(username, gameData);
        GameData updatedGame = null;
        if (currUserColor == ChessGame.TeamColor.WHITE) {
            updatedGame = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (currUserColor == ChessGame.TeamColor.BLACK) {
            updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }
        if (updatedGame != null) {
            gameService.update(updatedGame);
        }

        // notify all users
        String notificationDescription = String.format("%s has left the game.", username);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationDescription);
        connections.broadcast(username, notificationMessage, gameData.gameID());
        connections.remove(username);
    }

    private void resign(Session session, UserGameCommand command) throws IOException, DataAccessException {
        GameData gameData = getGameFromDatabaseHandlingExceptions(session, command);
        String username = getUsernameFromDatabaseHandlingExceptions(session, command);

        try {
            ensureActuallyPlayingGame(session, username, gameData, getPlayerColor(username, gameData));
        } catch (IOException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
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

        // end game, update in database, notify all users
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
        ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                String.format("Error: no game found with gameID %d", command.getGameID()));
        session.getRemote().sendString(new Gson().toJson(errorMessage));
        return null;
    }

    private ChessGame.TeamColor getPlayerColor(String myUsername, GameData gameData) {
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
        }
        throw new IOException("App has attempted to retrieve opponent for observer.");
    }

    private void ensureActuallyPlayingGame(Session session, String username, GameData gameData,
                                           ChessGame.TeamColor thisPlayerColor) throws IOException {
        if (thisPlayerColor == null) {
            String errorDescription = String.format("%s is not one of the game players.", username);
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", errorDescription));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        }
    }

    private GameData getGameFromDatabaseHandlingExceptions(Session session, UserGameCommand command) {
        // retrieve game from database
        GameData gameData = null;
        try {
            gameData = retrieveGameFromDatabase(session, command);
            if (gameData == null) {
                throw new DataAccessException("game not found");
            }
        } catch (DataAccessException | IOException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            try {
                session.getRemote().sendString(new Gson().toJson(errorMessage));
            } catch (IOException subEx) {
                System.out.printf("Can't even send an error message. Error: %s.%n", subEx.getMessage());
            }
        }
        return gameData;
    }

    private String getUsernameFromDatabaseHandlingExceptions(Session session, UserGameCommand command) {
        // retrieve username from database
        String username = null;
        try {
            username = getUsernameFromAuth(command.getAuthToken());
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            try {
                session.getRemote().sendString(new Gson().toJson(errorMessage));
            } catch (IOException subEx) {
                System.out.printf("Can't even send an error message. Error: %s.%n", subEx.getMessage());
            }
        }
        return username;
    }
}
