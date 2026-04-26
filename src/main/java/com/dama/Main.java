package com.dama;

import com.dama.model.Board;
import com.dama.model.MockBoardModel;
import com.dama.view.GameWindow;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Board board = new Board();
        GameWindow window = new GameWindow(primaryStage, board);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}