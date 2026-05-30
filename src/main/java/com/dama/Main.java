package com.dama;

import com.dama.controller.GameController;
import com.dama.controller.GameType;
import com.dama.model.Board;
import com.dama.network.Client;
import com.dama.view.GameWindow;
import com.dama.view.MenuView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showMenu();
    }

    // ── Menu ──────────────────────────────────────────────────────

    private void showMenu() {
        MenuView menu = new MenuView(primaryStage);
        menu.setMenuListener(new MenuView.MenuListener() {

            @Override
            public void onSinglePlayer() {
                startSinglePlayer();
            }

            @Override
            public void onPvP() { startPvP(); }  

            @Override
            public void onMultiplayerHost() {
                startMultiplayerHost();
            }

            @Override
            public void onMultiplayerJoin(String code) {
                startMultiplayerJoin(code);
            }
        });
        menu.show();
    }

    // ── Single Player (vs AI) ─────────────────────────────────────

    private void startSinglePlayer() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.LOCAL_VS_AI);
        window.setController(controller);
        addBackToMenuButton(window, controller);
        window.show();
    }

    // ── Single Player (vs Local) ─────────────────────────────────────

    private void startPvP() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.LOCAL_TWO_PAYERS);
        window.setController(controller);
        addBackToMenuButton(window, controller);
        window.show();
    }

    // ── Multiplayer — Host ────────────────────────────────────────

    private void startMultiplayerHost() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);

        // Show a waiting overlay with the session code
        showSessionCodeOverlay(window, controller, board, null);
    }

    // ── Multiplayer — Join ────────────────────────────────────────

    private void startMultiplayerJoin(String code) {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);

        Client client = new Client(code);

        // Handle invalid session code from server
        client.setSessionErrorListener(msg -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Code");
            alert.setHeaderText(null);
            alert.setContentText("Session code \"" + code + "\" is invalid or has expired.");
            alert.showAndWait();
            showMenu();
        }));

        controller.setLocalClient(client);
        addBackToMenuButton(window, controller);
        window.show();
    }

    // ── Session code overlay (host waiting screen) ────────────────

    /**
     * After the host connects, a floating panel shows their session code.
     * It dismisses once the opponent joins (START message received).
     */
    private void showSessionCodeOverlay(GameWindow window, GameController controller,
                                         Board board, String ignoredCode) {
        // Connect to server first — server assigns a code
        Client client = new Client(); // SESSION:NEW

        // The overlay stage
        Stage overlayStage = new Stage();
        overlayStage.initOwner(primaryStage);
        overlayStage.setTitle("Waiting for Opponent");
        overlayStage.setResizable(false);
        overlayStage.setOnCloseRequest(e -> {
            client.closeConnection();
            showMenu();
        });

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 48, 36, 48));
        card.setBackground(new Background(new BackgroundFill(
                Color.web("#16213E"), new CornerRadii(12), Insets.EMPTY)));

        Text title = new Text("Share this code with your friend");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 15));
        title.setFill(Color.web("#CCCCCC"));

        Text codeText = new Text("…");
        codeText.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        codeText.setFill(Color.web("#FFD700"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FFD700", 0.6));
        glow.setRadius(18);
        codeText.setEffect(glow);

        Text waiting = new Text("Waiting for opponent to join…");
        waiting.setFont(Font.font("SansSerif", 13));
        waiting.setFill(Color.web("#8899AA"));

        // Copy button
        Button copyBtn = new Button("Copy Code");
        copyBtn.setStyle(
                "-fx-background-color:#0F3460;-fx-text-fill:white;" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-padding:8 20 8 20;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(codeText.getText());
            clipboard.setContent(content);
            copyBtn.setText("Copied ✓");
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color:#2C2C3E;-fx-text-fill:#AAAAAA;" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-padding:8 20 8 20;");
        cancelBtn.setOnAction(e -> {
            client.closeConnection();
            overlayStage.close();
            showMenu();
        });

        HBox btnRow = new HBox(12, copyBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, codeText, waiting, btnRow);

        Scene overlayScene = new Scene(card, 380, 240);
        overlayStage.setScene(overlayScene);
        overlayStage.show();

        // Receive the assigned session code from the server
        client.setSessionCodeListener(code -> Platform.runLater(() -> codeText.setText(code)));

        // Once both players are connected, the server sends START — dismiss overlay and play
        client.setStartListener(isLocalTurn -> Platform.runLater(() -> {
            overlayStage.close();
            controller.setLocalTurn(isLocalTurn);
            // Wire client AFTER start so listeners are set properly
            addBackToMenuButton(window, controller);
            window.show();
        }));

        // Wire remaining listeners through controller
        client.setWaitingListener(() -> Platform.runLater(() ->
                window.setStatusMessage("Waiting for opponent…")));
        client.setMoveListener(move -> {}); // will be overridden by controller.setLocalClient below

        controller.setLocalClient(client);
    }

    // ── Back to menu helper ───────────────────────────────────────

    /**
     * Adds a "Menu" button to the game sidebar that returns to the main menu.
     * Hooks into the GameWindow's sidebar via an extra action button.
     */
    private void addBackToMenuButton(GameWindow window, GameController controller) {
        window.setOnMenuRequested(this::showMenu);
    }

    public static void main(String[] args) {
        launch(args);
    }
}