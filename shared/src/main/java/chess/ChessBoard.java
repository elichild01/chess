package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] pieces;

    public ChessBoard() {
        this.pieces = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.pieces[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.pieces[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // wipe existing pieces
        this.pieces = new ChessPiece[8][8];
        // add pawns
        int[] pawnRows = {2, 7};
        for (int row : pawnRows) {
            for (int col = 1; col <= 8; col++) {
                ChessGame.TeamColor color = row==2 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = new ChessPiece(color, ChessPiece.PieceType.PAWN);
                addPiece(position, piece);
            }
        }
        // add all other pieces
        for (int row = 1; row <= 8; row += 7) {
            for (int col = 1; col <= 8; col++) {
                ChessGame.TeamColor color = row==1 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece.PieceType type = getPieceType(col, color);
                ChessPiece piece = new ChessPiece(color, type);
                addPiece(position, piece);
            }
        }
    }

    private static ChessPiece.PieceType getPieceType(int col, ChessGame.TeamColor color) {
        return switch (col) {
            case 1, 8 -> ChessPiece.PieceType.ROOK;
            case 2, 7 -> ChessPiece.PieceType.KNIGHT;
            case 3, 6 -> ChessPiece.PieceType.BISHOP;
            case 4 -> color == ChessGame.TeamColor.WHITE ? ChessPiece.PieceType.QUEEN : ChessPiece.PieceType.KING;
            case 5 -> color == ChessGame.TeamColor.BLACK ? ChessPiece.PieceType.QUEEN : ChessPiece.PieceType.KING;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(pieces, that.pieces);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pieces);
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = getPiece(pos);
                String pieceStr;
                if (piece==null) { pieceStr = " "; }
                else {
                    pieceStr = switch (piece.getPieceType()) {
                        case KING -> "k";
                        case QUEEN -> "q";
                        case ROOK -> "r";
                        case BISHOP -> "b";
                        case KNIGHT -> "n";
                        case PAWN -> "p";
                    };
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        pieceStr = pieceStr.toUpperCase();
                    }
                }
                boardString.append(String.format("|%s", pieceStr));
            }
            boardString.append("|\n");
        }
        return boardString.toString();
    }
}
