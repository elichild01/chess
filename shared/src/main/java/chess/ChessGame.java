package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private ChessBoard tempBoard;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) { return null; }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        Collection<ChessMove> legalMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            if (isValidMove(move)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Checks if a given move is valid
     *
     * @param move the move to check if valid
     * @return valid whether the move is valid
     */
    private boolean isValidMove(ChessMove move) {
        tempBoard = new ChessBoard(board);
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = tempBoard.getPiece(startPosition);
        if (piece==null) { return false; }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(tempBoard, startPosition);
        for (ChessMove possMove : possibleMoves) {
            if (possMove.equals(move)) {
                tempBoard.addPiece(move.getEndPosition(), piece);
                tempBoard.addPiece(startPosition, null);
                return !isInCheck(piece.getTeamColor());
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (isValidMove(move)) {
            ChessPiece piece = board.getPiece(move.getStartPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
        } else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check. NOTE: Uses only tempBoard.
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) { return false; }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition enemyPosition = new ChessPosition(row, col);
                ChessPiece enemyPiece = tempBoard.getPiece(enemyPosition);
                if (enemyPiece == null || enemyPiece.getTeamColor() == teamColor) { continue; }
                Collection<ChessMove> threatMoves = enemyPiece.pieceMoves(tempBoard, enemyPosition);
                for (ChessMove threat : threatMoves) {
                    if (threat.getEndPosition().equals(kingPosition)) { return true; }
                }
            }
        }
        return false;
    }

    /**
     * Finds the King of a given side. NOTE: Uses tempBoard.
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPos = new ChessPosition(row, col);
                ChessPiece currPiece = tempBoard.getPiece(currPos);
                if (currPiece == null) { continue; }
                if (currPiece.getTeamColor() == teamColor && currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return currPos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
