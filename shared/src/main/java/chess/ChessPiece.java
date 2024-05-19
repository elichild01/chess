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
        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();
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
                        ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir, myPosition.getColumn()+hdir);
                        boolean stopped = false;
                        while (!stopped) {
                            // Checks if we have run off the board
                            if (currPosition.isOffBoard()) { break; }
                            // Checks if we are sitting on a piece (friendly or unfriendly)
                            ChessPiece localInhabitant = board.getPiece(currPosition);
                            if (localInhabitant != null) {
                                if (localInhabitant.getTeamColor() == myColor) { break; }
                                else { stopped = true; }
                            }
                            // Adds the current position to list
                            ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                            validMoves.add(newMove);
                            // Increments the position being checked
                            currPosition = new ChessPosition(currPosition.getRow()+vdir, currPosition.getColumn()+hdir);
                        }
                    }
                }
                break;
            case KNIGHT:
                throw new RuntimeException("Not implemented");
        }
        return validMoves;
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

    @Override
    public String toString() {
        return String.format("%s %s", this.color, this.pType);
    }
}
