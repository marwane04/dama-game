package com.dama.controller;

import com.dama.model.Board;
import com.dama.network.Client;
import com.dama.view.GameWindow;
import com.dama.view.MenuView;
import com.dama.view.SessionOverlayView;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MenuController {

    private final Stage primaryStage;

    public MenuController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showMenu() {
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
        addBackToMenuButton(window);
        window.show();
    }

    private void startPvP() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.LOCAL_TWO_PAYERS);
        window.setController(controller);
        addBackToMenuButton(window);
        window.show();
    }

    private void startMultiplayerHost() {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);
        window.setController(controller);

        showSessionCodeOverlay(window, controller);
    }

    private void startMultiplayerJoin(String code) {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(board, window, GameType.ONLINE);
        window.setController(controller);

        Client client = new Client(code);
        client.setSessionErrorListener(msg -> Platform.runLater(() -> {
            client.closeConnection();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Code");
            alert.setHeaderText(null);
            alert.setContentText("Session code \"" + code + "\" is invalid or has expired.");
            alert.showAndWait();
            showMenu();
        }));

        controller.setLocalClient(client);
        addBackToMenuButton(window);
        window.show();
    }

    private void showSessionCodeOverlay(GameWindow window, GameController controller) {
        Client client = new Client();
        SessionOverlayView overlay = new SessionOverlayView(primaryStage);

        overlay.setOnClose(() -> {
            client.closeConnection();
            showMenu();
        });
        overlay.show();

        client.setSessionCodeListener(code -> Platform.runLater(() -> overlay.setCode(code)));
        controller.setOnlineStartListener(isLocalTurn -> Platform.runLater(() -> {
            overlay.close();
            addBackToMenuButton(window);
            window.show();
        }));
        client.setWaitingListener(() -> Platform.runLater(() -> window.setStatusMessage("Waiting for opponent...")));
        client.setMoveListener(move -> {});

        controller.setLocalClient(client);
    }

    private void addBackToMenuButton(GameWindow window) {
        window.setOnMenuRequested(this::showMenu);
    }
}
