package com.dama.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] pieces = new Piece[8][8];
    private GameState state;

    public Board() {
        this.state = GameState.IN_PROGRESS;
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

    public void movePiece(Position initialPosition, Position newPosition) {
        if (getPossibleMouvements(initialPosition).contains(newPosition)) {
            this.pieces[newPosition.getX()][newPosition.getY()] = this.pieces[initialPosition.getX()][initialPosition.getY()];
            this.pieces[initialPosition.getX()][initialPosition.getY()] = null;
        }

    }

    public List<Position> getPossibleMouvements(Position position) {
        Piece piece = this.pieces[position.getX()][position.getY()];

        if (!position.isPlayable() || piece == null) return new ArrayList<>();

        List<Position> mouvements = new ArrayList<>();

        if (Position.isPlayable(position.getX() + 1, position.getY() + 1) &&
                (this.pieces[position.getX() + 1][position.getY() + 1] == null ||
                this.pieces[position.getX() + 1][position.getY() + 1].getColor() != piece.getColor())) {
            mouvements.add(new Position(position.getX() + 1, position.getY() + 1));
        }

        if (Position.isPlayable(position.getX() + 1, position.getY() - 1) &&
                (this.pieces[position.getX() + 1][position.getY() - 1] == null ||
                this.pieces[position.getX() + 1][position.getY() - 1].getColor() != piece.getColor())) {
            mouvements.add(new Position(position.getX() + 1, position.getY() - 1));
        }

        return mouvements;
    }


}
