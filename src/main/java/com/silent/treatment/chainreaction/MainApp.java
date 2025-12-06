package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.core.SoundManager;
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
        SoundManager.getInstance().playBGM(SoundManager.BGM_MAIN);

        stage.setTitle("Silent Treatment - Chain Reaction");
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}