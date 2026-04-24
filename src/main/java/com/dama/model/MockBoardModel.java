package com.dama.model;

import java.beans.PropertyChangeListener;

public class MockBoardModel implements BoardModel {

    private final PieceType[][] board = new PieceType[8][8];

    public MockBoardModel() {
        // Initialize empty board
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board[r][c] = PieceType.EMPTY;

        // Place black pieces (top)
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 8; c++)
                if ((r + c) % 2 != 0)
                    board[r][c] = PieceType.BLACK;

        // Place red pieces (bottom)
        for (int r = 5; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if ((r + c) % 2 != 0)
                    board[r][c] = PieceType.RED;
    }

    @Override public PieceType getPieceAt(int row, int col) { return board[row][col]; }
    @Override public String getCurrentPlayer() { return "RED"; }
    @Override public boolean isGameOver() { return false; }
    @Override public String getWinner() { return null; }
    @Override public int[][] getValidMoves(int row, int col) { return new int[0][]; }
    @Override public void addPropertyChangeListener(PropertyChangeListener l) {}
    @Override public void removePropertyChangeListener(PropertyChangeListener l) {}
}