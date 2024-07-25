import chess.*;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        AppState state = AppState.PRELOGIN;

        System.out.println("Welcome to Eli's 240 Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);

        boolean quit = false;
        while (!quit) {
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
    }

    private static void handleQuit() {
    }

    private static void handleLogin() {
    }

    private static void handleRegister() {
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