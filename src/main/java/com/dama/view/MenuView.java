package com.dama.view;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MenuView {

    // ── Listener interface (matches Main.java expectations) ───────
    public interface MenuListener {
        void onSinglePlayer();
        void onMultiplayerHost();
        void onMultiplayerJoin(String code);
    }

    private final Stage stage;
    private MenuListener menuListener;

    public MenuView(Stage stage) {
        this.stage = stage;
    }

    public void setMenuListener(MenuListener listener) {
        this.menuListener = listener;
    }

    // Keep these for backward compatibility with any old references
    public void setOnSinglePlayer(Runnable r) {
        setMenuListener(new MenuListener() {
            @Override public void onSinglePlayer()            { r.run(); }
            @Override public void onMultiplayerHost()         {}
            @Override public void onMultiplayerJoin(String c) {}
        });
    }

    public void show() {
        // ── Root ──────────────────────────────────────────────────
        StackPane root = new StackPane();
        root.setPrefSize(820, 640);

        // ── Background — board pattern ────────────────────────────
        Canvas bg = new Canvas(820, 640);
        drawBackground(bg.getGraphicsContext2D());
        root.getChildren().add(bg);

        // ── Dark overlay ──────────────────────────────────────────
        Pane overlay = new Pane();
        overlay.setPrefSize(820, 640);
        overlay.setBackground(new Background(new BackgroundFill(
            Color.rgb(10, 10, 20, 0.80), CornerRadii.EMPTY, Insets.EMPTY
        )));
        root.getChildren().add(overlay);

        // ── Center card ───────────────────────────────────────────
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(360);
        card.setPadding(new Insets(45, 40, 45, 40));
        card.setBackground(new Background(new BackgroundFill(
            Color.rgb(22, 28, 48, 0.93), new CornerRadii(16), Insets.EMPTY
        )));
        card.setStyle("-fx-border-color: #3a4060; -fx-border-width: 1; -fx-border-radius: 16;");

        // Decorative pieces
        HBox pieces = buildDecorativePieces();

        // Title
        Text title = new Text("DAMA");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 58));
        title.setFill(Color.web("#FFD700"));
        title.setStyle("-fx-effect: dropshadow(gaussian, #FFD70088, 18, 0.4, 0, 0);");

        // Subtitle
        Text subtitle = new Text("Jeu de Dames");
        subtitle.setFont(Font.font("Serif", FontWeight.NORMAL, 15));
        subtitle.setFill(Color.web("#8899AA"));

        // Divider
        Pane divider = new Pane();
        divider.setPrefSize(200, 1);
        divider.setMaxWidth(200);
        divider.setBackground(new Background(new BackgroundFill(
            Color.web("#3a4060"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        // ── Single Player button ──────────────────────────────────
        Button singleBtn = createMenuButton("⚔   Single Player  (vs AI)", "#C0392B", "#922B21");
        singleBtn.setOnAction(e -> {
            if (menuListener != null) menuListener.onSinglePlayer();
        });

        // ── Multiplayer section label ─────────────────────────────
        Text multiLabel = new Text("MULTIPLAYER");
        multiLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 11));
        multiLabel.setFill(Color.web("#556677"));

        // ── Host button ───────────────────────────────────────────
        Button hostBtn = createMenuButton("🌐   Host a Game", "#0F3460", "#0a2040");
        hostBtn.setOnAction(e -> {
            if (menuListener != null) menuListener.onMultiplayerHost();
        });

        // ── Join section — code input + button ────────────────────
        HBox joinRow = buildJoinRow();

        card.getChildren().addAll(
            pieces, title, subtitle, divider,
            singleBtn, multiLabel, hostBtn, joinRow
        );

        StackPane.setAlignment(card, Pos.CENTER);
        root.getChildren().add(card);

        // ── Entrance animation ────────────────────────────────────
        card.setOpacity(0);
        card.setTranslateY(25);

        FadeTransition fade = new FadeTransition(Duration.millis(550), card);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(550), card);
        slide.setFromY(25); slide.setToY(0); slide.play();

        // Add icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/icon.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, skipping.");
        }

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
        stage.show();
    }

    // ── Join row (code input + join button) ───────────────────────

    private HBox buildJoinRow() {
        TextField codeField = new TextField();
        codeField.setPromptText("Enter code…");
        codeField.setPrefWidth(160);
        codeField.setPrefHeight(40);
        codeField.setStyle(
            "-fx-background-color: #1a2040;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #556677;" +
            "-fx-border-color: #3a4060;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-font-size: 14px;"
        );

        Button joinBtn = new Button("Join");
        joinBtn.setPrefHeight(40);
        joinBtn.setPrefWidth(90);
        joinBtn.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        String base  = "-fx-background-color:#1B6CA8;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        String hover = "-fx-background-color:#1483C8;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        joinBtn.setStyle(base);
        joinBtn.setOnMouseEntered(e -> joinBtn.setStyle(hover));
        joinBtn.setOnMouseExited(e  -> joinBtn.setStyle(base));

        joinBtn.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (!code.isEmpty() && menuListener != null) {
                menuListener.onMultiplayerJoin(code);
            }
        });

        // Also allow pressing Enter in the text field
        codeField.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (!code.isEmpty() && menuListener != null) {
                menuListener.onMultiplayerJoin(code);
            }
        });

        HBox row = new HBox(10, codeField, joinBtn);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void drawBackground(GraphicsContext gc) {
        Color dark  = Color.web("#2C1A0E");
        Color light = Color.web("#3D2512");
        int tileSize = 80;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 11; c++) {
                gc.setFill((r + c) % 2 == 0 ? light : dark);
                gc.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
            }
        }
    }

    private HBox buildDecorativePieces() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            Circle c = new Circle(10);
            c.setFill(i % 2 == 0 ? Color.web("#C0392B") : Color.web("#1A1A2E"));
            c.setStroke(Color.web("#FFD70066"));
            c.setStrokeWidth(1.5);
            box.getChildren().add(c);
        }
        return box;
    }

    private Button createMenuButton(String label, String bg, String hoverBg) {
        Button btn = new Button(label);
        btn.setPrefWidth(280);
        btn.setPrefHeight(48);
        btn.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        String base  = "-fx-background-color:" + bg      + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;";
        String hover = "-fx-background-color:" + hoverBg + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
}