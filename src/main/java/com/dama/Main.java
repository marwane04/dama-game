package com.dama;

import com.dama.model.MockBoardModel;
import com.dama.view.GameWindow;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MockBoardModel model = new MockBoardModel();
        GameWindow window = new GameWindow(primaryStage, model);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}