package com.dama;

import com.dama.controller.GameController;
import com.dama.controller.GameType;
import com.dama.model.Board;
import com.dama.view.GameWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        GameController controller = new GameController(
                board,
                window,
                GameType.LOCAL_TWO_PAYERS
        );
        controller.setLocalClient(new com.dama.network.Client());
        window.setController(controller);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}