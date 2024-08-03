package websocketserver;

import com.google.gson.Gson;
//import javax.websocket.*;

import dataaccess.DataAccessException;
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
            case MAKE_MOVE -> makeMove(message);
            case LEAVE -> leave();
            case RESIGN -> resign();
        }
    }

    private void connect(Session session, UserGameCommand command) throws IOException {
        connections.add(command.getUsername(), session, command.getGameID());

        // load the requested game
        Collection<GameData> gameList;
        try {
            gameList = gameService.list(new ListRequest(command.getAuthToken())).games();
        } catch (DataAccessException ex) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("Error: %s", ex.getMessage()));
            session.getRemote().sendString(new Gson().toJson(errorMessage, ErrorMessage.class));
            return;
        }
        GameData game = null;
        for (GameData currGame : gameList) {
            if (currGame.gameID() == command.getGameID()) {
                game = currGame;
            }
        }
        if (game == null) {
            ErrorMessage errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: Game not found.");
            session.getRemote().sendString(new Gson().toJson(errorMessage, ErrorMessage.class));
            return;
        }

        // send LOAD_GAME message
        LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        session.getRemote().sendString(new Gson().toJson(loadGameMessage, LoadGameMessage.class));

        // notify other clients of connection
        String gameRole;
        if (game.whiteUsername().equals(command.getUsername())) {
            gameRole = "playing white.";
        } else if (game.blackUsername().equals(command.getUsername())) {
            gameRole = "playing black.";
        } else {
            gameRole = "as an observer.";
        }
        String notification = String.format("%s has joined the game %s", command.getUsername(), gameRole);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);

        connections.broadcast(command.getUsername(), notificationMessage, game.gameID());
    }

    private void makeMove(String message) {
        var makeMoveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
        // make move
        // send notification
    }

    private void leave() {

    }

    private void resign() {

    }
}
