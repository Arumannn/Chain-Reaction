package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.view.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Delegasikan semua logika ke SceneManager
        SceneManager.getInstance().setStage(stage);
        
        // Panggil Menu Utama
        SceneManager.getInstance().showMainMenu();
        
        stage.setTitle("Silent Treatment - Chain Reaction");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}