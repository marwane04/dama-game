package com.dama.controller.players;

import com.dama.model.Color;

public abstract class Player {

    private final Color color;

    protected Player(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // Hook for human, AI, or remote players to provide a move.
    public abstract void requestMove();
}
