import chess.*;
import model.GameData;
import requestresult.ListResult;
import serverfacade.ServerFacade;
import websocketclient.WSClient;

import java.io.IOException;
import java.util.*;

import static websocketclient.WSClient.drawBoard;

public class Main {
    private static boolean finished;
    private static AppState state = AppState.NOT_LOGGED_IN;
    private static ServerFacade httpServer;
    private static Scanner scanner;
    private static String authToken;
    private static HashMap<Integer, GameData> currGameList;
    private static ChessGame.TeamColor currColor;
    private static WSClient wsClient;


    public static void main(String[] args) throws Exception {
        int port = 8081;
        httpServer = new ServerFacade(port);
        wsClient = new WSClient();

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println("Welcome to Eli's 240 Chess. Type 'help' to get started.");
        scanner = new Scanner(System.in);

        finished = false;
        while (!finished) {
            String userOption = scanner.nextLine();

            if (Arrays.asList(getUserOptions(state)).contains(userOption)) {
                switch (userOption) {
                    case "help" -> handleHelp();
                    case "quit" -> handleQuit();
                    case "login" -> handleLogin();
                    case "register" -> handleRegister();
                    case "logout" -> handleLogout();
                    case "create" -> handleCreate();
                    case "list" -> handleList();
                    case "play" -> handlePlay();
                    case "observe" -> handleObserve();
                    case "redraw" -> handleRedraw();
                    case "leave" -> handleLeave();
                    case "move" -> handleMove();
                    case "resign" -> handleResign();
                    case "highlight" -> handleHighlight();
                    default -> handleUnrecognizedOption();
                }
            }
//            if (state == AppState.IN_GAMEPLAY) {
//                drawBoard(wsClient.getCurrGame().game(), currColor == ChessGame.TeamColor.WHITE, highlightSquare);
//                highlightSquare = null;
//            }
        }
    }

    private static void handleHelp() {
        String preLoginHelp = """
                register - register new user
                login - log in existing user
                quit - exit chess app
                help - display this help text
                """;

        String postLoginHelp = """
                logout - log out current user
                create - create new game
                list - list all current games
                play - play chess
                observe - observe chess game without joining
                help - display this help text
                """;

        String gamePlayHelp = """
                redraw - redraw chess board
                leave - leave current game
                move - make move in chess game
                resign - resign from current game
                highlight - highlight legal moves for given piece
                help - display this help text
                """;

        switch (state) {
            case NOT_LOGGED_IN -> System.out.print(preLoginHelp);
            case LOGGED_IN -> System.out.print(postLoginHelp);
            case IN_GAMEPLAY -> System.out.print(gamePlayHelp);
        }
    }

    private static void handleQuit() {
        System.out.println("Thanks for playing!");
        finished = true;
    }

    private static void handleLogin() throws IOException {
        // get info from user
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // perform login
        Map<String, Object> response = httpServer.login(username, password);
        if (!response.containsKey("message")) {
            authToken = (String) response.get("authToken");
            state = AppState.LOGGED_IN;
            System.out.printf("Successfully logged in user %s.%n", response.get("username"));
        } else {
            System.out.println(response.get("message"));
        }
        handleHelp();
    }

    private static void handleRegister() throws IOException {
        // get info from user
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        // perform register
        Map<String, Object> response = httpServer.register(username, password, email);
        if (!response.containsKey("message")) {
            authToken = (String) response.get("authToken");
            state = AppState.LOGGED_IN;
            System.out.printf("Successfully logged in user %s.%n", response.get("username"));
        } else {
            System.out.println(response.get("message"));
        }
        handleHelp();
    }

    private static void handleLogout() throws IOException {
        Map<String, Object> response = httpServer.logout(authToken);
        if (!response.containsKey("message")) {
            state = AppState.NOT_LOGGED_IN;
            System.out.println("Successfully logged out.");
        } else {
            System.out.println(response.get("message"));
        }
        handleHelp();
    }

    private static void handleList() throws IOException {
        ListResult list = httpServer.list(authToken);

        if (list.games().isEmpty()) {
            System.out.println("There are currently no games to display.");
        } else {
            currGameList = new HashMap<>();
            int i = 0;
            for (GameData game : list.games()) {
                currGameList.put(i, game);
                System.out.printf("%d: %s, whiteUsername: %s, blackUsername: %s%n", i++, game.gameName(), game.whiteUsername(), game.blackUsername());
            }
        }
    }

    private static void handleCreate() throws IOException {
        // get game name from user
        System.out.println("Game name: ");
        String gameName = scanner.nextLine();

        // create game
        Map<String, Object> response = httpServer.create(authToken, gameName);
        if (!response.containsKey("message")) {
            System.out.printf("Successfully created game %s.%n", gameName);
        } else {
            System.out.println(response.get("message"));
        }
    }

    private static void handlePlay() throws IOException {
        if (currGameList == null || currGameList.isEmpty()) {
            System.out.println("You cannot choose a game to play until you have seen the list of current games. Please enter 'list' first.");
            handleHelp();
            return;
        }

        // get and parse info from user
        System.out.println("Enter number of the game you would like to join (from most recently-displayed list): ");
        int gameNum = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter color you would like to play as: ");
        String colorStr = scanner.nextLine();
        while (!colorStr.equalsIgnoreCase("WHITE") && !colorStr.equalsIgnoreCase("BLACK")) {
            System.out.println("Color not recognized. Please enter 'WHITE' or 'BLACK'.");
            colorStr = scanner.nextLine();
        }
        ChessGame.TeamColor playerColor = colorStr.equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

        GameData selectedGame = currGameList.get(gameNum);
        if (selectedGame == null) {
            System.out.println("Selection invalid.");
            handleHelp();
            return;
        }

        // join game
        Map<String, Object> response = httpServer.join(authToken, playerColor, selectedGame.gameID());
        if (!response.containsKey("message")) {
            // open WebSocket connection
            try {
                wsClient = new WSClient();
                wsClient.setCurrGame(selectedGame);
            } catch (Exception ex) {
                System.out.printf("Error: %s%n", ex.getMessage());
                return;
            }

            currColor = playerColor;
            System.out.printf("Successfully joined game %s.%n", wsClient.getCurrGame().gameName());

            // send a CONNECT WebSocket message
            wsClient.connect(authToken, wsClient.getCurrGame().gameID(), currColor == ChessGame.TeamColor.WHITE);

            // transition to gameplay UI
            state = AppState.IN_GAMEPLAY;
        } else {
            System.out.println(response.get("message"));
            handleHelp();
        }
    }

    private static void handleObserve() {
        if (currGameList == null || currGameList.isEmpty()) {
            System.out.println("You cannot choose a game to observe until you have seen the list of current games. Please enter 'list' first.");
            handleHelp();
            return;
        }

        // get and parse info from user
        System.out.print("Enter number of the game you would like to observe (from most recently-displayed list): ");
        int gameNum = scanner.nextInt();
        scanner.nextLine();
        currColor = null;

        GameData selectedGame = currGameList.get(gameNum);
        if (selectedGame == null) {
            System.out.println("Selection invalid.");
            handleHelp();
            return;
        }

        // observe game
        // open WebSocket connection
        try {
            wsClient = new WSClient();
            wsClient.setCurrGame(selectedGame);
        } catch (Exception ex) {
            System.out.printf("Error: %s%n", ex.getMessage());
            return;
        }

        System.out.printf("Successfully joined game %s as an observer.%n", wsClient.getCurrGame().gameName());

        try {
            // send a CONNECT WebSocket message
            wsClient.connect(authToken, wsClient.getCurrGame().gameID(), true);
        } catch (IOException ex) {
            System.out.println("Error. Could not connect to server.");
        }

        // transition to gameplay UI
        state = AppState.IN_GAMEPLAY;
    }

    private static void handleRedraw() {
        drawBoard(wsClient.getCurrGame().game(), currColor == ChessGame.TeamColor.WHITE, null);
    }

    private static void handleLeave() {
        try {
            wsClient.leave(authToken, wsClient.getCurrGame().gameID());
        } catch (IOException err) {
            System.out.printf("Could not leave game %s. %s%n", wsClient.getCurrGame().gameName(), err.getMessage());
            return;
        }

        // leave game
        state = AppState.LOGGED_IN;
        System.out.printf("Successfully left game %s.%n", wsClient.getCurrGame().gameName());
        wsClient.setCurrGame(null);
        currColor = null;
        handleHelp();
    }

    private static void handleMove() {
        // position moving from
        System.out.print("Please enter the square you would like to move FROM (letter and number, no space, e.g. A1): ");
        String fromSquare = scanner.nextLine();
        ChessPosition fromPosition = parseSquareInfo(fromSquare);
        if (fromPosition == null) {
            System.out.println("Invalid square selection.");
            handleHelp();
            return;
        }
        ChessPiece pieceToMove = wsClient.getCurrGame().game().getBoard().getPiece(fromPosition);
        if (pieceToMove == null) {
            System.out.println("Cannot move piece from empty square.");
            handleHelp();
            return;
        }
        if (pieceToMove.getTeamColor() != currColor) {
            System.out.println("You cannot move pieces that do not belong to you.");
            handleHelp();
            return;
        }

        // position moving to
        System.out.print("Please enter the square you would like to move TO (letter and number, no space, e.g. B3): ");
        String toSquare = scanner.nextLine();
        ChessPosition toPosition = parseSquareInfo(toSquare);
        if (toPosition == null) {
            System.out.println("Invalid square selection.");
            handleHelp();
            return;
        }

        // pawn promotion
        ChessPiece.PieceType promoPieceType = null;
        if (pieceToMove.getPieceType() == ChessPiece.PieceType.PAWN
                && (toPosition.getRow() == 1 || toPosition.getRow() == 8)) {
            System.out.print("Pawn promo attempt! Please enter the piece type you would like to promote to: ");
            String promotionPiece = scanner.nextLine();
            promoPieceType = parsePawnPromoInfo(promotionPiece);
        }

        // make the move
        ChessMove move = new ChessMove(fromPosition, toPosition, promoPieceType);
        try {
            wsClient.makeMove(authToken, wsClient.getCurrGame().gameID(), move);
        } catch (IOException err) {
            System.out.printf("Could not make move %s. %s%n", move, err.getMessage());
        }
    }

    private static void handleResign() {
        try {
            wsClient.resign(authToken, wsClient.getCurrGame().gameID());
        } catch (IOException err) {
            System.out.printf("Could not resign from game %s. %s%n", wsClient.getCurrGame().gameName(), err.getMessage());
        }
    }

    private static void handleHighlight() {
        // get info from user
        System.out.print("Please enter the square of the piece whose moves you'd like to highlight (letter and number, no space, e.g. A1): ");
        String fromSquare = scanner.nextLine();

        // highlight the square, checking for errors
        ChessPosition highlightSquare = parseSquareInfo(fromSquare);
        if (highlightSquare == null) {
            System.out.println("Invalid square selection.");
            return;
        }
        ChessPiece highlightedPiece = wsClient.getCurrGame().game().getBoard().getPiece(highlightSquare);
        if (highlightedPiece == null
                || highlightedPiece.getTeamColor() != wsClient.getCurrGame().game().getTeamTurn()) {
            System.out.println("Selected square does not contain a piece of the side whose turn it is.");
            highlightSquare = null;
        }

        drawBoard(wsClient.getCurrGame().game(), currColor == ChessGame.TeamColor.WHITE, highlightSquare);
    }


    private static void handleUnrecognizedOption() {
        System.out.println("Option not recognized. Type 'help' to get started.");
    }

    private enum AppState {
        NOT_LOGGED_IN,
        LOGGED_IN,
        IN_GAMEPLAY
    }

    private static String[] getUserOptions(AppState state) {
        return switch (state) {
            case NOT_LOGGED_IN -> new String[]{"help", "quit", "login", "register"};
            case LOGGED_IN -> new String[]{"help", "logout", "create", "list", "play", "observe"};
            case IN_GAMEPLAY -> new String[]{"help", "redraw", "leave", "move", "resign", "highlight"};
        };
    }

    private static ChessPosition parseSquareInfo(String squareInfo) {
        if (squareInfo.length() != 2) {
            return null;
        }

        String[] colLetters = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] rowNumbers = {"1", "2", "3", "4", "5", "6", "7", "8"};

        String colStr = squareInfo.substring(0, 1).toLowerCase();
        String rowStr = squareInfo.substring(1);

        if (!Arrays.asList(colLetters).contains(colStr)) { return null; }
        if (!Arrays.asList(rowNumbers).contains(rowStr)) { return null; }

        int col = 0;
        for (int i = 0; i <= 7; i++) {
            if (colLetters[i].equals(colStr)) {
                col = i + 1;
            }
        }
        int row = Integer.parseInt(rowStr);

        return new ChessPosition(row, col);
    }

    private static ChessPiece.PieceType parsePawnPromoInfo(String promotionInfo) {
        return switch (promotionInfo.toLowerCase()) {
//            case "na" -> null;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}