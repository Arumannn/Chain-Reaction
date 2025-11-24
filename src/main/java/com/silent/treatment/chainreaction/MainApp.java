package com.silent.treatment.chainreaction;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.GridPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

public class MainApp extends Application {

    private Label turnLabel;
    private Circle turnIndicatorCircle;
    private VBox playersStatusBox;
    private GridPanel gameBoardView;

    @Override
    public void start(Stage stage) {
        // 1. Inisialisasi Game (Core)
        GameManager gm = GameManager.getInstance();
        gm.initializeGame(10, 10, 3); // Board 9x6, 2 Players

        // 2. Inisialisasi Controller
        GameController controller = new GameController();

        gameBoardView = new GridPanel(gm.getBoard(), controller);

        // 3. Setup Layout Utama (BorderPane)
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;"); // Dark Theme Background

        // --- Header Section (Info Giliran) ---
        HBox header = createHeader(gm);
        root.setTop(header);
        
        // --- Center Section (Game Board) ---
        // Bungkus GridPanel dalam VBox agar bisa ditengah-tengah
        VBox centerContainer = new VBox(gameBoardView);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(20));
        root.setCenter(centerContainer);

        // --- Right Sidebar (Player Stats) ---
        VBox sidebar = createPlayerSidebar(gm);
        root.setRight(sidebar);

        // 4. Hubungkan Controller dengan UI Header
        controller.setOnTurnChanged(() -> updateGameInfo(gm));

        // Init Data Awal
        updateGameInfo(gm);

        // Setup Scene
        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("Silent Treatment - Chain Reaction");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader(GameManager gm) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #333; -fx-border-width: 0 0 2 0;");

        Label titleLabel = new Label("CURRENT TURN:");
        titleLabel.setTextFill(Color.GRAY);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Lingkaran warna indikator
        turnIndicatorCircle = new Circle(8);
        turnIndicatorCircle.setStroke(Color.WHITE);

        // Teks Nama Pemain
        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        header.getChildren().addAll(titleLabel, turnIndicatorCircle, turnLabel);
        return header;
    }

    private VBox createPlayerSidebar(GameManager gm) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #181818; -fx-border-color: #333; -fx-border-width: 0 0 0 2;");
        
        Label title = new Label("PLAYER STATUS");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        
        playersStatusBox = new VBox(10); // Tempat item player dinamis yang akan diisi updateGameInfo
        
        sidebar.getChildren().addAll(title, new Separator(), playersStatusBox);
        return sidebar;
    }

    // Method utama yang dipanggil setiap kali giliran berganti untuk update semua info UI
    private void updateGameInfo(GameManager gm) {
        Player current = gm.getCurrentPlayer();
        
        // 1. Update Header Info
        turnLabel.setText(current.getName().toUpperCase());
        turnLabel.setTextFill(current.getColor());
        turnIndicatorCircle.setFill(current.getColor());
        turnIndicatorCircle.setEffect(new DropShadow(10, current.getColor()));

        // 2. Update Grid Background Color (Fitur Ganti Warna Background)
        gameBoardView.setBackgroundTheme(current.getColor());

        // 3. Update Sidebar (List Pemain & Orb Count)
        playersStatusBox.getChildren().clear();
        for (Player p : gm.getPlayers()) {
            HBox playerRow = new HBox(10);
            playerRow.setAlignment(Pos.CENTER_LEFT);
            playerRow.setPadding(new Insets(10));
            
            // Highlight pemain yang sedang jalan di sidebar dengan background kotak
            if (p.equals(current)) {
                playerRow.setStyle("-fx-background-color: #333; -fx-background-radius: 5;");
            }

            Circle pIcon = new Circle(5, p.getColor());
            
            VBox infoBox = new VBox(2);
            Label pName = new Label(p.getName());
            pName.setTextFill(Color.LIGHTGRAY);
            pName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            
            // Hitung total orb pemain
            int orbCount = gm.getPlayerOrbCount(p);
            Label pOrbs = new Label(orbCount + " Orbs");
            pOrbs.setTextFill(Color.GRAY);
            pOrbs.setFont(Font.font("Arial", 11));

            infoBox.getChildren().addAll(pName, pOrbs);
            playerRow.getChildren().addAll(pIcon, infoBox);
            
            playersStatusBox.getChildren().add(playerRow);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}   