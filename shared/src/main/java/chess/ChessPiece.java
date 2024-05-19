package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private ChessPiece.PieceType pType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.pType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && pType == that.pType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, pType);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        switch (this.pType) {
            case KING:
                throw new RuntimeException("Not implemented");
            case QUEEN:
                throw new RuntimeException("Not implemented");
            case PAWN:
                throw new RuntimeException("Not implemented");
            case ROOK:
                throw new RuntimeException("Not implemented");
            case BISHOP:
                // Loop through the four directions Bishops can travel
                for (int hdir = -1; hdir <= 1; hdir+=2) {
                    for (int vdir = -1; vdir <= 1; vdir+=2) {
                        int currRow = myPosition.getRow() + vdir;
                        int currCol = myPosition.getColumn() + hdir;
                        while (currRow <= 8 && currRow >= 1 && currCol <= 8 && currCol >= 1) {
                            // Adds the current position to list if we haven't hit a wall yet
                            ChessPosition newPosition = new ChessPosition(currRow, currCol);
                            ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                            validMoves.add(newMove);

                            // Increments the position being checked
                            currRow += vdir;
                            currCol += hdir;
                        }
                    }
                }
            case KNIGHT:
                throw new RuntimeException("Not implemented");
        }
        return validMoves;
    }
}
