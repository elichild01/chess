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
        return teamTurn;
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
        ChessBoard origBoard = new ChessBoard(board);
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = board.getPiece(startPosition);
        if (piece==null) { return false; }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        for (ChessMove possMove : possibleMoves) {
            if (possMove.equals(move)) {
                boolean isCastle = piece.getPieceType() == ChessPiece.PieceType.KING;// &&
                board.addPiece(move.getEndPosition(), piece);
                board.addPiece(startPosition, null);
                // TODO: If castling move, move rook
                boolean inCheck = isInCheck(piece.getTeamColor());
                // checks if move is an invalid castling move
                boolean brokeCastlingRules = false;
                if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                    int startCol = startPosition.getColumn();
                    int endCol = move.getEndPosition().getColumn();
                    boolean isCastlingMove = Math.abs(startCol-endCol)==2;
                    if (isCastlingMove) {
                        if (startCol != 5) { brokeCastlingRules = true; }
                        // FIXME make checks in hasLostCastle to be more robust to poor start positions, if necessary
                        ChessPiece jumpedPiece = origBoard.getPiece(new ChessPosition(startPosition.getRow(), (startCol+endCol)/2));
                        if (jumpedPiece != null) { brokeCastlingRules = true; }
                        boolean isQueenSide = endCol == 3;
//                        ChessPiece castlingPartnerRook = isQueenSide ? ;
                    }
                }
                this.board = new ChessBoard(origBoard);
                return !inCheck && !brokeCastlingRules;
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
            if (piece.getTeamColor() != teamTurn) { throw new InvalidMoveException(); }
            if (move.getPromotionPiece() != null) {
                piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            // TODO: If castling move, move rook
            teamTurn = teamTurn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK;
        } else {
            throw new InvalidMoveException();
        }
    }
//    //FIXME add docstring here
//    public boolean isCastlingMove(ChessMove move) {
//        ChessPiece piece = board.getPiece(move.getStartPosition());
//        if (piece.getPieceType() != ChessPiece.PieceType.KING) { return false; }
//        if ()
//    }

    /**
     * Determines if the given team is in check.
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
                ChessPiece enemyPiece = board.getPiece(enemyPosition);
                if (enemyPiece == null || enemyPiece.getTeamColor() == teamColor) { continue; }
                Collection<ChessMove> threatMoves = enemyPiece.pieceMoves(board, enemyPosition);
                for (ChessMove threat : threatMoves) {
                    if (threat.getEndPosition().equals(kingPosition)) { return true; }
                }
            }
        }
        return false;
    }

    /**
     * Finds the King of a given side.
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPos = new ChessPosition(row, col);
                ChessPiece currPiece = board.getPiece(currPos);
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
        if (!isInCheck(teamColor)) { return false; }
        if (teamTurn != teamColor) { return false; }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(currPosition);
                if (piece != null && piece.getTeamColor()==teamColor) {
                    int numPossibleMoves = validMoves(currPosition).size();
                    if (numPossibleMoves > 0) { return false; }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) { return false; }
        if (teamTurn != teamColor) { return false; }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(currPosition);
                if (piece != null && piece.getTeamColor()==teamColor) {
                    int numPossibleMoves = validMoves(currPosition).size();
                    if (numPossibleMoves > 0) { return false; }
                }
            }
        }
        return true;
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
