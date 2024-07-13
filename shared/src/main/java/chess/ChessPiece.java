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
    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType pType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.pType = type;
    }

    public ChessPiece(ChessPiece oldPiece) {
        this.color = oldPiece.getTeamColor();
        this.pType = oldPiece.getPieceType();
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
        return switch (pType) {
            case KING -> calcKingMoves(board, myPosition);
            case QUEEN, ROOK, BISHOP -> calcQueenBishopRookMoves(board, myPosition);
            case PAWN -> calcPawnMoves(board, myPosition);
            case KNIGHT -> calcKnightMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> calcKingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // Loop through the eight directions Kings can travel
        for (int hdir = -1; hdir <= 1; hdir++) {
            for (int vdir = -1; vdir <= 1; vdir++) {
                // Skips the not-moving case
                if (vdir == 0 && hdir == 0) { continue; }
                ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir, myPosition.getColumn()+hdir);
                // Checks if we have run off the board
                if (currPosition.isOffBoard()) { continue; }
                // Checks if we are sitting on a piece (friendly or unfriendly)
                ChessPiece localInhabitant = board.getPiece(currPosition);
                if (localInhabitant != null && localInhabitant.getTeamColor() == color) { continue; }
                // Adds the current position to list
                ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                validMoves.add(newMove);
            }
        }
        // Add castling moves
        if (myPosition.getColumn() == 5) {
            int colorModifier = color == ChessGame.TeamColor.WHITE ? 1 : -1;
            // If the king's row isn't what it should be to castle, break
            int kingRow = myPosition.getRow();
            if (kingRow + 3.5*colorModifier != 4.5) { return validMoves; }
            if (!board.getHasLostCastle(color, true)
                    && board.getPiece(new ChessPosition(kingRow, 4))==null
                    && board.getPiece(new ChessPosition(kingRow, 3))==null
                    && board.getPiece(new ChessPosition(kingRow, 2))==null) {
                ChessPiece castlingRook = board.getPiece(new ChessPosition(kingRow, 1));
                if (castlingRook != null
                        && castlingRook.getPieceType() == PieceType.ROOK
                        && castlingRook.getTeamColor() == color) {
                    ChessPosition castlePosition = new ChessPosition(kingRow, 3);
                    ChessMove newMove = new ChessMove(myPosition, castlePosition, null);
                    validMoves.add(newMove);
                }
            }
            if (!board.getHasLostCastle(color, false)
                    && board.getPiece(new ChessPosition(kingRow, 6))==null
                    && board.getPiece(new ChessPosition(kingRow, 7))==null) {
                ChessPiece castlingRook = board.getPiece(new ChessPosition(kingRow, 8));
                if (castlingRook != null
                        && castlingRook.getPieceType() == PieceType.ROOK
                        && castlingRook.getTeamColor() == color) {
                    ChessPosition castlePosition = new ChessPosition(kingRow, 7);
                    ChessMove newMove = new ChessMove(myPosition, castlePosition, null);
                    validMoves.add(newMove);
                }
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> calcQueenBishopRookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // Loop through the eight directions Queens, Rooks, and/or Bishops can travel
        for (int hdir = -1; hdir <= 1; hdir+=1) {
            for (int vdir = -1; vdir <= 1; vdir+=1) {
                if (vdir == 0 && hdir == 0) { continue; }
                if (vdir != 0 && hdir != 0 && pType == PieceType.ROOK) { continue; }
                if ((vdir == 0 || hdir == 0) && pType == PieceType.BISHOP) { continue; }

                // Go as far as we can go in current direction
                ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir, myPosition.getColumn()+hdir);
                boolean stopped = false;
                while (!stopped) {
                    if (currPosition.isOffBoard()) { break; }
                    ChessPiece localInhabitant = board.getPiece(currPosition);
                    if (localInhabitant != null) {
                        if (localInhabitant.getTeamColor() == color) { break; }
                        else { stopped = true; }
                    }
                    // Adds the current position to list
                    ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                    validMoves.add(newMove);
                    currPosition = new ChessPosition(currPosition.getRow()+vdir, currPosition.getColumn()+hdir);
                }
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> calcKnightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // Loop through the eight directions Knights can travel
        for (int hdir = -2; hdir <= 2; hdir++) {
            // Skips the not-moving case
            if (hdir==0) { continue; }
            for (int vdir = -3+Math.abs(hdir); vdir <= 3-Math.abs(hdir); vdir+=2*(3-Math.abs(hdir))) {
                ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir, myPosition.getColumn()+hdir);
                // Checks if we have run off the board
                if (currPosition.isOffBoard()) { continue; }
                // Checks if we are sitting on a piece (friendly or unfriendly)
                ChessPiece localInhabitant = board.getPiece(currPosition);
                if (localInhabitant != null && localInhabitant.getTeamColor() == color) { continue; }
                // Adds the current position to list
                ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                validMoves.add(newMove);
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> calcPawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // Pieces pawns can be promoted to
        PieceType[] promoPieces = {PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP};
        // Loops through the ways pawns can travel without capturing
        int colorModifier = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        for (int vdir = 1; vdir <= 2; vdir++) {
            ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir*colorModifier, myPosition.getColumn());
            // If attempting to move two spaces, checks if legal
            if (vdir == 2) {
                if (currPosition.getRow()+.5*colorModifier != 4.5) { continue; }
                ChessPosition jumpedPosition = new ChessPosition(currPosition.getRow()-colorModifier, currPosition.getColumn());
                ChessPiece jumpedInhabitant = board.getPiece(jumpedPosition);
                if (jumpedInhabitant != null) { continue; }
            }
            // Checks if we have run off the board
            if (currPosition.isOffBoard()) { continue; }
            // Checks if we are sitting on a piece (friendly or unfriendly)
            ChessPiece localInhabitant = board.getPiece(currPosition);
            if (localInhabitant != null) { continue; }
            // Adds the current position to list, with all possible promotions
            if (currPosition.getRow()-3.5*colorModifier==4.5) {
                for (PieceType promoPiece : promoPieces) {
                    ChessMove newMove = new ChessMove(myPosition, currPosition, promoPiece);
                    validMoves.add(newMove);
                }
            } else {
                ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                validMoves.add(newMove);
            }
        }
        // Loops through the ways pawns can capture
        for (int hdir = -1; hdir <= 1; hdir+=2) {
            ChessPosition currPosition = new ChessPosition(myPosition.getRow()+colorModifier, myPosition.getColumn()+hdir);
            // Checks if we can en passant!
            if (currPosition.equals(board.getEnPassantVulnerability())) {
                ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                validMoves.add(newMove);
            }
            // Checks if we have run off the board
            if (currPosition.isOffBoard()) { continue; }
            // Checks if we are sitting on a piece (friendly or unfriendly)
            ChessPiece localInhabitant = board.getPiece(currPosition);
            if (localInhabitant == null) { continue; }
            if (localInhabitant.getTeamColor() == color) { continue; }
            // Adds the current position to list, with all possible promotions
            if (currPosition.getRow()-3.5*colorModifier == 4.5) {
                for (PieceType promoPiece : promoPieces) {
                    ChessMove newMove = new ChessMove(myPosition, currPosition, promoPiece);
                    validMoves.add(newMove);
                }
            } else {
                ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                validMoves.add(newMove);
            }
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
