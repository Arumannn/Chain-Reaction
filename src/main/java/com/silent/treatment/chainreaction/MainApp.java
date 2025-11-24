package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.view.GridPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Inisialisasi Game (Core)
        GameManager gm = GameManager.getInstance();
        gm.initializeGame(10, 10, 3); // Board 9x6, 2 Players

        // 2. Inisialisasi Controller
        GameController controller = new GameController();

        // 3. Inisialisasi View
        GridPanel gameBoardView = new GridPanel(gm.getBoard(), controller);

        // Root Layout
        StackPane root = new StackPane(gameBoardView);
        root.setStyle("-fx-background-color: #121212; -fx-padding: 20;");

        Scene scene = new Scene(root, 1280, 720);

        stage.setTitle("Silent Treatment - Chain Reaction");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}   