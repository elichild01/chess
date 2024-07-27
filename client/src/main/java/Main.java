import chess.*;
import requestresult.LoginResult;
import requestresult.RegisterResult;
import server.Server;
import serverfacade.ServerFacade;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static boolean finished;
    private static AppState state = AppState.PRELOGIN;
    private static ServerFacade facade;
    private static Scanner scanner;
    private static String authToken;

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
        }
    }

    private static void handleLogout() {

    }

    private static void handleCreate() {

    }

    private static void handleList() {

    }

    private static void handlePlay() {

    }

    private static void handleObserve() {

    }

    private static void handleUnrecognizedOption() {
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
}