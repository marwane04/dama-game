package com.dama;

import com.dama.controller.GameController;
import com.dama.controller.GameType;
import com.dama.model.Board;
import com.dama.network.Client;
import com.dama.view.GameWindow;
import com.dama.view.MenuView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showMenu();
    }

    private void showMenu() {
        MenuView menu = new MenuView(primaryStage);
        menu.setMenuListener(new MenuView.MenuListener() {

            @Override
            public void onSinglePlayer() {
                startSinglePlayer();
            }

            @Override
            public void onPvP() {
                startPvP();
            }

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

    private void startSinglePlayer() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.LOCAL_VS_AI);
        window.setController(controller);
        window.setOnMenuRequested(this::showMenu);
        window.show();
    }

    private void startPvP() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.LOCAL_TWO_PAYERS);
        window.setController(controller);
        window.setOnMenuRequested(this::showMenu);
        window.show();
    }

    private void startMultiplayerHost() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);

        Client client = new Client();

        // Tell MenuView to show the "waiting for opponent" card inline
        MenuView menu = new MenuView(primaryStage);
        menu.setMenuListener(new MenuView.MenuListener() {
            @Override public void onSinglePlayer()            { startSinglePlayer(); }
            @Override public void onPvP()                     { startPvP(); }
            @Override public void onMultiplayerHost()         { startMultiplayerHost(); }
            @Override public void onMultiplayerJoin(String c) { startMultiplayerJoin(c); }
        });
        menu.showHostWaiting(client, () -> {
            // Called when opponent connects — switch to game
            controller.setLocalTurn(true);
            window.setOnMenuRequested(this::showMenu);
            window.show();
            controller.setLocalClient(client);
        }, this::showMenu);
    }

    private void startMultiplayerJoin(String code) {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);

        Client client = new Client(code);

        client.setSessionErrorListener(msg -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Code");
            alert.setHeaderText(null);
            alert.setContentText("Code \"" + code + "\" is invalid or expired.");
            alert.showAndWait();
            showMenu();
        }));

        // Show inline "connecting..." card while waiting for START
        MenuView menu = new MenuView(primaryStage);
        menu.setMenuListener(new MenuView.MenuListener() {
            @Override public void onSinglePlayer()            { startSinglePlayer(); }
            @Override public void onPvP()                     { startPvP(); }
            @Override public void onMultiplayerHost()         { startMultiplayerHost(); }
            @Override public void onMultiplayerJoin(String c) { startMultiplayerJoin(c); }
        });
        menu.showJoinWaiting(code, client, isLocalTurn -> {
            controller.setLocalTurn(isLocalTurn);
            window.setOnMenuRequested(this::showMenu);
            window.show();
            controller.setLocalClient(client);
        }, this::showMenu);
    }

    public static void main(String[] args) {
        launch(args);
    }
}