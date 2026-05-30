package com.dama.controller;

import com.dama.model.*;
import com.dama.network.Client;
import com.dama.network.Move;
import com.dama.view.CheckersView;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;

public class GameController {

    private final Board model;
    private final CheckersView view;
    private final GameType gameType;

    private Client localClient;
    private final AIPlayer aiPlayer;

    private boolean isLocalTurn = true;
    private boolean onlineReady = true;
    private boolean aiThinking = false;

    private Position selectedPosition;
    private List<Position> selectedMoves;

    public GameController(Board model, CheckersView view, GameType gameType) {
        this.model = model;
        this.view = view;
        this.gameType = gameType;
        this.aiPlayer = new AIPlayer();
        updateStatusForCurrentPlayer();
    }

    public void setLocalClient(Client localClient) {
        this.localClient = localClient;
        if (this.localClient != null && gameType == GameType.ONLINE) {
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
            this.localClient.setMoveListener(this::handleRemoteMove);
            this.localClient.listenForMoves();
        }
    }

    public void setLocalTurn(boolean isLocalTurn) {
        this.isLocalTurn = isLocalTurn;
        Platform.runLater(this::updateStatusForCurrentPlayer);
    }

    // ── Square click handler ──────────────────────────────────────

    public void onSquareSelected(int row, int col) {
        if (model.getState() != GameState.IN_PROGRESS) return;

        // Online guard
        if (gameType == GameType.ONLINE) {
            if (!onlineReady) { view.setStatusMessage("Waiting for opponent"); return; }
            if (!isLocalTurn) { view.setStatusMessage("Opponent's turn"); return; }
        }

        // AI guard — don't let player move while AI is thinking or during AI's turn
        if (gameType == GameType.LOCAL_VS_AI) {
            if (aiThinking) return;
            if (model.getCurrentPlayer() == Color.BLACK) return; // AI's turn
        }

        Position clicked = new Position(row, col);
        Piece clickedPiece = model.getPiece(clicked);

        // ── Nothing selected yet ──────────────────────────────────
        if (selectedPosition == null) {
            Color expected = (gameType == GameType.ONLINE) ? Color.RED : model.getCurrentPlayer();
            if (clickedPiece == null || clickedPiece.getColor() != expected) {
                view.setStatusMessage("Select a " + expected + " piece");
                return;
            }
            selectPiece(clicked);
            return;
        }

        // ── Clicked the already-selected piece ────────────────────
        if (selectedPosition.equals(clicked)) {
            clearSelection();
            return;
        }

        // ── Clicked another own piece ─────────────────────────────
        if (clickedPiece != null) {
            Color expected = (gameType == GameType.ONLINE) ? Color.RED : model.getCurrentPlayer();
            if (clickedPiece.getColor() == expected) {
                selectPiece(clicked);
                return;
            }
        }

        // ── Try to move ───────────────────────────────────────────
        if (isValidDestination(clicked)) {
            Position from = selectedPosition;
            clearSelection();
            model.movePiece(from, clicked);

            if (gameType == GameType.ONLINE && localClient != null) {
                localClient.sendMove(new Move(from, clicked).getMirrorMove());
                isLocalTurn = false;
            }

            updateStatusForCurrentPlayer();
            checkGameOver();

            if (gameType == GameType.LOCAL_VS_AI && model.getState() == GameState.IN_PROGRESS) {
                scheduleAiMove();
            }
        } else {
            view.setStatusMessage("Invalid move");
        }
    }

    // ── AI scheduling ─────────────────────────────────────────────

    private void scheduleAiMove() {
        aiThinking = true;
        view.setStatusMessage("AI is thinking…");

        Task<int[]> task = new Task<>() {
            @Override
            protected int[] call() {
                try { Thread.sleep(400); } catch (InterruptedException ignored) {} // small delay for feel
                return aiPlayer.getBestMove(model);
            }
        };

        task.setOnSucceeded(e -> {
            int[] move = task.getValue();
            aiThinking = false;
            if (move == null || model.getState() != GameState.IN_PROGRESS) {
                updateStatusForCurrentPlayer();
                return;
            }
            model.movePiece(new Position(move[0], move[1]), new Position(move[2], move[3]));
            updateStatusForCurrentPlayer();
            checkGameOver();
        });

        task.setOnFailed(e -> {
            aiThinking = false;
            updateStatusForCurrentPlayer();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ── Game actions ──────────────────────────────────────────────

    public void onNewGame() {
        model.reset();
        clearSelection();
        aiThinking = false;
        updateStatusForCurrentPlayer();
    }

    public void onQuit() {
        Platform.exit();
    }

    // ── Selection helpers ─────────────────────────────────────────

    private void selectPiece(Position position) {
        selectedPosition = position;
        selectedMoves = model.getPossibleMovements(position);
        view.highlightSquares(toSquaresArray(selectedMoves));
        view.setSelectedPiece(position.getX(), position.getY());
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

    // ── Status helpers ────────────────────────────────────────────

    private void updateStatusForCurrentPlayer() {
        if (model.getState() != GameState.IN_PROGRESS) return;
        if (gameType == GameType.ONLINE) {
            if (!onlineReady) { view.setStatusMessage("Waiting for opponent"); return; }
            view.setStatusMessage(isLocalTurn ? "Your turn (RED)" : "Opponent's turn");
        } else if (gameType == GameType.LOCAL_VS_AI) {
            if (model.getCurrentPlayer() == Color.RED) {
                view.setStatusMessage("Your turn (RED)");
            } else {
                view.setStatusMessage("AI thinking…");
            }
        } else {
            view.setStatusMessage(model.getCurrentPlayer() + "'s turn");
        }
    }

    private void checkGameOver() {
        GameState state = model.getState();
        if (state == GameState.RED_WINS)   view.showGameOverMessage("RED");
        if (state == GameState.BLACK_WINS) view.showGameOverMessage(gameType == GameType.LOCAL_VS_AI ? "AI (BLACK)" : "BLACK");
        if (state == GameState.DRAW)       view.showGameOverMessage("Draw");
    }

    // ── Remote move handler (Online) ──────────────────────────────

    private void handleRemoteMove(Move move) {
        if (move == null || gameType != GameType.ONLINE) return;
        Platform.runLater(() -> {
            if (model.getState() != GameState.IN_PROGRESS) return;
            clearSelection();
            model.movePiece(move.getInitialPosition(), move.getTargetPosition());
            isLocalTurn = true;
            updateStatusForCurrentPlayer();
            checkGameOver();
        });
    }
}