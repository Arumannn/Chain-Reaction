package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.controller.TutorialController;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.SoundManager;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.model.MapType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

public class TutorialView extends BorderPane {

    public TutorialView(Runnable onExit) {
        // 1. Setup Game Manager (Board Kecil 6x9 untuk tutorial)
        GameManager gm = GameManager.getInstance();
        MapType mapType = MapType.SMALL; // 9x6 board (landscape)
        List<Player> tutorialPlayers = new ArrayList<>();
        tutorialPlayers.add(new Player("YOU", Color.RED));
        tutorialPlayers.add(new Player("ENEMY", Color.LIMEGREEN));
        gm.initializeGame(mapType, tutorialPlayers);

        // 2. Setup Instruksi (Kotak Melayang)
        Label instructionLabel = new Label("Initializing...");
        instructionLabel.setTextFill(Color.WHITE);
        instructionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        instructionLabel.setWrapText(true);
        instructionLabel.setAlignment(Pos.CENTER);
        instructionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        instructionLabel.setPrefWidth(320);
        instructionLabel.setPadding(new Insets(20));
        instructionLabel.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #00ffff;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 0;");
        instructionLabel.getStyleClass().add("panel-secondary");

        // 3. Setup Controller
        TutorialController tutorialController = new TutorialController(
                instructionLabel::setText,
                onExit);

        // 4. Setup GridPanel
        GridPanel gameBoardView = new GridPanel(gm.getBoard(), tutorialController);
        gameBoardView.setBackgroundTheme(Color.RED);

        // Listener Warna Board + animasi ledakan
        tutorialController.setOnTurnChanged(() -> {
            Player currentPlayer = gm.getCurrentPlayer();
            if (currentPlayer != null) {
                Color themeColor = currentPlayer.getColor();
                gameBoardView.setBackgroundTheme(themeColor);
            }
        });
        tutorialController.setOnAnimationStart(gameBoardView::startAnimationProcessing);

        // 5. Layout
        this.setStyle("-fx-background-color: #0a0a0a;");

        // Header
        Label lblTitle = new Label("HOW TO PLAY");
        lblTitle.setTextFill(Color.GRAY);
        lblTitle.setFont(Font.font("Impact", 24));

        StackPane btnSkip = createStyledButton("SKIP", Color.RED, onExit);
        btnSkip.setMaxWidth(80);
        btnSkip.setMaxHeight(30);

        HBox header = new HBox(20, lblTitle, btnSkip);
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(20));
        this.setTop(header);

        // Center
        VBox centerBox = new VBox(30, gameBoardView, instructionLabel);
        centerBox.setAlignment(Pos.CENTER);
        this.setCenter(centerBox);
    }

    private StackPane createStyledButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFill(color);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        javafx.scene.shape.Rectangle border = new javafx.scene.shape.Rectangle(80, 30);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(3);
        border.setArcWidth(0);
        border.setArcHeight(0);
        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }
}