package com.dama.model;

public class Piece {
    private final Color color;
    private Position position;
    private boolean isKing;

    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
        this.isKing = false;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setPosition(int x, int y) {
        this.position.setX(x);
        this.position.setY(y);
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing() {
        this.isKing = true;
    }

}
