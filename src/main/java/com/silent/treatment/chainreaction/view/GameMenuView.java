package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.core.SoundManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class GameMenuView extends StackPane {

    public GameMenuView(Runnable onResume, Runnable onExit) {
        // 1. Overlay Gelap (Semi-transparan)
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // 2. Panel Menu Box
        VBox menuBox = new VBox(20);
        menuBox.setMaxWidth(300);
        // menuBox.setMaxHeight(250);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));

        // Styling Panel (Neon Border)
        menuBox.setStyle(
                "-fx-background-color: #1a1a1a;" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 4;" +
                "-fx-border-radius: 0;" +
                "-fx-background-radius: 0;"
        );
        menuBox.getStyleClass().add("dialog-panel");

        // 3. Header "MENU"
        Label lblTitle = new Label("PAUSED");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Impact", 40));
        lblTitle.setEffect(new DropShadow(5, Color.CYAN));

                // --- Audio Toggles (Mini) ---
        HBox audioBox = new HBox(10);
        audioBox.setAlignment(Pos.CENTER);
        
        Button bgmBtn = createMiniToggle("BGM", SoundManager.getInstance().isBgmMuted());
        bgmBtn.setOnAction(e -> {
            SoundManager.getInstance().toggleBGM();
            updateMiniToggleStyle(bgmBtn, SoundManager.getInstance().isBgmMuted());
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
        });
        
        Button sfxBtn = createMiniToggle("SFX", SoundManager.getInstance().isSfxMuted());
        sfxBtn.setOnAction(e -> {
            SoundManager.getInstance().toggleSFX();
            updateMiniToggleStyle(sfxBtn, SoundManager.getInstance().isSfxMuted());
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
        });
        
        audioBox.getChildren().addAll(bgmBtn, sfxBtn);

        // 4. Tombol-tombol
        StackPane btnResume = createStyledButton("RESUME", Color.LIME, onResume);
        StackPane btnExit = createStyledButton("EXIT TO MENU", Color.RED, onExit);

        menuBox.getChildren().addAll(lblTitle, audioBox, btnResume, btnExit);
        this.getChildren().add(menuBox);
    }

    private Button createMiniToggle(String text, boolean isMuted) {
        Button btn = new Button(text);
        btn.setPrefWidth(80);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        updateMiniToggleStyle(btn, isMuted);
        return btn;
    }

    private void updateMiniToggleStyle(Button btn, boolean isMuted) {
        if (isMuted) {
            btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #888888; -fx-border-color: #888888; -fx-border-radius: 0; -fx-border-width: 3; -fx-cursor: hand;");
            btn.getStyleClass().add("off-state");
        } else {
            btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #00ffff; -fx-border-color: #00ffff; -fx-border-radius: 0; -fx-border-width: 3; -fx-cursor: hand;");
            btn.getStyleClass().add("on-state");
        }
    }

    private StackPane createStyledButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFill(color);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Rectangle border = new Rectangle(200, 45);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(3);
        border.setArcWidth(0);
        border.setArcHeight(0);

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> action.run());
        btn.setCursor(javafx.scene.Cursor.HAND);

        // Hover Effect
        btn.setOnMouseEntered(e -> {
            border.setFill(color.deriveColor(0, 1, 1, 0.2));
            txt.setFill(Color.WHITE);
        });
        btn.setOnMouseExited(e -> {
            border.setFill(null);
            txt.setFill(color);
        });

        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });

        return btn;
    }
}