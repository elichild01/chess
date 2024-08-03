import chess.*;
import model.GameData;
import requestresult.ListResult;
import serverfacade.ServerFacade;
import websocketclient.WSClient;

import java.io.IOException;
import java.util.*;

import static websocketclient.WSClient.currGame;
import static websocketclient.WSClient.drawBoard;

public class Main {
    private static String username;
    private static boolean finished;
    private static AppState state = AppState.PRELOGIN;
    private static ServerFacade httpServer;
    private static Scanner scanner;
    private static String authToken;
    private static HashMap<Integer, GameData> currGameList;
    private static ChessGame.TeamColor currColor;
    private static WSClient wsClient;


    public static void main(String[] args) throws Exception {
        int port = 8081;
        httpServer = new ServerFacade(port);

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
            if (state == AppState.GAMEPLAY) {
                drawBoard(currGame.game().getBoard(), currColor == ChessGame.TeamColor.BLACK);
            }
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
            case PRELOGIN -> System.out.print(preLoginHelp);
            case POSTLOGIN -> System.out.print(postLoginHelp);
            case GAMEPLAY -> System.out.print(gamePlayHelp);
        }
    }

    private static void handleQuit() {
        System.out.println("Thanks for playing!");
        finished = true;
    }

    private static void handleLogin() throws IOException {
        // get info from user
        System.out.print("Username: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // perform login
        Map<String, Object> response = httpServer.login(enteredUsername, password);
        if (!response.containsKey("message")) {
            authToken = (String) response.get("authToken");
            state = AppState.POSTLOGIN;
            username = enteredUsername;
            System.out.printf("Successfully logged in user %s.%n", response.get("username"));
        } else {
            System.out.println(response.get("message"));
        }
        handleHelp();
    }

    private static void handleRegister() throws IOException {
        // get info from user
        System.out.print("Username: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        // perform register
        Map<String, Object> response = httpServer.register(enteredUsername, password, email);
        if (!response.containsKey("message")) {
            authToken = (String) response.get("authToken");
            state = AppState.POSTLOGIN;
            username = enteredUsername;
            System.out.printf("Successfully logged in user %s.%n", response.get("username"));
        } else {
            System.out.println(response.get("message"));
        }
        handleHelp();
    }

    private static void handleLogout() throws IOException {
        Map<String, Object> response = httpServer.logout(authToken);
        if (!response.containsKey("message")) {
            state = AppState.PRELOGIN;
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
        currGame = currGameList.get(gameNum);

        // join game
        Map<String, Object> response = httpServer.join(authToken, playerColor, currGame.gameID());
        if (!response.containsKey("message")) {
            currColor = playerColor;
            System.out.printf("Successfully joined game %s.%n", currGame.gameName());

            // open WebSocket connection
            try {
                wsClient = new WSClient();
            } catch (Exception ex) {
                System.out.printf("Error: %s%n", ex.getMessage());
                return;
            }

            // send a CONNECT WebSocket message
            wsClient.connect(authToken, currGame.gameID(), username);

            // transition to gameplay UI
            state = AppState.GAMEPLAY;
        } else {
            System.out.println(response.get("message"));
            handleHelp();
        }
    }

    private static void handleObserve() {
        System.out.print("Enter number of the game you would like to observe (from most recently-displayed list): ");
        int gameNum = scanner.nextInt();
        currGame = currGameList.get(gameNum);
        currColor = null;

        // proceed to observe game
        drawStartingBoards();
    }

    private static void handleRedraw() {}

    private static void handleLeave() {
        try {
            wsClient.leave(authToken, currGame.gameID(), username);
        } catch (IOException err) {
            System.out.printf("Could not leave game %s. %s%n", currGame.gameName(), err.getMessage());
            return;
        }

        // Leave game
        currGame = null;
        currColor = null;
        state = AppState.POSTLOGIN;
    }

    private static void handleMove() {
        // get info from user
        System.out.print("Please enter the square you would like to move FROM (letter and number, no space, e.g. A1): ");
        String fromSquare = scanner.nextLine();
        System.out.print("Please enter the square you would like to move TO (letter and number, no space, e.g. B3): ");
        String toSquare = scanner.nextLine();

        // FIXME: figure out pawn promotion!

        ChessPosition fromPosition = parseSquareInfo(fromSquare);
        ChessPosition toPosition = parseSquareInfo(toSquare);

        ChessMove move = new ChessMove(fromPosition, toPosition, null);

        try {
            wsClient.makeMove(authToken, currGame.gameID(), username, move);
        } catch (IOException err) {
            System.out.printf("Could not make move %s. %s%n", move, err.getMessage());
        }
    }

    private static void handleResign() {

    }

    private static void handleHighlight() {

    }


    private static void handleUnrecognizedOption() {
        System.out.println("Option not recognized. Type 'help' to get started.");
    }

    private enum AppState {
        PRELOGIN,
        POSTLOGIN,
        GAMEPLAY
    }

    private static String[] getUserOptions(AppState state) {
        return switch (state) {
            case PRELOGIN -> new String[]{"help", "quit", "login", "register"};
            case POSTLOGIN -> new String[]{"help", "logout", "create", "list", "play", "observe"};
            case GAMEPLAY -> new String[]{"help", "redraw", "leave", "move", "resign", "highlight"};
        };
    }

    private static void drawStartingBoards() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        drawBoards(board);
    }

    private static void drawBoards(ChessBoard board) {
        drawBoard(board, false);
        drawBoard(board, true);
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
}