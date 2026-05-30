package com.dama.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] pieces = new Piece[8][8];
    private Color currentPlayer;
    private GameState state;

    private final PropertyChangeSupport support;

    public Board() {
        this.state = GameState.IN_PROGRESS;
        this.currentPlayer = Color.RED;
        support = new PropertyChangeSupport(this);
        initBoard();
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public GameState getState() { return state; }

    public void setState(GameState state) { this.state = state; }

    public void setBlackWinner() { this.state = GameState.BLACK_WINS; }

    public void setRedWinner() { this.state = GameState.RED_WINS; }

    public void setDraw() { this.state = GameState.DRAW; }

    public Color getCurrentPlayer() { return this.currentPlayer; }

    public void setCurrentPlayer(Color color) { this.currentPlayer = color; }

    // ── Initialisation ────────────────────────────────────────────

    private void initBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (Position.isPlayable(i, j))
                    this.pieces[i][j] = new Piece(Color.BLACK, new Position(i, j));
            }
        }
        for (int i = 5; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (Position.isPlayable(i, j))
                    this.pieces[i][j] = new Piece(Color.RED, new Position(i, j));
            }
        }
    }

    // ── Piece access ──────────────────────────────────────────────

    public Piece getPiece(int x, int y) { return this.pieces[x][y]; }

    public Piece getPiece(Position position) { return getPiece(position.getX(), position.getY()); }

    /**
     * Removes a piece from the given cell (used by AI board cloning).
     */
    public void clearPiece(int x, int y) {
        this.pieces[x][y] = null;
    }

    /**
     * Places a piece of the given colour/king-status into the given cell (used by AI board cloning).
     */
    public void placePiece(Color color, int x, int y, boolean king) {
        Piece p = new Piece(color, new Position(x, y));
        if (king) p.setKing();
        this.pieces[x][y] = p;
    }

    // ── Turn management ───────────────────────────────────────────

    public void switchTurn() {
        this.currentPlayer = (this.currentPlayer == Color.BLACK) ? Color.RED : Color.BLACK;
    }

    // ── Reset ─────────────────────────────────────────────────────

    public void reset() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                pieces[i][j] = null;

        this.state = GameState.IN_PROGRESS;
        this.currentPlayer = Color.RED;
        initBoard();
        this.support.firePropertyChange("boardState", null, this.pieces);
    }

    // ── Move logic ────────────────────────────────────────────────

    private boolean isLegalLandingSquare(Piece piece, Position position) {
        return position.isPlayable() && getPiece(position) == null;
    }

    private List<Position> getCaptureMoves(Position position) {
        Piece piece = getPiece(position);
        if (piece == null) return new ArrayList<>();

        List<Position> captures = new ArrayList<>();
        int direction = piece.getColor() == Color.BLACK ? 1 : -1;

        Position[] candidates = piece.isKing()
                ? new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1),
                    new Position(position.getX() - direction, position.getY() + 1),
                    new Position(position.getX() - direction, position.getY() - 1)
                }
                : new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1)
                };

        for (Position candidate : candidates) {
            if (!candidate.isPlayable()) continue;
            Piece jumped = getPiece(candidate);
            if (jumped == null || jumped.getColor() == piece.getColor()) continue;

            int jumpX = position.getX() + (candidate.getX() - position.getX()) * 2;
            int jumpY = position.getY() + (candidate.getY() - position.getY()) * 2;
            Position landing = new Position(jumpX, jumpY);
            if (isLegalLandingSquare(piece, landing)) captures.add(landing);
        }
        return captures;
    }

    public boolean hasCaptureMove(Color color) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = pieces[x][y];
                if (piece == null || piece.getColor() != color) continue;
                if (!getCaptureMoves(new Position(x, y)).isEmpty()) return true;
            }
        }
        return false;
    }

    public void movePiece(Position initialPosition, Position newPosition) {
        if (!getPossibleMovements(initialPosition).contains(newPosition)) return;

        Piece moving = getPiece(initialPosition);
        pieces[newPosition.getX()][newPosition.getY()] = moving;
        pieces[initialPosition.getX()][initialPosition.getY()] = null;

        // Remove captured piece
        int deltaX = newPosition.getX() - initialPosition.getX();
        int deltaY = newPosition.getY() - initialPosition.getY();
        if (Math.abs(deltaX) == 2) {
            int jumpedX = initialPosition.getX() + (deltaX / 2);
            int jumpedY = initialPosition.getY() + (deltaY / 2);
            pieces[jumpedX][jumpedY] = null;
        }

        if (moving != null) {
            moving.setPosition(newPosition.getX(), newPosition.getY());
            // Promote to king
            if (moving.getColor() == Color.BLACK && newPosition.getX() == 7) moving.setKing();
            if (moving.getColor() == Color.RED   && newPosition.getX() == 0) moving.setKing();
        }

        checkWinCondition();
        this.support.firePropertyChange("boardState", null, this.pieces);
        switchTurn();
    }

    private void checkWinCondition() {
        boolean hasRed   = false;
        boolean hasBlack = false;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (pieces[x][y] == null) continue;
                if (pieces[x][y].getColor() == Color.RED)   hasRed   = true;
                if (pieces[x][y].getColor() == Color.BLACK) hasBlack = true;
            }
        }
        if (!hasRed)   { this.state = GameState.BLACK_WINS; return; }
        if (!hasBlack) { this.state = GameState.RED_WINS;   return; }
    }

    public List<Position> getPossibleMovements(Position position) {
        Piece piece = getPiece(position);
        if (!position.isPlayable() || piece == null) return new ArrayList<>();

        List<Position> captures = getCaptureMoves(position);
        if (!captures.isEmpty()) return captures;

        if (hasCaptureMove(piece.getColor())) return new ArrayList<>();

        List<Position> movements = new ArrayList<>();
        int direction = piece.getColor() == Color.BLACK ? 1 : -1;

        Position[] candidates = piece.isKing()
                ? new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1),
                    new Position(position.getX() - direction, position.getY() + 1),
                    new Position(position.getX() - direction, position.getY() - 1)
                }
                : new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1)
                };

        for (Position candidate : candidates) {
            if (isLegalLandingSquare(piece, candidate)) movements.add(candidate);
        }
        return movements;
    }

    // ── Property change support ───────────────────────────────────

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
}