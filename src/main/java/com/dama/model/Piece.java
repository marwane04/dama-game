package com.dama.model;

public class Piece {
    private final Couleur couleur;
    private Position position;
    private boolean estDame;

    public Piece(Couleur couleur, Position position) {
        this.couleur = couleur;
        this.position = position;
        this.estDame = false;
    }

    public Couleur getCouleur() {
        return couleur;
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

    public boolean estDame() {
        return estDame;
    }

    public void damer() {
        this.estDame = true;
    }

}
