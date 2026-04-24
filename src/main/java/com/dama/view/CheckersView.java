package com.dama.view;

public interface CheckersView {

    // Refresh the entire board display
    void refreshBoard();

    // Highlight valid move squares for a selected piece
    void highlightSquares(int[][] squares);

    // Clear all highlights
    void clearHighlights();

    // Update the status message (e.g. "Red's turn")
    void setStatusMessage(String message);

    // Show a dialog when game ends
    void showGameOverMessage(String winner);

    // Launch and show the window
    void show();
}