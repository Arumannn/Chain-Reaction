package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.GridPanel;
import com.silent.treatment.chainreaction.view.MenuView;
import com.silent.treatment.chainreaction.view.SetupView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.lang.reflect.Field;
import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Silent Treatment - Chain Reaction");
        // Alih-alih langsung masuk game, kita tampilkan menu dulu
        showMainMenu();

        stage.show();
    }

    // --- BAGIAN 1: MENU & SETUP (Navigasi) ---

    private void showMainMenu() {
        MenuView menuView = new MenuView(
                () -> showGameSetup(),      // Aksi jika klik New Game
                () -> System.exit(0)        // Aksi jika klik Exit
        );
        primaryStage.setScene(new Scene(menuView, 450, 700));
        primaryStage.centerOnScreen();
    }

    private void showGameSetup() {
        SetupView setupView = new SetupView(
                config -> startGame(config), // Aksi jika klik Start Game
                () -> showMainMenu()         // Aksi jika klik Back
        );
        primaryStage.setScene(new Scene(setupView, 500, 600));
        primaryStage.centerOnScreen();
    }

    // --- BAGIAN 2: LOGIKA GAME ASLI (Dipindah kesini) ---
    
    private void startGame(SetupView.GameConfig config) {
        // 1. Inisialisasi Game (Core)
        GameManager gm = GameManager.getInstance();

        // Setup Custom (Ukuran Board & Jumlah Pemain)
        gm.initializeGame(config.width, config.height, config.players.size());

        // HACK: Update data player (Warna & Nama) sesuai input user
        // (Dilakukan disini agar tidak perlu mengubah GameManager dan merusak kode teman)
        injectCustomPlayers(gm, config.players);

        // 2. Inisialisasi Controller (SAMA SEPERTI ASLINYA)
        GameController controller = new GameController();

        // 3. Inisialisasi View (SAMA SEPERTI ASLINYA)
        GridPanel gameBoardView = new GridPanel(gm.getBoard(), controller);

        // Root Layout (SAMA SEPERTI ASLINYA)
        StackPane root = new StackPane(gameBoardView);
        root.setStyle("-fx-background-color: #121212; -fx-padding: 20;");

        // Ganti Scene ke Game
        // Ukuran window dihitung otomatis biar pas dengan board
        double width = (config.width * 60) + 100;
        double height = (config.height * 60) + 100;

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.centerOnScreen();
    }

    private void injectCustomPlayers(GameManager gm, List<Player> newPlayers) {
        try {
            Field field = GameManager.class.getDeclaredField("players");
            field.setAccessible(true);
            field.set(gm, newPlayers);
        } catch (Exception e) {
            System.err.println("Gagal update player: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}