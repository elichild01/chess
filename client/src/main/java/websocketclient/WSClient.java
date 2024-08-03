package websocketclient;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static ui.EscapeSequences.*;


public class WSClient extends Endpoint {
    private final Session session;
    public static GameData currGame;

    public WSClient() throws Exception {
        URI uri = new URI("ws://localhost:8081/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage serverMessage;
                try {
                    serverMessage = new Gson().fromJson(message, ServerMessage.class);
                } catch (Exception ex) {
                    System.out.printf("Error in onMessage: %s%n", ex.getMessage());
                    return;
                }

                switch (serverMessage.getServerMessageType()) {
                    case NOTIFICATION -> handleNotification(message);
                    case LOAD_GAME -> handleLoadGame(message);
                    case ERROR -> handleError(message);
                }
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

    private void handleNotification(String message) {
        NotificationMessage notificationMessage = new Gson().fromJson(message, NotificationMessage.class);
        System.out.printf("Notification: %s%n", notificationMessage.getMessage());
    }

    private void handleLoadGame(String message) {
        LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
        currGame = loadGameMessage.getGame();
        System.out.printf("Loaded game %s%n.", currGame.gameName());
    }

    private void handleError(String message) {
        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
        System.out.printf("Error: %s%n", errorMessage.getErrorMessage());
    }

    public static void drawBoard(ChessBoard board, boolean flip) {
        String borderBackgroundColor = SET_BG_COLOR_YELLOW;
        String borderTextColor = SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD;

        // sets directions if this is the first printing of the board or the reverse
        int firstRow = 1;
        int lastRow = 8;
        char firstCol = 'h';
        char lastCol = 'a';
        int direction = 1;
        if (flip) {
            firstRow = 8;
            lastRow = 1;
            firstCol = 'a';
            lastCol = 'h';
            direction = -1;
        }

        // top border
        printSquare(borderBackgroundColor, "", EMPTY);
        for (char colChar = firstCol; colChar != lastCol-direction; colChar -= (char) direction) {
            printSquare(borderBackgroundColor, borderTextColor, String.format(" %s ", colChar));
        }
        printSquare(borderBackgroundColor, "", EMPTY);
        System.out.printf("%s\n", RESET_BG_COLOR);

        // board and left/right borders
        for (int row = firstRow; row != lastRow+direction; row += direction) {
            printSquare(borderBackgroundColor, borderTextColor, String.format(" %d ", row));
            for (int col = firstRow; col != lastRow+direction; col += direction) {
                String squareColor = (row + col & 1) == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK;
                ChessPiece piece = board.getPiece(new ChessPosition(row, 9-col));
                String pieceColor = "";
                String pieceString = EMPTY;
                if (piece != null) {
                    pieceColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLUE;
                    pieceString = getStringRepresentingPiece(piece);
                }
                printSquare(squareColor, pieceColor, pieceString);
            }
            printSquare(borderBackgroundColor, borderTextColor, String.format(" %d ", row));
            System.out.printf("%s\n", RESET_BG_COLOR);
        }

        // bottom border
        printSquare(borderBackgroundColor, "", EMPTY);
        for (char colChar = firstCol; colChar != lastCol-direction; colChar -= (char) direction) {
            printSquare(borderBackgroundColor, borderTextColor, String.format(" %s ", colChar));
        }
        printSquare(borderBackgroundColor, "", EMPTY);
        System.out.printf("%s%s%s\n\n", RESET_BG_COLOR, RESET_TEXT_COLOR, RESET_TEXT_BOLD_FAINT);
    }

    private static void printSquare(String backgroundColor, String textColor, String character) {
        System.out.printf("%s%s%s", backgroundColor, textColor, character);
    }

    private static String getStringRepresentingPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return switch (piece.getPieceType()) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case ROOK -> WHITE_ROOK;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            return switch (piece.getPieceType()) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case ROOK -> BLACK_ROOK;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case PAWN -> BLACK_PAWN;
            };
        }
    }

}