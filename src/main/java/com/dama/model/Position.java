package com.dama.model;

public class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static boolean isPlayable(int x, int y) {
        return (x + y) % 2 == 0 && x >= 0 && x <= 7 && y >= 0 && y <= 7;

    }

    public boolean isPlayable() {
        return isPlayable(this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

}
