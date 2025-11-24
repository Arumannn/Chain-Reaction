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

    @Override
    public void start(Stage stage) {
        // 1. Init Core
        GameManager gm = GameManager.getInstance();
        gm.initializeGame(9, 6, 2); // 2 Players

        // 2. Init Controller & View
        GameController controller = new GameController();
        GridPanel gameBoardView = new GridPanel(gm.getBoard(), controller);

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

        // 4. Hubungkan Controller dengan UI Header
        controller.setOnTurnChanged(() -> updateHeaderInfo(gm));

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

        Label titleLabel = new Label("TURN:");
        titleLabel.setTextFill(Color.GRAY);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Lingkaran warna indikator
        turnIndicatorCircle = new Circle(10);
        turnIndicatorCircle.setStroke(Color.WHITE);
        turnIndicatorCircle.setStrokeWidth(1);

        // Teks Nama Pemain
        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        header.getChildren().addAll(titleLabel, turnIndicatorCircle, turnLabel);
        
        // Set info awal
        updateHeaderInfo(gm);
        
        return header;
    }

    private void updateHeaderInfo(GameManager gm) {
        Player current = gm.getCurrentPlayer();
        turnLabel.setText(current.getName().toUpperCase());
        turnLabel.setTextFill(current.getColor());
        turnIndicatorCircle.setFill(current.getColor());
        
        // Efek visual sederhana pada lingkaran indikator
        turnIndicatorCircle.setEffect(new javafx.scene.effect.DropShadow(10, current.getColor()));
    }

    public static void main(String[] args) {
        launch();
    }
}