package com.dama.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SessionOverlayView {

    private final Stage stage;
    private Runnable onClose;
    private Text codeText;
    private Button copyBtn;

    public SessionOverlayView(Stage owner) {
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Waiting for Opponent");
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.setOnShown(e -> stage.centerOnScreen());
        stage.setOnCloseRequest(e -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        buildUI();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setCode(String code) {
        if (codeText != null) {
            codeText.setText(code);
        }
        if (copyBtn != null) {
            copyBtn.setText("Copy Code");
        }
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    private void buildUI() {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 48, 36, 48));
        card.setBackground(new Background(new BackgroundFill(
                Color.web("#16213E"), new CornerRadii(12), Insets.EMPTY)));

        Text title = new Text("Share this code with your friend");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 15));
        title.setFill(Color.web("#CCCCCC"));

        codeText = new Text("...");
        codeText.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        codeText.setFill(Color.web("#FFD700"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FFD700", 0.6));
        glow.setRadius(18);
        codeText.setEffect(glow);

        Text waiting = new Text("Waiting for opponent to join...");
        waiting.setFont(Font.font("SansSerif", 13));
        waiting.setFill(Color.web("#8899AA"));

        copyBtn = new Button("Copy Code");
        copyBtn.setStyle(
                "-fx-background-color:#0F3460;-fx-text-fill:white;" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-padding:8 20 8 20;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(codeText.getText());
            clipboard.setContent(content);
            copyBtn.setText("Copied");
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color:#2C2C3E;-fx-text-fill:#AAAAAA;" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-padding:8 20 8 20;");
        cancelBtn.setOnAction(e -> {
            if (onClose != null) {
                onClose.run();
            }
            stage.close();
        });

        HBox btnRow = new HBox(12, copyBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, codeText, waiting, btnRow);

        Scene scene = new Scene(card, 380, 240);
        stage.setScene(scene);
    }
}
