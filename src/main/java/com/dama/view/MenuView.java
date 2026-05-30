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

    // ── Listener interface ────────────────────────────────────────
    public interface MenuListener {
        void onSinglePlayer();
        void onPvP();
        void onMultiplayerHost();
        void onMultiplayerJoin(String code);
    }

    private final Stage stage;
    private MenuListener menuListener;

    // Track which card is showing
    private StackPane root;

    public MenuView(Stage stage) {
        this.stage = stage;
    }

    public void setMenuListener(MenuListener listener) {
        this.menuListener = listener;
    }

    // ── Main entry point ──────────────────────────────────────────

    public void show() {
        root = new StackPane();
        root.setPrefSize(820, 640);

        // Background board pattern
        Canvas bg = new Canvas(820, 640);
        drawBackground(bg.getGraphicsContext2D());
        root.getChildren().add(bg);

        // Dark overlay
        Pane overlay = new Pane();
        overlay.setPrefSize(820, 640);
        overlay.setBackground(new Background(new BackgroundFill(
            Color.rgb(10, 10, 20, 0.80), CornerRadii.EMPTY, Insets.EMPTY
        )));
        root.getChildren().add(overlay);

        // Show main menu card
        showMainCard();

        // Icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/damalcon.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, skipping.");
        }

        Scene scene = new Scene(root);
        stage.setTitle("Dama — Checkers");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // ── Screen 1: Main Menu ───────────────────────────────────────

    private void showMainCard() {
        VBox card = buildCardShell();

        // Header
        HBox pieces = buildDecorativePieces();
        Text title = buildTitle("DAMA");
        Text subtitle = buildSubtitle("Jeu de Dames");
        Pane divider = buildDivider();

        // Mode buttons
        Button pvAiBtn  = createMenuButton("⚔   Player vs AI",      "#C0392B", "#922B21");
        Button pvpBtn   = createMenuButton("👥   Player vs Player",  "#1A6B3C", "#145530");
        Button multiBtn = createMenuButton("🌐   Multiplayer",       "#0F3460", "#0a2040");

        pvAiBtn.setOnAction(e -> {
            if (menuListener != null) menuListener.onSinglePlayer();
        });

        pvpBtn.setOnAction(e -> {
            // Directly launch local two-player — no sub-menu needed
            if (menuListener != null) menuListener.onMultiplayerHost(); 
            // Note: we use a dedicated PvP callback below
        });

        multiBtn.setOnAction(e -> animateToCard(buildMultiplayerCard()));

        // Override PvP to use its own path
        pvpBtn.setOnAction(e -> {
            if (menuListener != null) menuListener.onPvP();
        });

        card.getChildren().addAll(pieces, title, subtitle, divider, pvAiBtn, pvpBtn, multiBtn);
        animateCardIn(card);
    }

    // ── Screen 2: Multiplayer Sub-menu ────────────────────────────

    private VBox buildMultiplayerCard() {
        VBox card = buildCardShell();

        Text title    = buildTitle("Multiplayer");
        Text subtitle = buildSubtitle("Online Game");
        Pane divider  = buildDivider();

        // Host button
        Button hostBtn = createMenuButton("🏠   Host a Game", "#0F3460", "#0a2040");
        hostBtn.setOnAction(e -> {
            if (menuListener != null) menuListener.onMultiplayerHost();
        });

        // Join section
        Text joinLabel = new Text("— or join with a code —");
        joinLabel.setFont(Font.font("SansSerif", 12));
        joinLabel.setFill(Color.web("#556677"));

        HBox joinRow = buildJoinRow();

        // Back button
        Button backBtn = createSecondaryButton("← Back");
        backBtn.setOnAction(e -> {
            clearCards();
            showMainCard();
        });

        card.getChildren().addAll(title, subtitle, divider, hostBtn, joinLabel, joinRow, backBtn);
        return card;
    }

    // ── Animation helpers ─────────────────────────────────────────

    private void animateToCard(VBox newCard) {
        clearCards();
        animateCardIn(newCard);
    }

    private void clearCards() {
        // Remove all VBox cards (keep bg + overlay which are Canvas and Pane)
        root.getChildren().removeIf(n -> n instanceof VBox);
    }

    private void animateCardIn(VBox card) {
        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        card.setOpacity(0);
        card.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
        slide.setFromY(20); slide.setToY(0); slide.play();
    }

    // ── Card shell ────────────────────────────────────────────────

    private VBox buildCardShell() {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(360);
        card.setPadding(new Insets(45, 40, 45, 40));
        card.setBackground(new Background(new BackgroundFill(
            Color.rgb(22, 28, 48, 0.93), new CornerRadii(16), Insets.EMPTY
        )));
        card.setStyle("-fx-border-color: #3a4060; -fx-border-width: 1; -fx-border-radius: 16;");
        return card;
    }

    // ── Join row ──────────────────────────────────────────────────

    private HBox buildJoinRow() {
        TextField codeField = new TextField();
        codeField.setPromptText("Enter code…");
        codeField.setPrefWidth(160);
        codeField.setPrefHeight(40);
        codeField.setStyle(
            "-fx-background-color:#1a2040;" +
            "-fx-text-fill:white;" +
            "-fx-prompt-text-fill:#556677;" +
            "-fx-border-color:#3a4060;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;" +
            "-fx-font-size:14px;"
        );

        Button joinBtn = new Button("Join");
        joinBtn.setPrefSize(90, 40);
        joinBtn.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        String base  = "-fx-background-color:#1B6CA8;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        String hover = "-fx-background-color:#1483C8;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;";
        joinBtn.setStyle(base);
        joinBtn.setOnMouseEntered(e -> joinBtn.setStyle(hover));
        joinBtn.setOnMouseExited(e  -> joinBtn.setStyle(base));

        Runnable doJoin = () -> {
            String code = codeField.getText().trim();
            if (!code.isEmpty() && menuListener != null) {
                menuListener.onMultiplayerJoin(code);
            }
        };
        joinBtn.setOnAction(e -> doJoin.run());
        codeField.setOnAction(e -> doJoin.run());

        HBox row = new HBox(10, codeField, joinBtn);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ── Reusable UI pieces ────────────────────────────────────────

    private Text buildTitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Serif", FontWeight.BOLD, 48));
        t.setFill(Color.web("#FFD700"));
        t.setStyle("-fx-effect: dropshadow(gaussian, #FFD70088, 18, 0.4, 0, 0);");
        return t;
    }

    private Text buildSubtitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Serif", FontWeight.NORMAL, 14));
        t.setFill(Color.web("#8899AA"));
        return t;
    }

    private Pane buildDivider() {
        Pane d = new Pane();
        d.setPrefSize(200, 1);
        d.setMaxWidth(200);
        d.setBackground(new Background(new BackgroundFill(
            Color.web("#3a4060"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        return d;
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

    private Button createSecondaryButton(String label) {
        Button btn = new Button(label);
        btn.setPrefWidth(280);
        btn.setPrefHeight(38);
        btn.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        String base  = "-fx-background-color:#2C2C3E;-fx-text-fill:#AAAAAA;-fx-background-radius:8;-fx-cursor:hand;";
        String hover = "-fx-background-color:#3C3C4E;-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private void drawBackground(GraphicsContext gc) {
        Color dark  = Color.web("#2C1A0E");
        Color light = Color.web("#3D2512");
        int ts = 80;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 11; c++) {
                gc.setFill((r + c) % 2 == 0 ? light : dark);
                gc.fillRect(c * ts, r * ts, ts, ts);
            }
    }
}