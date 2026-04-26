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
        //always red starts 😊
        this.currentPlayer = Color.RED;

        support = new PropertyChangeSupport(this);

        initBoard();
    }

    public GameState getState() {
        return state;
    }

    public void setBlackWinner() {
        this.state = GameState.BLACK_WINS;
    }

    public void setRedWinner() {
        this.state = GameState.RED_WINS;
    }

    public void setDraw() {
        this.state = GameState.DRAW;
    }

    private void initBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (Position.isPlayable(i, j))
                    this.pieces[i][j] = new Piece(Color.BLACK, new Position(i, j));
            }
        }

        for (int i = 5; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((Position.isPlayable(i, j)))
                    this.pieces[i][j] = new Piece(Color.RED, new Position(i, j));
            }
        }
    }

    public Piece getPiece(int x, int y) {
        return this.pieces[x][y];
    }

    public Piece getPiece(Position position) {
        return getPiece(position.getX(), position.getY());
    }

    public Color getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void switchTurn() {
        if (this.currentPlayer == Color.BLACK) {
            this.currentPlayer = Color.RED;
        } else {
            this.currentPlayer = Color.BLACK;
        }
    }

    public void reset() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pieces[i][j] = null;
            }
        }
        this.state = GameState.IN_PROGRESS;
        this.currentPlayer = Color.RED;
        initBoard();
        this.support.firePropertyChange("boardState", null, this.pieces);
    }

    /**
     * Returns true when the target square is playable and not occupied.
     */
    private boolean isLegalLandingSquare(Piece piece, Position position) {
        return position.isPlayable() && getPiece(position) == null;
    }

    private List<Position> getCaptureMoves(Position position) {
        Piece piece = getPiece(position);
        if (piece == null) return new ArrayList<>();

        List<Position> captures = new ArrayList<>();
        Position[] candidates;

        int direction = piece.getColor() == Color.BLACK ? 1 : -1;
        if (!piece.isKing()) {
            candidates = new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1)
            };
        } else {
            candidates = new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1),
                    new Position(position.getX() - direction, position.getY() + 1),
                    new Position(position.getX() - direction, position.getY() - 1)
            };
        }

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
        if (getPossibleMovements(initialPosition).contains(newPosition)) {
            pieces[newPosition.getX()][newPosition.getY()] = getPiece(initialPosition);
            pieces[initialPosition.getX()][initialPosition.getY()] = null;

            this.support.firePropertyChange("boardState", null, this.pieces);

            switchTurn();
        }
    }

    public List<Position> getPossibleMovements(Position position) {
        Piece piece = getPiece(position);

        if (!position.isPlayable() || piece == null) return new ArrayList<>();

        List<Position> captures = getCaptureMoves(position);
        if (!captures.isEmpty()) return captures;

        if (hasCaptureMove(piece.getColor())) return new ArrayList<>();

        List<Position> movements = new ArrayList<>();
        Position[] candidates;

        int direction = piece.getColor() == Color.BLACK ? 1 : -1;
        if (!piece.isKing()) {
            candidates = new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1)
            };
        } else {
            candidates = new Position[]{
                    new Position(position.getX() + direction, position.getY() + 1),
                    new Position(position.getX() + direction, position.getY() - 1),
                    new Position(position.getX() - direction, position.getY() + 1),
                    new Position(position.getX() - direction, position.getY() - 1)
            };
        }

        for (Position candidate : candidates) {
            if (isLegalLandingSquare(piece, candidate)) movements.add(candidate);
        }

        return movements;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }


}
