import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        AppState state = AppState.PRELOGIN;

        System.out.println("Welcome to Eli's 240 Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        String userSelection = scanner.nextLine();

        while (!getUserOptions(state).contains(userSelection.getFirstWord())) {
            System.out.println("Option not recognized. Type 'help' to get started.");
        }


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