package websocketclient;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;


public class WSClient extends Endpoint {
    private final Session session;

    public WSClient() throws Exception {
        URI uri = new URI("ws://localhost:8080/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws IOException {
        send(UserGameCommand.CommandType.CONNECT, authToken, gameID, null);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        send(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
    }

    public void leave(String authToken, int gameID) throws IOException {
        send(UserGameCommand.CommandType.LEAVE, authToken, gameID, null);
    }

    public void resign(String authToken, int gameID) throws IOException {
        send(UserGameCommand.CommandType.RESIGN, authToken, gameID, null);
    }

    private void send(UserGameCommand.CommandType commandType, String authToken, int gameID, ChessMove move) throws IOException {
        UserGameCommand command;
        if (move == null) {
            command = new UserGameCommand(commandType, authToken, gameID);
        } else {
            command = new MakeMoveCommand(commandType, authToken, gameID, move);
        }

        session.getBasicRemote().sendText(new Gson().toJson(command));
    }

}
