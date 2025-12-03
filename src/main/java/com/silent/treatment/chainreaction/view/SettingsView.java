package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.core.SoundManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.animation.AnimationTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SettingsView extends StackPane {

    private final Runnable onBack;

    public SettingsView(Runnable onBack) {
        this.onBack = onBack;

        // Background
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: #121212;");
        createFloatingAtoms(backgroundLayer);
        this.getChildren().add(backgroundLayer);

        // Main Panel
        VBox mainPanel = new VBox(30);
        mainPanel.setMaxWidth(500);
        mainPanel.setMaxHeight(400);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setStyle(
                "-fx-background-color: rgba(30, 30, 30, 0.8);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;"
        );
        mainPanel.setEffect(new DropShadow(20, Color.BLACK));

        // Title
        Text title = new Text("SETTINGS");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 40));
        title.setEffect(new DropShadow(10, Color.MAGENTA));

        // --- Audio Settings ---
        VBox audioBox = new VBox(20);
        audioBox.setAlignment(Pos.CENTER);

        // BGM Toggle
        HBox bgmRow = createToggleRow("Background Music", SoundManager.getInstance().isBgmMuted(), () -> {
            SoundManager.getInstance().toggleBGM();
        });

        // SFX Toggle
        HBox sfxRow = createToggleRow("Sound Effects", SoundManager.getInstance().isSfxMuted(), () -> {
            SoundManager.getInstance().toggleSFX();
        });

        audioBox.getChildren().addAll(bgmRow, sfxRow);

        // Back Button
        StackPane btnBack = createStyledButton("BACK", Color.GRAY, onBack);

        mainPanel.getChildren().addAll(title, audioBox, btnBack);
        this.getChildren().add(mainPanel);
    }

    private HBox createToggleRow(String label, boolean isMuted, Runnable onToggle) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);

        Label lbl = new Label(label);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setPrefWidth(200);

        Button toggleBtn = new Button(isMuted ? "OFF" : "ON");
        toggleBtn.setPrefWidth(80);
        updateButtonStyle(toggleBtn, isMuted);

        toggleBtn.setOnAction(e -> {
            onToggle.run();
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            
            // Cek state baru (kebalikan dari isMuted awal)
            boolean newState = toggleBtn.getText().equals("ON"); // Jika ON berarti mau dimatikan
            updateButtonStyle(toggleBtn, newState);
            toggleBtn.setText(newState ? "OFF" : "ON");
        });

        row.getChildren().addAll(lbl, toggleBtn);
        return row;
    }

    private void updateButtonStyle(Button btn, boolean isMuted) {
        if (isMuted) {
            btn.setStyle("-fx-background-color: #333; -fx-text-fill: gray; -fx-border-color: gray; -fx-background-radius: 5; -fx-border-radius: 5; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #333; -fx-text-fill: lime; -fx-border-color: lime; -fx-background-radius: 5; -fx-border-radius: 5; -fx-cursor: hand;");
        }
    }

    private StackPane createStyledButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFill(color);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Rectangle border = new Rectangle(120, 40);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(2);
        border.setArcWidth(10);
        border.setArcHeight(10);

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        btn.setOnMouseEntered(e -> {
            border.setFill(color);
            txt.setFill(Color.BLACK);
        });
        btn.setOnMouseExited(e -> {
            border.setFill(null);
            txt.setFill(color);
        });

        return btn;
    }

    private void createFloatingAtoms(Pane pane) {
        Random rand = new Random();
        List<Circle> atoms = new ArrayList<>();
        Color[] colors = {Color.RED, Color.CYAN, Color.LIME, Color.YELLOW};
        for (int i = 0; i < 20; i++) {
            Circle c = new Circle(rand.nextInt(4) + 2, colors[rand.nextInt(4)]);
            c.setOpacity(0.2);
            c.setTranslateX(rand.nextInt(800)); c.setTranslateY(rand.nextInt(600));
            pane.getChildren().add(c);
            atoms.add(c);
        }
        new AnimationTimer() {
            public void handle(long now) {
                for (Circle c : atoms) {
                    c.setTranslateY(c.getTranslateY() - 0.3);
                    if (c.getTranslateY() < 0) c.setTranslateY(pane.getHeight() + 10);
                }
            }
        }.start();
    }
}