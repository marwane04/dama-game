package com.dama.view;

import com.dama.controller.GameController;
import com.dama.model.Board;
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

    /** Called when the user clicks "Menu" to return to the main menu. */
    private Runnable onMenuRequested;

    public void setOnMenuRequested(Runnable callback) {
        this.onMenuRequested = callback;
    }

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
        boardView = new BoardView(model);

        VBox sidebar = buildSidebar();

        HBox root = new HBox(0, boardView, sidebar);
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#1A1A2E"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        Scene scene = new Scene(root);
        stage.setTitle("Dama — Checkers");
        stage.setScene(scene);
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/damaIcon.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, skipping.");
        }
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

        Text title = new Text("DAMA");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        title.setFill(Color.web("#FFD700"));

        statusText = new Text("Red's turn");
        statusText.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        statusText.setFill(Color.web("#CCCCCC"));
        statusText.setWrappingWidth(130);

        // Legend
        VBox legend = buildLegend();

        Button newGameBtn = createButton("New Game");
        Button quitBtn    = createButton("Quit");

        Button menuBtn = createButton("Menu");

        newGameBtn.setOnAction(e -> { if (controller != null) controller.onNewGame(); });
        menuBtn.setOnAction(e ->    { if (onMenuRequested != null) onMenuRequested.run(); });
        quitBtn.setOnAction(e ->    { if (controller != null) controller.onQuit(); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(title, statusText, legend, spacer, newGameBtn, menuBtn, quitBtn);
        return sidebar;
    }

    private VBox buildLegend() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 0, 0, 0));

        box.getChildren().add(legendRow("●", "#27AE60", "Move"));
        box.getChildren().add(legendRow("●", "#E67E22", "Capture"));
        box.getChildren().add(legendRow("□", "#FFD700", "Selected"));

        return box;
    }

    private javafx.scene.layout.HBox legendRow(String symbol, String hexColor, String label) {
        Text sym = new Text(symbol);
        sym.setFill(Color.web(hexColor));
        sym.setFont(Font.font("SansSerif", 14));

        Text lbl = new Text("  " + label);
        lbl.setFill(Color.web("#AAAAAA"));
        lbl.setFont(Font.font("SansSerif", 12));

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(sym, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Button createButton(String label) {
        Button btn = new Button(label);
        btn.setPrefWidth(130);
        btn.setPrefHeight(40);
        btn.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        String normal = "-fx-background-color:#0F3460;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        String hover  = "-fx-background-color:#E94560;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        return btn;
    }

    // ── CheckersView ──────────────────────────────────────────────

    @Override public void refreshBoard() { boardView.refresh(); }

    @Override public void highlightSquares(int[][] squares) { boardView.setHighlights(squares); }

    @Override public void setSelectedPiece(int row, int col) { boardView.setSelectedPiece(row, col); }

    @Override public void clearHighlights() { boardView.clearHighlights(); }

    @Override public void setStatusMessage(String message) {
        Platform.runLater(() -> statusText.setText(message));
    }

    @Override
    public void showGameOverMessage(String winner) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(winner + " wins! Congratulations!");
            alert.showAndWait();
        });
    }

    @Override public void show() { stage.show(); }

    // ── PropertyChangeListener ────────────────────────────────────

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("boardState".equals(evt.getPropertyName())) refreshBoard();
        });
    }
}