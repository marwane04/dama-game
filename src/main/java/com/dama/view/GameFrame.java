package com.dama.view;

import com.dama.controller.GameController;
import com.dama.model.BoardModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class GameFrame extends JFrame implements CheckersView, PropertyChangeListener {
    private final BoardModel model;
    private GameController controller;

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private StatusBar statusBar;

    public GameFrame(BoardModel model) {
        this.model = model;
        model.addPropertyChangeListener(this); // subscribe to model changes
        initUI();
    }

    public void setController(GameController controller) {
        this.controller = controller;
        boardPanel.setController(controller);
        controlPanel.setController(controller);
    }

    private void initUI() {
        setTitle("Checkers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        boardPanel   = new BoardPanel(model);
        controlPanel = new ControlPanel();
        statusBar    = new StatusBar();

        add(boardPanel,   BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(statusBar,    BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // center on screen
    }

    // --- CheckersView interface ---

    @Override
    public void refreshBoard() {
        boardPanel.repaint();
        statusBar.setMessage(model.getCurrentPlayer() + "'s turn");
    }

    @Override
    public void highlightSquares(int[][] squares) {
        boardPanel.setHighlights(squares);
        boardPanel.repaint();
    }

    @Override
    public void clearHighlights() {
        boardPanel.clearHighlights();
        boardPanel.repaint();
    }

    @Override
    public void setStatusMessage(String message) {
        statusBar.setMessage(message);
    }

    @Override
    public void showGameOverMessage(String winner) {
        JOptionPane.showMessageDialog(
            this,
            winner + " wins! Congratulations!",
            "Game Over",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void show() {
        setVisible(true);
    }

    // --- PropertyChangeListener (Model notifies View here) ---

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Always update UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            if ("boardState".equals(evt.getPropertyName())) {
                refreshBoard();
            }
            if ("gameOver".equals(evt.getPropertyName())) {
                showGameOverMessage(model.getWinner());
            }
        });
    }
}
