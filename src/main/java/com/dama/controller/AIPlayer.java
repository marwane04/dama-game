package com.dama.controller;

import com.dama.model.*;

import java.util.ArrayList;
import java.util.List;

public class AIPlayer {

    private static final int MAX_DEPTH = 4;
    private static final Color AI_COLOR    = Color.BLACK;
    private static final Color HUMAN_COLOR = Color.RED;

    public AIPlayer() {}

    public int[] getBestMove(Board board) {
        List<int[]> allMoves = getAllMoves(board, AI_COLOR);
        if (allMoves.isEmpty()) return null;

        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int[] move : allMoves) {
            Board copy = copyBoard(board);
            copy.movePiece(new Position(move[0], move[1]), new Position(move[2], move[3]));
            int score = minimax(copy, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
        }
        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || board.getState() != GameState.IN_PROGRESS) {
            return evaluate(board);
        }

        Color current = maximizing ? AI_COLOR : HUMAN_COLOR;
        List<int[]> moves = getAllMoves(board, current);
        if (moves.isEmpty()) return maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        if (maximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int[] move : moves) {
                Board copy = copyBoard(board);
                copy.movePiece(new Position(move[0], move[1]), new Position(move[2], move[3]));
                int score = minimax(copy, depth - 1, alpha, beta, false);
                maxScore = Math.max(maxScore, score);
                alpha    = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int[] move : moves) {
                Board copy = copyBoard(board);
                copy.movePiece(new Position(move[0], move[1]), new Position(move[2], move[3]));
                int score = minimax(copy, depth - 1, alpha, beta, true);
                minScore = Math.min(minScore, score);
                beta     = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return minScore;
        }
    }

    private int evaluate(Board board) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null) continue;

                int pieceValue    = piece.isKing() ? 30 : 10;
                int advanceBonus  = piece.getColor() == Color.BLACK
                    ? piece.getPosition().getX()
                    : (7 - piece.getPosition().getX());
                int centerBonus   = (piece.getPosition().getY() >= 2 &&
                                     piece.getPosition().getY() <= 5) ? 2 : 0;

                int total = pieceValue + advanceBonus + centerBonus;
                if (piece.getColor() == AI_COLOR) score += total;
                else                              score -= total;
            }
        }
        return score;
    }

    private List<int[]> getAllMoves(Board board, Color color) {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null || piece.getColor() != color) continue;
                Position from = new Position(r, c);
                for (Position to : board.getPossibleMovements(from)) {
                    moves.add(new int[]{r, c, to.getX(), to.getY()});
                }
            }
        }
        return moves;
    }

    private Board copyBoard(Board original) {
        Board copy = new Board();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                clearPiece(copy, r, c);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = original.getPiece(r, c);
                if (p != null) setPiece(copy, r, c, p.getColor(), p.isKing());
            }
        }
        if (copy.getCurrentPlayer() != original.getCurrentPlayer()) {
            copy.switchTurn();
        }
        return copy;
    }

    private void clearPiece(Board board, int r, int c) {
        try {
            var field = board.getClass().getDeclaredField("pieces");
            field.setAccessible(true);
            ((Piece[][]) field.get(board))[r][c] = null;
        } catch (Exception e) { throw new RuntimeException("Board copy failed", e); }
    }

    private void setPiece(Board board, int r, int c, Color color, boolean isKing) {
        try {
            var field = board.getClass().getDeclaredField("pieces");
            field.setAccessible(true);
            Piece p = new Piece(color, new Position(r, c));
            if (isKing) p.setKing();
            ((Piece[][]) field.get(board))[r][c] = p;
        } catch (Exception e) { throw new RuntimeException("Board copy failed", e); }
    }
}