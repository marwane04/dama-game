package com.dama.model;

import java.beans.PropertyChangeListener;

public interface BoardModel {
    // Returns the piece at a given cell
    PieceType getPieceAt(int row, int col);

    // Returns whose turn it is: "RED" or "BLACK"
    String getCurrentPlayer();

    // Returns true if the game is over
    boolean isGameOver();

    // Returns winner string or null if game is ongoing
    String getWinner();

    // Returns list of valid destination squares for a selected piece
    int[][] getValidMoves(int row, int col);

    // Observer pattern — View subscribes to model changes
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}
