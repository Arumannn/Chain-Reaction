package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.controller.AIController;
import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.SoundManager;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.SetupView.GameConfig;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private static final String STYLESHEET_PATH = "/pixel-style.css";

    private static SceneManager instance;
    private Stage stage;
    private StackPane globalGameRoot; // Root untuk Game + Overlay (Pause/GameOver)
    private GameLayout gameLayout; // UI utama Game (Header + Board + Sidebar)
    private PauseTransition currentAiDelay; // Untuk tracking AI delay agar bisa di-stop

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null)
            instance = new SceneManager();
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // --- NAVIGASI SCENE ---

    public void showMainMenu() {
        resetGameState();
        // [Fix] Play BGM again because resetGameState stops it
        SoundManager.getInstance().playBGM(SoundManager.BGM_MAIN);

        MenuView menuView = new MenuView(
                this::showGameSetup,
                () -> System.exit(0),
                this::showTutorial,
                null);
        // [Modified] Use adaptive scene size & reuse scene
        if (stage.getScene() == null) {
            Scene scene = new Scene((Parent) menuView);
            scene.getStylesheets().add(getClass().getResource(STYLESHEET_PATH).toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot((Parent) menuView);
        }
    }

    public void showGameSetup() {
        SetupView setupView = new SetupView(
                this::startGame,
                this::showMainMenu);
        // [Modified] Use adaptive scene size & reuse scene
        if (stage.getScene() == null) {
            Scene scene = new Scene((Parent) setupView);
            scene.getStylesheets().add(getClass().getResource(STYLESHEET_PATH).toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot((Parent) setupView);
        }
    }

    public void showTutorial() {
        TutorialView tutorialView = new TutorialView(this::showMainMenu);
        // [Modified] Use adaptive scene size & reuse scene
        if (stage.getScene() == null) {
            Scene scene = new Scene((Parent) tutorialView);
            scene.getStylesheets().add(getClass().getResource(STYLESHEET_PATH).toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot((Parent) tutorialView);
        }
    }

    // --- LOGIKA UTAMA GAME (Dipindahkan dari MainApp) ---

    public void startGame(GameConfig config) {
        // 1. Init Core
        GameManager gm = GameManager.getInstance();
        gm.initializeGame(config.getMapType(), config.getPlayers());
        SoundManager.getInstance().playBGM(SoundManager.BGM_MAIN);

        // 2. Init Controller & Grid
        GameController controller = new GameController();
        GridPanel gridPanel = new GridPanel(gm.getBoard(), controller);

        // 3. Init Layout UI (Menggunakan class GameLayout baru)
        gameLayout = new GameLayout(gm, gridPanel, this::showInGameMenu);

        // StackPane untuk menampung GameLayout dan Overlay (Pause/GameOver)
        globalGameRoot = new StackPane(gameLayout);

        // 4. Setup AI
        List<AIController> aiControllers = setupAI(config, controller);

        // 5. Wiring Listeners
        gm.getBoard().attachGlobalObserver(cell -> Platform.runLater(() -> {
            // [Fix] Cek eliminasi secara real-time setiap ada perubahan cell
            gm.checkEliminations();
            gameLayout.updateGameInfo();

            // [Fix] Cek Win Condition segera jika terjadi di tengah animasi
            Player winner = gm.checkWinner();
            if (winner != null) {
                gridPanel.clearAnimations(); // Stop chaos
                showGameOverDialog(winner);
            }
        }));

        Runnable handleTurnChanged = () -> {
            gameLayout.updateGameInfo();
            handleAITurn(gm, aiControllers, gridPanel);
        };

        controller.setOnTurnChanged(handleTurnChanged);
        controller.setOnAnimationStart(gridPanel::startAnimationProcessing);
        controller.setOnGameStateUpdated(gameLayout::updateGameInfo);
        controller.setOnGameOver(() -> showGameOverDialog(gm.getWinner()));
        controller.setOnHumansDefeated(() -> showGameOverDialog(gm.getCurrentPlayer()));

        // Jalankan state awal
        handleTurnChanged.run();

        // 6. Set Scene
        // [Modified] Use adaptive scene size (Maximized) & reuse scene
        if (stage.getScene() == null) {
            Scene scene = new Scene(globalGameRoot);
            scene.getStylesheets().add(getClass().getResource(STYLESHEET_PATH).toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(globalGameRoot);
        }
    }

    // --- HELPER METHODS ---

    private List<AIController> setupAI(GameConfig config, GameController controller) {
        List<AIController> ais = new ArrayList<>();
        for (int i = 0; i < config.getPlayers().size(); i++) {
            if (config.getIsBot().get(i)) {
                ais.add(new AIController(controller, config.getBotDifficulty()));
            } else {
                ais.add(null);
            }
        }
        return ais;
    }

    private void handleAITurn(GameManager gm, List<AIController> ais, GridPanel gridPanel) {
        int idx = gm.getPlayers().indexOf(gm.getCurrentPlayer());
        AIController ai = ais.get(idx);

        if (ai != null) {
            gridPanel.setPlayerInteractionEnabled(false);
            Platform.runLater(() -> {
                if (currentAiDelay != null) {
                    currentAiDelay.stop();
                }
                currentAiDelay = new PauseTransition(Duration.millis(1000));
                currentAiDelay.setOnFinished(e -> {
                    if (GameManager.getInstance().isGameOver())
                        return;
                    ai.performMove();
                });
                currentAiDelay.play();
            });
        } else {
            gridPanel.setPlayerInteractionEnabled(true);
        }
    }

    private void resetGameState() {
        if (currentAiDelay != null) {
            currentAiDelay.stop();
            currentAiDelay = null;
        }
        if (gameLayout != null) {
            gameLayout.cleanup();
        }

        SoundManager.getInstance().stopBGM();
        ExplosionQueue.getInstance().clear();
        GameManager.getInstance().reset();
    }

    // --- OVERLAYS ---

    private void showInGameMenu() {
        GameMenuView menuView = new GameMenuView(
                () -> { // Resume
                    gameLayout.setEffect(null);
                    globalGameRoot.getChildren().removeIf(GameMenuView.class::isInstance);
                },
                this::showMainMenu // Exit
        );
        gameLayout.setEffect(new GaussianBlur(10));
        globalGameRoot.getChildren().add(menuView);
    }

    private void showGameOverDialog(Player winner) {
        if (winner == null)
            return;
        globalGameRoot.getChildren().removeIf(GameOverView.class::isInstance);

        GameOverView view = new GameOverView(winner, this::showMainMenu, () -> {
            gameLayout.setEffect(null);
            globalGameRoot.getChildren().removeIf(GameOverView.class::isInstance);
        });

        gameLayout.setEffect(new GaussianBlur(10));
        globalGameRoot.getChildren().add(view);
    }
}