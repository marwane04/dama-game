package com.dama.controller;

import com.dama.controller.players.Player;
import com.dama.model.Board;
import com.dama.model.Color;
import com.dama.model.GameState;
import com.dama.model.Piece;
import com.dama.model.Position;
import com.dama.view.CheckersView;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    private final Board model;
    private final CheckersView view;
    private final Player redPlayer;
    private final Player blackPlayer;

    private Position selectedPosition;
    private List<Position> selectedMoves;

    public GameController(Board model, CheckersView view, Player redPlayer, Player blackPlayer) {
        this.model = model;
        this.view = view;
        this.redPlayer = redPlayer;
        this.blackPlayer = blackPlayer;
        updateStatusForCurrentPlayer();
    }

    public void onSquareSelected(int row, int col) {
        if (model.getState() != GameState.IN_PROGRESS) {
            return;
        }

        Position clicked = new Position(row, col);
        Piece clickedPiece = model.getPiece(clicked);

        if (selectedPosition == null) {
            if (clickedPiece == null || clickedPiece.getColor() != model.getCurrentPlayer()) {
                view.setStatusMessage("Select a " + model.getCurrentPlayer() + " piece");
                return;
            }
            selectPiece(clicked);
            return;
        }

        if (selectedPosition.equals(clicked)) {
            clearSelection();
            return;
        }

        if (clickedPiece != null && clickedPiece.getColor() == model.getCurrentPlayer()) {
            selectPiece(clicked);
            return;
        }

        if (isValidDestination(clicked)) {
            model.movePiece(selectedPosition, clicked);
            clearSelection();
            updateStatusForCurrentPlayer();
        } else {
            view.setStatusMessage("Invalid move");
        }
    }

    public void onNewGame() {
        model.reset();
        clearSelection();
        updateStatusForCurrentPlayer();
    }

    public void onQuit() {
        Platform.exit();
    }

    private void selectPiece(Position position) {
        selectedPosition = position;
        selectedMoves = model.getPossibleMovements(position);
        view.highlightSquares(toSquaresArray(selectedMoves));
    }

    private void clearSelection() {
        selectedPosition = null;
        selectedMoves = null;
        view.clearHighlights();
    }

    private boolean isValidDestination(Position position) {
        return selectedMoves != null && selectedMoves.contains(position);
    }

    private int[][] toSquaresArray(List<Position> positions) {
        if (positions == null || positions.isEmpty()) return new int[0][0];
        int[][] squares = new int[positions.size()][2];
        for (int i = 0; i < positions.size(); i++) {
            squares[i][0] = positions.get(i).getX();
            squares[i][1] = positions.get(i).getY();
        }
        return squares;
    }

    private void updateStatusForCurrentPlayer() {
        view.setStatusMessage(model.getCurrentPlayer() + "'s turn");
    }
}
