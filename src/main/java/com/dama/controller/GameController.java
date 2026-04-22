package com.dama.controller;

public interface GameController {
    // Called when player selects a piece
    void onSquareSelected(int row, int col);

    // Called when player requests a new game
    void onNewGame();

    // Called when player quits
    void onQuit();
}
