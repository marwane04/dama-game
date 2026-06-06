package com.dama.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;

public class GameOverView {

    private final Stage stage;
    private Runnable onNewGame;
    private Runnable onMenu;

    private static final int CONFETTI_COUNT = 55;
    private final double[] cx   = new double[CONFETTI_COUNT];
    private final double[] cy   = new double[CONFETTI_COUNT];
    private final double[] cvx  = new double[CONFETTI_COUNT];
    private final double[] cvy  = new double[CONFETTI_COUNT];
    private final double[] crot = new double[CONFETTI_COUNT];
    private final double[] cspd = new double[CONFETTI_COUNT];
    private final Color[]  ccol = new Color[CONFETTI_COUNT];
    private AnimationTimer confettiTimer;

    public GameOverView(Stage owner, String winner) {
        stage = new Stage(StageStyle.UNDECORATED);
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        buildUI(winner);
    }

    public void setOnNewGame(Runnable onNewGame) { this.onNewGame = onNewGame; }
    public void setOnMenu(Runnable onMenu)       { this.onMenu = onMenu; }

    // Call show() AFTER setting the callbacks
    public void show() {
        stage.show();
        stage.centerOnScreen();

        // Start animations only AFTER stage is visible (safe to animate)
        startEntranceAnimation();
        startMedalFloat();
        startConfettiTimer();
    }

    // ── Fields kept as instance vars so show() can access them ───

    private VBox card;
    private StackPane medal;
    private Canvas confettiCanvas;

    // ── UI ────────────────────────────────────────────────────────

    private void buildUI(String winner) {
        boolean isDraw = winner.equalsIgnoreCase("Draw");

        Color accentA, accentB;
        String emoji, headline, subLine;

        if (isDraw) {
            accentA  = Color.web("#8899AA");
            accentB  = Color.web("#556677");
            emoji    = "\uD83E\uDD1D"; // 🤝
            headline = "It's a Draw!";
            subLine  = "Well played by both sides!";
        } else if (winner.toUpperCase().contains("RED")) {
            accentA  = Color.web("#E94560");
            accentB  = Color.web("#C0392B");
            emoji    = "\uD83C\uDFC6"; // 🏆
            headline = "RED Wins!";
            subLine  = "Congratulations, champion!";
        } else if (winner.toUpperCase().contains("AI")) {
            accentA  = Color.web("#8854D0");
            accentB  = Color.web("#5C2D91");
            emoji    = "\uD83E\uDD16"; // 🤖
            headline = "AI Wins!";
            subLine  = "Better luck next time!";
        } else {
            accentA  = Color.web("#27AE60");
            accentB  = Color.web("#1A6B3C");
            emoji    = "\uD83C\uDFC6"; // 🏆
            headline = "BLACK Wins!";
            subLine  = "Congratulations, champion!";
        }

        final double W = 440, H = 460;

        // ── Confetti canvas (behind everything) ──
        confettiCanvas = new Canvas(W, H);
        initConfetti((int) W, (int) H, accentA, accentB);

        // ── Card ──
        card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(48, 44, 44, 44));
        card.setBackground(new Background(new BackgroundFill(
                Color.web("#16213E"), new CornerRadii(16), Insets.EMPTY
        )));
        card.setStyle(
            "-fx-border-color: " + toHex(accentA) + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;"
        );
        DropShadow cardGlow = new DropShadow(40, accentA.deriveColor(0, 1, 1, 0.45));
        card.setEffect(cardGlow);

        // Accent bar at top of card
        Rectangle bar = new Rectangle(380, 5);
        bar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, accentA.deriveColor(0, 1, 1, 0)),
                new Stop(0.4, accentA),
                new Stop(0.6, accentB),
                new Stop(1, accentB.deriveColor(0, 1, 1, 0))
        ));

        // Medal
        medal = buildMedalRing(emoji, accentA, accentB);

        // Headline
        Text headlineText = new Text(headline);
        headlineText.setFont(Font.font("Georgia", FontWeight.BOLD, 42));
        headlineText.setFill(Color.WHITE);
        headlineText.setEffect(new DropShadow(18, accentA));

        // Sub-line
        Text sub = new Text(subLine);
        sub.setFont(Font.font("Georgia", 14));
        sub.setFill(Color.web("#8899AA"));

        // Divider
        Rectangle divider = new Rectangle(220, 1);
        divider.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.5, accentA.deriveColor(0, 1, 1, 0.5)),
                new Stop(1, Color.TRANSPARENT)
        ));

        // Buttons
        Button newGameBtn = buildButton("Play Again", accentA, accentB);
        Button menuBtn    = buildButton("Main Menu", Color.web("#2C2C3E"), Color.web("#3C3C4E"));

        newGameBtn.setOnAction(e -> { stopConfetti(); stage.close(); if (onNewGame != null) onNewGame.run(); });
        menuBtn.setOnAction(e ->    { stopConfetti(); stage.close(); if (onMenu    != null) onMenu.run(); });

        HBox btnRow = new HBox(14, newGameBtn, menuBtn);
        btnRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(bar, medal, headlineText, sub, divider, btnRow);

        // ── Root ──
        StackPane root = new StackPane();
        root.setPrefSize(W, H);
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#0D0D1E"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        root.getChildren().addAll(confettiCanvas, card);

        // Card starts invisible; entrance animation runs after show()
        card.setOpacity(0);

        Scene scene = new Scene(root, W, H);
        stage.setScene(scene);
    }

    // ── Animations (called from show(), after stage is visible) ──

    private void startEntranceAnimation() {
        card.setScaleX(0.85);
        card.setScaleY(0.85);

        FadeTransition fade = new FadeTransition(Duration.millis(350), card);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
        scale.setFromX(0.85); scale.setToX(1.0);
        scale.setFromY(0.85); scale.setToY(1.0);
        scale.setInterpolator(Interpolator.SPLINE(0.22, 1.4, 0.58, 1));
        scale.play();
    }

    private void startMedalFloat() {
        TranslateTransition floatAnim = new TranslateTransition(Duration.millis(2400), medal);
        floatAnim.setFromY(0); floatAnim.setToY(-10);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();
    }

    private void startConfettiTimer() {
        GraphicsContext gc = confettiCanvas.getGraphicsContext2D();
        double W = confettiCanvas.getWidth(), H = confettiCanvas.getHeight();
        confettiTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                gc.clearRect(0, 0, W, H);
                for (int i = 0; i < CONFETTI_COUNT; i++) {
                    cy[i] += cvy[i];
                    cx[i] += cvx[i];
                    crot[i] += cspd[i];
                    if (cy[i] > H + 10) { cy[i] = -12; cx[i] = Math.random() * W; }
                    if (cx[i] < -10)    { cx[i] = W + 10; }
                    if (cx[i] > W + 10) { cx[i] = -10; }
                    gc.save();
                    gc.translate(cx[i], cy[i]);
                    gc.rotate(crot[i]);
                    gc.setGlobalAlpha(0.80);
                    gc.setFill(ccol[i]);
                    int shape = i % 3;
                    if (shape == 0)      gc.fillRect(-5, -3, 10, 6);
                    else if (shape == 1) gc.fillOval(-4, -4, 8, 8);
                    else                 gc.fillPolygon(new double[]{0,-5,5}, new double[]{-5,4,4}, 3);
                    gc.restore();
                }
            }
        };
        confettiTimer.start();
    }

    // ── Medal ─────────────────────────────────────────────────────

    private StackPane buildMedalRing(String emoji, Color a, Color b) {
        StackPane sp = new StackPane();

        Circle outerGlow = new Circle(52);
        outerGlow.setFill(Color.TRANSPARENT);
        outerGlow.setStroke(a.deriveColor(0, 1, 1, 0.25));
        outerGlow.setStrokeWidth(2);
        outerGlow.setEffect(new DropShadow(28, a.deriveColor(0, 1, 1, 0.55)));

        Circle body = new Circle(44);
        body.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, a.deriveColor(0, 1, 0.28, 1)),
                new Stop(1, b.deriveColor(0, 1, 0.18, 1))
        ));
        body.setStroke(a.deriveColor(0, 1, 1, 0.75));
        body.setStrokeWidth(2);

        Text t = new Text(emoji);
        t.setFont(Font.font("Serif", 38));

        sp.getChildren().addAll(outerGlow, body, t);
        sp.setMinSize(114, 114);
        sp.setMaxSize(114, 114);
        return sp;
    }

    // ── Button ────────────────────────────────────────────────────

    private Button buildButton(String label, Color bg, Color hoverBg) {
        Button btn = new Button(label);
        btn.setPrefWidth(155);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        String base = btnStyle(bg);
        String hov  = btnStyle(hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private String btnStyle(Color c) {
        return String.format(
            "-fx-background-color:rgb(%d,%d,%d);" +
            "-fx-text-fill:white;-fx-background-radius:10;-fx-cursor:hand;",
            (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)
        );
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    // ── Confetti init ─────────────────────────────────────────────

    private void initConfetti(int W, int H, Color a, Color b) {
        Color[] palette = {
            a, b, Color.web("#FFD700"), Color.web("#FFFFFF"),
            a.brighter(), b.brighter(), Color.web("#DDDDDD")
        };
        Random rng = new Random();
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            cx[i]  = rng.nextDouble() * W;
            cy[i]  = -rng.nextDouble() * H * 0.5;
            cvx[i] = (rng.nextDouble() - 0.5) * 1.4;
            cvy[i] = rng.nextDouble() * 1.6 + 0.7;
            crot[i]= rng.nextDouble() * 360;
            cspd[i]= (rng.nextDouble() - 0.5) * 4;
            ccol[i]= palette[rng.nextInt(palette.length)];
        }
    }

    private void stopConfetti() {
        if (confettiTimer != null) confettiTimer.stop();
    }
}