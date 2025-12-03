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
        // 1. Setup Game Manager (Board Kecil 4x4 cukup untuk tutorial)
        GameManager gm = GameManager.getInstance();
        MapType mapType = MapType.SMALL;
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
                "-fx-background-color: rgba(30, 30, 30, 0.9);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: cyan;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 255, 255, 0.3), 10, 0, 0, 0);"
        );

        // 3. Setup Controller
        TutorialController tutorialController = new TutorialController(
                instructionLabel::setText,
                onExit
        );

        // 4. Setup GridPanel
        GridPanel gameBoardView = new GridPanel(gm.getBoard(), tutorialController);
        gameBoardView.setBackgroundTheme(Color.RED);

        // Listener Warna Board
        tutorialController.setOnTurnChanged(() -> {
            // Karena ini tutorial, kita buat warna board merah saat user main, dan hijau saat AI main
            // Tapi karena controller kita handle manual, kita bisa set statis atau ambil dari GM
            // Untuk simpel: Biarkan merah (fokus ke player)
        });

        // 5. Layout
        this.setStyle("-fx-background-color: #121212;");

        // Header
        Label lblTitle = new Label("HOW TO PLAY");
        lblTitle.setTextFill(Color.GRAY);
        lblTitle.setFont(Font.font("Impact", 24));

        StackPane btnSkip = createStyledButton("SKIP", Color.RED, onExit);
        btnSkip.setMaxWidth(80); btnSkip.setMaxHeight(30);

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
        border.setFill(null); border.setStroke(color); border.setArcWidth(10); border.setArcHeight(10);
        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }
}