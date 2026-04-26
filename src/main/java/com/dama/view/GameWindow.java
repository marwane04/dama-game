package com.dama.view;

import com.dama.controller.GameController;
import com.dama.model.Board;
import com.dama.model.BoardModel;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class GameWindow implements CheckersView, PropertyChangeListener {

    private final Board model;
    private GameController controller;

    private final Stage stage;
    private BoardView boardView;
    private Text statusText;

    public GameWindow(Stage stage, Board model) {
        this.stage = stage;
        this.model = model;
        model.addPropertyChangeListener(this);
        buildUI();
    }

    public void setController(GameController controller) {
        this.controller = controller;
        boardView.setController(controller);
    }

    // ── UI Construction ───────────────────────────────────────────

    private void buildUI() {
        // Board
        boardView = new BoardView(model);

        // Sidebar
        VBox sidebar = buildSidebar();

        // Root layout
        HBox root = new HBox(0, boardView, sidebar);
        root.setBackground(new Background(new BackgroundFill(
            Color.web("#1A1A2E"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        Scene scene = new Scene(root);
        stage.setTitle("Dama — Checkers");
        stage.setScene(scene);
        stage.setResizable(false);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(160);
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setBackground(new Background(new BackgroundFill(
            Color.web("#16213E"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        // Title
        Text title = new Text("DAMA");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        title.setFill(Color.web("#FFD700"));

        // Status
        statusText = new Text("Red's turn");
        statusText.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        statusText.setFill(Color.web("#CCCCCC"));
        statusText.setWrappingWidth(130);

        // Buttons
        Button newGameBtn = createButton("New Game");
        Button quitBtn    = createButton("Quit");

        newGameBtn.setOnAction(e -> { if (controller != null) controller.onNewGame(); });
        quitBtn.setOnAction(e    -> { if (controller != null) controller.onQuit(); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(title, statusText, spacer, newGameBtn, quitBtn);
        return sidebar;
    }

    private Button createButton(String label) {
        Button btn = new Button(label);
        btn.setPrefWidth(130);
        btn.setPrefHeight(40);
        btn.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        btn.setStyle(
            "-fx-background-color: #0F3460;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #E94560;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #0F3460;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    // ── CheckersView ──────────────────────────────────────────────

    @Override
    public void refreshBoard() {
        boardView.refresh();
        statusText.setText(model.getCurrentPlayer() + "'s turn");
    }

    @Override
    public void highlightSquares(int[][] squares) {
        boardView.setHighlights(squares);
    }

    @Override
    public void clearHighlights() {
        boardView.clearHighlights();
    }

    @Override
    public void setStatusMessage(String message) {
        statusText.setText(message);
    }

    @Override
    public void showGameOverMessage(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(winner + " wins! Congratulations!");
        alert.showAndWait();
    }

    @Override
    public void show() {
        stage.show();
    }

    // ── PropertyChangeListener (Model → View) ─────────────────────

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("boardState".equals(evt.getPropertyName())) {
                refreshBoard();
            }
            if ("gameOver".equals(evt.getPropertyName())) {
//                #TODO: showGameOverMessage(model.getWinner());
            }
        });
    }
}