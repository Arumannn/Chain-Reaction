package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.GridPanel;
import com.silent.treatment.chainreaction.view.MenuView;
import com.silent.treatment.chainreaction.view.SetupView;
import com.silent.treatment.chainreaction.view.GameOverView;
import com.silent.treatment.chainreaction.view.GameMenuView;
import com.silent.treatment.chainreaction.view.TutorialView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

public class MainApp extends Application {

    private Stage primaryStage;

    // Komponen UI dari Faris
    private Label turnLabel;
    private Circle turnIndicatorCircle;
    private VBox playersStatusBox;
    private GridPanel gameBoardView;
    private StackPane globalGameRoot; 
    private BorderPane gameRoot;

    @Override
    public void start(Stage stage) {
    
        this.primaryStage = stage;
        stage.setTitle("Silent Treatment - Chain Reaction");

        // Mulai dari Menu Utama (Logic Revan)
        showMainMenu();
        stage.show();
    }

    // --- BAGIAN 1: NAVIGASI (Dari Revan) ---

    private void showMainMenu() {
        MenuView menuView = new MenuView(
                () -> showGameSetup(), // New Game
                () -> System.exit(0),  // Exit
                this::startTutorial    // Tutorial (Callback baru)
        );
        primaryStage.setScene(new Scene(menuView, 450, 700));
        primaryStage.centerOnScreen();
    }

    private void showGameSetup() {
        SetupView setupView = new SetupView(
                config -> startGame(config),
                () -> showMainMenu()
        );
        primaryStage.setScene(new Scene(setupView, 500, 600));
        primaryStage.centerOnScreen();
    }

    // --- BAGIAN 2: LOGIKA GAME (Gabungan Logic Revan + UI Faris) ---

    private void startGame(SetupView.GameConfig config) {
        // 1. Inisialisasi Core
        GameManager gm = GameManager.getInstance();
        gm.initializeGame(config.mapType , config.players);

        // 2. Init Controller & View
        GameController controller = new GameController();
        gameBoardView = new GridPanel(gm.getBoard(), controller);

        // 3. Setup Layout Utama
        // SAYA UBAH NAMA VARIABEL 'root' MENJADI 'gameRoot' AGAR KONSISTEN
        gameRoot = new BorderPane();
        gameRoot.setStyle("-fx-background-color: #121212;");

        // --- Header Section ---
        gameRoot.setTop(createHeader(gm));

        // --- Center Section ---
        VBox centerContainer = new VBox(gameBoardView);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(20));
        gameRoot.setCenter(centerContainer);

        // --- Right Sidebar ---
        gameRoot.setRight(createPlayerSidebar(gm));

        // 4. Hubungkan Controller dengan UI
        controller.setOnTurnChanged(() -> updateGameInfo(gm));
        // [LOGIC GAME OVER]
        controller.setOnGameOver(() -> {
            Player winner = gm.getWinner();

            // Aksi saat tombol "VIEW BOARD" ditekan
            Runnable onCloseDialog = () -> {
                gameRoot.setEffect(null); // Hapus efek blur
                globalGameRoot.getChildren().removeIf(node -> node instanceof GameOverView);
;
            };

            // Buat View Game Over
            GameOverView gameOverView = new GameOverView(winner, this::showMainMenu, onCloseDialog);

            // Beri efek Blur ke game di belakangnya
            gameRoot.setEffect(new GaussianBlur(10));

            globalGameRoot.getChildren().add(gameOverView);
        });

        // Init Data Awal
        updateGameInfo(gm);

        globalGameRoot = new StackPane(gameRoot);

        // 5. Atur Scene (Ukuran dinamis menyesuaikan board)
        // Estimasi: (lebar board * 60px) + Sidebar(250) + Padding(100)
        double winWidth = (config.mapType.getWidth() * 60) + 350;
        double winHeight = (config.mapType.getHeight() * 60) + 150;

        if (winWidth < 800) winWidth = 800;
        if (winHeight < 600) winHeight = 600;

        // Gunakan gameRoot sebagai scene awal
        primaryStage.setScene(new Scene(globalGameRoot, winWidth, winHeight));
        primaryStage.centerOnScreen();
    }

    // --- LOGIC MENU PAUSE ---
    private void showInGameMenu() {
        // Aksi Resume: Hapus menu, hilangkan blur
        Runnable onResume = () -> {
            gameRoot.setEffect(null);
            globalGameRoot.getChildren().removeIf(node -> node instanceof GameMenuView);
        };

        // Aksi Exit: Kembali ke Main Menu
        Runnable onExit = this::showMainMenu;

        GameMenuView menuView = new GameMenuView(onResume, onExit);
        
        // Efek Blur ke Background
        gameRoot.setEffect(new GaussianBlur(10));
        
        globalGameRoot.getChildren().add(menuView);
    }

    // --- BAGIAN 3: HELPER UI (Dari Faris) ---

    private HBox createHeader(GameManager gm) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #333; -fx-border-width: 0 0 2 0;");

        HBox turnInfoBox = new HBox(10);
        turnInfoBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("CURRENT TURN:");
        titleLabel.setTextFill(Color.GRAY);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        turnIndicatorCircle = new Circle(8);
        turnIndicatorCircle.setStroke(Color.WHITE);

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        turnInfoBox.getChildren().addAll(titleLabel, turnIndicatorCircle, turnLabel);

        Pane spacer = new Pane();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Tombol Menu
        Button btnMenu = new Button("MENU");
        btnMenu.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-border-color: gray; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        btnMenu.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnMenu.setOnAction(e -> showInGameMenu()); // Trigger PopUp
        
        Pane leftSpacer = new Pane();
        javafx.scene.layout.HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        
        Pane rightSpacer = new Pane();
        javafx.scene.layout.HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(leftSpacer, turnInfoBox, rightSpacer, btnMenu);
        return header;
    }
    private void startTutorial() {
        TutorialView tutorialView = new TutorialView(this::showMainMenu);
        primaryStage.setScene(new Scene(tutorialView, 550, 750));
        primaryStage.centerOnScreen();
    }

    private VBox createPlayerSidebar(GameManager gm) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #181818; -fx-border-color: #333; -fx-border-width: 0 0 0 2;");
        
        Label title = new Label("PLAYER STATUS");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        
        playersStatusBox = new VBox(10); 
        
        sidebar.getChildren().addAll(title, new Separator(), playersStatusBox);
        return sidebar;
    }

    private void updateGameInfo(GameManager gm) {
        Player current = gm.getCurrentPlayer();

        turnLabel.setText(current.getName().toUpperCase());
        turnLabel.setTextFill(current.getColor());
        turnIndicatorCircle.setFill(current.getColor());
        turnIndicatorCircle.setEffect(new DropShadow(10, current.getColor()));

        gameBoardView.setBackgroundTheme(current.getColor());

        // Update Sidebar (Daftar Pemain)
        playersStatusBox.getChildren().clear();
        for (Player p : gm.getPlayers()) {
            HBox playerRow = new HBox(10);
            playerRow.setAlignment(Pos.CENTER_LEFT);
            playerRow.setPadding(new Insets(10));

            if (p.equals(current)) {
                playerRow.setStyle("-fx-background-color: #333; -fx-background-radius: 5;");
            }

            // Ikon Warna Pemain
            Circle pIcon = new Circle(5, p.getColor());
            VBox infoBox = new VBox(2);
            
            Label pName = new Label(p.getName());
            pName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            
            Label pStatus = new Label(); // Label untuk status Orb atau Game Over
            pStatus.setFont(Font.font("Arial", 11));

            // [LOGIKA BARU] Cek status Hidup/Mati
            if (p.isAlive()) {
                // Jika Hidup: Tampilkan nama normal & jumlah Orb
                pName.setTextFill(Color.LIGHTGRAY);
                
                int orbCount = gm.getPlayerOrbCount(p);
                pStatus.setText(orbCount + " Orbs");
                pStatus.setTextFill(Color.GRAY);
            } else {
                // Jika Mati: Tampilkan status Game Over
                pName.setTextFill(Color.DARKGRAY); // Nama diredupkan
                pIcon.setFill(Color.DARKGRAY);     // Icon warna diredupkan (opsional)
                
                pStatus.setText("GAME OVER");
                pStatus.setTextFill(Color.RED); // Tulisan merah agar tegas
                pStatus.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            }

            infoBox.getChildren().addAll(pName, pStatus);
            playerRow.getChildren().addAll(pIcon, infoBox);
            playersStatusBox.getChildren().add(playerRow);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}