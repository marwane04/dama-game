package com.dama.view;

public interface CheckersView {

    // Refresh the entire board display
    void refreshBoard();

    // Highlight valid move target squares for a selected piece
    void highlightSquares(int[][] squares);

    // Highlight the currently selected piece's square
    void setSelectedPiece(int row, int col);

    // Clear all highlights and selection
    void clearHighlights();

    // Update the status message (e.g. "Red's turn")
    void setStatusMessage(String message);

    // Show a dialog when game ends
    void showGameOverMessage(String winner);

    // Launch and show the window
    void show();
}