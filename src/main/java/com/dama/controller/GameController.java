package com.dama.controller;

import com.dama.model.*;
import com.dama.network.Client;
import com.dama.network.Move;
import com.dama.view.CheckersView;
import javafx.application.Platform;

import java.util.List;

public class GameController {

    private final Board model;
    private final CheckersView view;
    private final GameType gameType;

    private Client localClient;

    private boolean isLocalTurn = true;
    private boolean onlineReady = true;

    private Position selectedPosition;
    private List<Position> selectedMoves;

    public GameController(Board model, CheckersView view, GameType gameType) {
        this.model = model;
        this.view = view;
        this.gameType = gameType;
        updateStatusForCurrentPlayer();
    }

    public void setLocalClient(Client localClient) {
        this.localClient = localClient;
        if (this.localClient != null) {
            if (gameType == GameType.ONLINE) {
                onlineReady = false;
                setLocalTurn(false);
                this.localClient.setStartListener(isLocalTurn -> {
                    onlineReady = true;
                    setLocalTurn(isLocalTurn);
                });
                this.localClient.setWaitingListener(() -> {
                    onlineReady = false;
                    updateStatusForCurrentPlayer();
                });
            }
            this.localClient.setMoveListener(this::handleRemoteMove);
            this.localClient.listenForMoves();
        }
    }

    public void setLocalTurn(boolean isLocalTurn) {
        this.isLocalTurn = isLocalTurn;
        updateStatusForCurrentPlayer();
    }

    public void onSquareSelected(int row, int col) {
        if (model.getState() != GameState.IN_PROGRESS) {
            return;
        }

        if (gameType == GameType.ONLINE) {
            if (!onlineReady) {
                view.setStatusMessage("Waiting for opponent");
                return;
            }
            if (!isLocalTurn) {
                view.setStatusMessage("Opponent's turn");
                return;
            }
        }

        Position clicked = new Position(row, col);
        Piece clickedPiece = model.getPiece(clicked);

        if (selectedPosition == null) {
            if (gameType == GameType.ONLINE) {
                if (clickedPiece == null || clickedPiece.getColor() != Color.RED) {
                    view.setStatusMessage("Select a RED piece");
                    return;
                }
            } else if (clickedPiece == null || clickedPiece.getColor() != model.getCurrentPlayer()) {
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

        if (clickedPiece != null) {
            if (gameType == GameType.ONLINE && clickedPiece.getColor() == Color.RED) {
                selectPiece(clicked);
                return;
            }
            if (gameType != GameType.ONLINE && clickedPiece.getColor() == model.getCurrentPlayer()) {
                selectPiece(clicked);
                return;
            }
        }

        if (isValidDestination(clicked)) {
            model.movePiece(selectedPosition, clicked);
            if (gameType == GameType.ONLINE && localClient != null) {
                localClient.sendMove((new Move(selectedPosition, clicked).getMirrorMove()));
                isLocalTurn = false;
            }
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
        if (gameType == GameType.ONLINE) {
            if (!onlineReady) {
                view.setStatusMessage("Waiting for opponent");
                return;
            }
            view.setStatusMessage(isLocalTurn ? "Your turn" : "Opponent's turn");
        } else {
            view.setStatusMessage(model.getCurrentPlayer() + "'s turn");
            System.out.println("am i a joke to u");
        }
    }

    private void handleRemoteMove(Move move) {
        if (move == null || gameType != GameType.ONLINE) {
            return;
        }
        Platform.runLater(() -> {
            if (model.getState() != GameState.IN_PROGRESS) {
                return;
            }
            clearSelection();
            model.movePiece(move.getInitialPosition(), move.getTargetPosition());
            isLocalTurn = true;
            updateStatusForCurrentPlayer();
        });
    }
}