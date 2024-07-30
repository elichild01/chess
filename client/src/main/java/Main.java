import chess.*;
import model.GameData;
import requestresult.ListResult;
import requestresult.LoginResult;
import requestresult.RegisterResult;
import server.Server;
import serverfacade.ServerFacade;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Main {
    private static boolean finished;
    private static AppState state = AppState.PRELOGIN;
    private static ServerFacade facade;
    private static Scanner scanner;
    private static String authToken;
    private static HashMap<Integer, GameData> currGameList;

    public static void main(String[] args) throws Exception {
        // use only for purposes of running locally
        Server server = new Server();
        int port = server.run(0);
        System.out.println("Started Main HTTP server on " + port);
        facade = new ServerFacade(port);

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
                    default -> handleUnrecognizedOption();
                }
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

        switch (state) {
            case PRELOGIN -> System.out.print(preLoginHelp);
            case POSTLOGIN -> System.out.print(postLoginHelp);
        }
    }

    private static void handleQuit() {
        System.out.println("Thanks for playing!");
        finished = true;
    }

    private static void handleLogin() throws IOException {
        System.out.println("Username: ");
        String username = scanner.nextLine();
        System.out.println("Password: ");
        String password = scanner.nextLine();

        LoginResult loginResult = facade.login(username, password);
        if (loginResult.authToken() != null) {
            authToken = loginResult.authToken();
            state = AppState.POSTLOGIN;
        }
    }

    private static void handleRegister() throws IOException {
        System.out.println("Username: ");
        String username = scanner.nextLine();
        System.out.println("Password: ");
        String password = scanner.nextLine();
        System.out.println("Email: ");
        String email = scanner.nextLine();

        RegisterResult registerResult = facade.register(username, password, email);
        if (registerResult.authToken() != null) {
            authToken = registerResult.authToken();
            state = AppState.POSTLOGIN;
            handleHelp();
        }
    }

    private static void handleLogout() throws IOException {
        facade.logout(authToken);
        state = AppState.PRELOGIN;
    }

    private static void handleCreate() throws IOException {
        System.out.println("Game name: ");
        String gameName = scanner.nextLine();

        facade.create(authToken, gameName);
    }

    private static void handleList() throws IOException {
        ListResult listResult = facade.list(authToken);

        currGameList = new HashMap<>();
        int i = 0;
        for (GameData game : listResult.games()) {
            currGameList.put(i, game);
            System.out.printf("%d: %s, ID: %d%n", i++, game.gameName(), game.gameID());
        }
    }

    private static void handlePlay() throws IOException {
        System.out.println("Enter number of the game you would like to join (from most recently-displayed list): ");
        int gameNum = scanner.nextInt();
        System.out.println("Enter color you would like to play as: ");
        String colorStr = scanner.nextLine();

        while (!colorStr.equalsIgnoreCase("WHITE") && !colorStr.equalsIgnoreCase("BLACK")) {
            System.out.println("Color not recognized. Please enter 'WHITE' or 'BLACK'.");
            colorStr = scanner.nextLine();
        }
        ChessGame.TeamColor playerColor = colorStr.equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        GameData game = currGameList.get(gameNum);

        facade.join(authToken, playerColor, game.gameID());

        // proceed to play game
        drawStartingBoard();
    }

    private static void handleObserve() throws IOException {
        System.out.println("Enter number of the game you would like to observe (from most recently-displayed list): ");
        int gameNum = scanner.nextInt();
        GameData game = currGameList.get(gameNum);

        // proceed to observe game
        drawStartingBoard();
    }

    private static void handleUnrecognizedOption() {
        System.out.println("Option not recognized. Type 'help' to get started.");
    }

    private enum AppState {
        PRELOGIN,
        POSTLOGIN
    }

    private static String[] getUserOptions(AppState state) {
        return switch (state) {
            case PRELOGIN -> new String[]{"help", "quit", "login", "register"};
            case POSTLOGIN -> new String[]{"help", "logout", "create", "list", "play", "observe"};
        };
    }

    private static void drawStartingBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        drawBoards(board);
    }

    private static void drawBoards(ChessBoard board) {
        drawBoard(board, false);
        drawBoard(board, true);
    }

    private static void drawBoard(ChessBoard board, boolean flip) {
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
        System.out.printf("%s\n\n", RESET_BG_COLOR);
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