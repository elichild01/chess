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
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessGame.TeamColor myColor = this.getTeamColor();
        switch (this.getPieceType()) {
            case KING:
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
                        if (localInhabitant != null && localInhabitant.getTeamColor() == myColor) { continue; }
                        // Adds the current position to list
                        ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                        validMoves.add(newMove);
                    }
                }
                // Add castling moves
                if (myPosition.getRow() == 5) {
                    if (!board.getHasLostCastle(myColor, true)) {
                        ChessPosition castlePosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 2);
                        // Checks if we have run off the board
                        if (castlePosition.isOffBoard()) {
                            break;
                        }
                        ChessMove newMove = new ChessMove(myPosition, castlePosition, null);
                        validMoves.add(newMove);
                    }
                    if (!board.getHasLostCastle(myColor, false)) {
                        ChessPosition castlePosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 2);
                        // Checks if we have run off the board
                        if (castlePosition.isOffBoard()) {
                            break;
                        }
                        ChessMove newMove = new ChessMove(myPosition, castlePosition, null);
                        validMoves.add(newMove);
                    }
                }
                break;
            case QUEEN:
                // Loop through the eight directions Queens can travel
                for (int hdir = -1; hdir <= 1; hdir+=1) {
                    for (int vdir = -1; vdir <= 1; vdir+=1) {
                        // Skips the not-moving case
                        if (vdir == 0 && hdir == 0) { continue; }
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
            case PAWN:
                // Pieces pawns can be promoted to
                PieceType[] promoPieces = {PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP};
                // Loops through the ways pawns can travel without capturing
                int colorModifier = myColor==ChessGame.TeamColor.WHITE ? 1 : -1;
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
                // FIXME: Does not yet account for en passant!
                for (int hdir = -1; hdir <= 1; hdir+=2) {
                    ChessPosition currPosition = new ChessPosition(myPosition.getRow()+colorModifier, myPosition.getColumn()+hdir);
                    // Checks if we have run off the board
                    if (currPosition.isOffBoard()) { continue; }
                    // Checks if we are sitting on a piece (friendly or unfriendly)
                    ChessPiece localInhabitant = board.getPiece(currPosition);
                    if (localInhabitant == null) { continue; }
                    if (localInhabitant.getTeamColor() == myColor) { continue; }
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
                break;
            case ROOK:
                // Horizontal movement
                for (int hdir = -1; hdir <= 1; hdir+=2) {
                    ChessPosition currPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn()+hdir);
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
                        currPosition = new ChessPosition(currPosition.getRow(), currPosition.getColumn()+hdir);
                    }
                }
                // Vertical movement
                for (int vdir = -1; vdir <= 1; vdir+=2) {
                    ChessPosition currPosition = new ChessPosition(myPosition.getRow()+vdir, myPosition.getColumn());
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
                        currPosition = new ChessPosition(currPosition.getRow()+vdir, currPosition.getColumn());
                    }
                }
                break;
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
                        if (localInhabitant != null && localInhabitant.getTeamColor() == myColor) { continue; }
                        // Adds the current position to list
                        ChessMove newMove = new ChessMove(myPosition, currPosition, null);
                        validMoves.add(newMove);
                    }
                }
                break;
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
