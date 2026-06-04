package com.dama;

import com.dama.controller.MenuController;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private MenuController menuController;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.menuController = new MenuController(primaryStage);
        this.menuController.showMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}