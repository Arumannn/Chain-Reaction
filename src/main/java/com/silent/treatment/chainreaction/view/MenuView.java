package com.silent.treatment.chainreaction.view;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.silent.treatment.chainreaction.core.SoundManager;

public class MenuView extends StackPane {

    private final Runnable onNewGame;
    private final Runnable onExit;
    private final Runnable onTutorial;
    private final Runnable onVsAI;

    private final List<StackPane> menuButtons = new ArrayList<>();
    private int selectedIndex = 0;

    private final VBox menuContainer;

    public MenuView(Runnable onNewGame, Runnable onExit, Runnable onTutorial, Runnable onVsAI) {
        this.onNewGame = onNewGame;
        this.onExit = onExit;
        this.onTutorial = onTutorial;
        this.onVsAI = onVsAI;

        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: #0a0a0a;");
        createFloatingAtoms(backgroundLayer);
        this.getChildren().add(backgroundLayer);

        Text title = new Text("CHAIN\nREACTION");
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setEffect(new DropShadow(20, Color.CYAN));

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);

        menuButtons.add(createButton("NEW GAME", Color.LIME, onNewGame));
        menuButtons.add(createButton("HOW TO PLAY", Color.YELLOW, onTutorial));
        menuButtons.add(createButton("SETTING", Color.MAGENTA, this::openSettings));
        menuButtons.add(createButton("EXIT", Color.RED, this::showExitConfirmation));

        menuBox.getChildren().addAll(menuButtons);

        menuContainer = new VBox(40, title, menuBox);
        menuContainer.setAlignment(Pos.CENTER);
        this.getChildren().add(menuContainer);

        this.setFocusTraversable(true);
        this.setOnKeyPressed(event -> {
            if (this.getChildren().size() > 2)
                return;

            if (event.getCode() == KeyCode.UP) {
                navigate(-1);
            } else if (event.getCode() == KeyCode.DOWN) {
                navigate(1);
            } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                triggerSelectedButton();
            }
        });
        updateSelectionVisuals();
    }

    private void openSettings() {
        // Dapatkan stage dari scene saat ini
        if (getScene() != null && getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) getScene().getWindow();
            
            // Buat SettingsView dengan callback Back yang mengembalikan scene ke MenuView ini
            SettingsView settingsView = new SettingsView(() -> {
                stage.getScene().setRoot(this); // Kembali ke menu ini
            });
            
            stage.getScene().setRoot(settingsView);
        }
    }

    private void showExitConfirmation() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlay.getStyleClass().add("dialog-overlay");

        VBox dialog = new VBox(20);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(250);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPadding(new javafx.geometry.Insets(30));

        dialog.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-border-color: #ff0000;" +
                        "-fx-border-width: 4;" +
                        "-fx-border-radius: 0;" +
                        "-fx-background-radius: 0;");
        dialog.getStyleClass().add("dialog-panel");
        dialog.setEffect(new DropShadow(30, Color.BLACK));

        Label lblTitle = new Label("EXIT GAME?");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Impact", 30));

        Label lblDesc = new Label("Are you sure you want to quit?");
        lblDesc.setTextFill(Color.LIGHTGRAY);
        lblDesc.setFont(Font.font("Arial", 16));

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        StackPane btnYes = createMiniButton("YES", Color.RED, onExit);
        StackPane btnNo = createMiniButton("CANCEL", Color.GRAY, () -> {
            this.getChildren().remove(overlay);
            menuContainer.setEffect(null);
        });

        buttons.getChildren().addAll(btnNo, btnYes);
        dialog.getChildren().addAll(lblTitle, lblDesc, buttons);
        overlay.getChildren().add(dialog);

        menuContainer.setEffect(new GaussianBlur(10));
        this.getChildren().add(overlay);
    }

    private void navigate(int direction) {
        selectedIndex += direction;
        if (selectedIndex < 0)
            selectedIndex = menuButtons.size() - 1;
        if (selectedIndex >= menuButtons.size())
            selectedIndex = 0;
        updateSelectionVisuals();
    }

    private void updateSelectionVisuals() {
        for (int i = 0; i < menuButtons.size(); i++) {
            StackPane btn = menuButtons.get(i);
            Rectangle border = (Rectangle) btn.getChildren().get(0);
            Color baseColor = (Color) border.getStroke();

            if (i == selectedIndex) {
                border.setFill(baseColor.deriveColor(0, 1, 1, 0.3));
                btn.setScaleX(1.1);
                btn.setScaleY(1.1);
            } else {
                border.setFill(null);
                btn.setScaleX(1.0);
                btn.setScaleY(1.0);
            }
        }
    }

    private void triggerSelectedButton() {
        menuButtons.get(selectedIndex).getOnMouseClicked().handle(null);
    }

    private StackPane createButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        txt.setFill(color);

        Rectangle border = new Rectangle(250, 50);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(3);
        border.setArcWidth(0);
        border.setArcHeight(0);
        border.setEffect(new DropShadow(10, color));

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> action.run());
        btn.setOnMouseEntered(e -> {
            selectedIndex = menuButtons.indexOf(btn);
            updateSelectionVisuals();
        });

        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });

        return btn;
    }

    private StackPane createMiniButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        txt.setFill(Color.WHITE);

        Rectangle border = new Rectangle(120, 40);
        border.setFill(color.deriveColor(0, 1, 1, 0.3));
        border.setStroke(color);
        border.setStrokeWidth(3);
        border.setArcWidth(0);
        border.setArcHeight(0);

        StackPane btn = new StackPane(border, txt);
        // btn.setOnMouseClicked(e -> action.run());
        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });

        btn.setOnMouseEntered(e -> {
            border.setFill(color);
            txt.setFill(Color.BLACK);
            btn.setCursor(javafx.scene.Cursor.HAND);
        });
        btn.setOnMouseExited(e -> {
            border.setFill(color.deriveColor(0, 1, 1, 0.2));
            txt.setFill(Color.WHITE);
        });
        return btn;
    }

    private void createFloatingAtoms(Pane pane) {
        Random rand = new Random();
        List<Circle> atoms = new ArrayList<>();
        Color[] colors = { Color.RED, Color.CYAN, Color.LIME, Color.YELLOW };
        for (int i = 0; i < 15; i++) {
            Circle c = new Circle(rand.nextInt(5) + 2, colors[rand.nextInt(4)]);
            c.setOpacity(0.3);
            c.setTranslateX(rand.nextInt(400));
            c.setTranslateY(rand.nextInt(600));
            pane.getChildren().add(c);
            atoms.add(c);
        }
        new AnimationTimer() {
            public void handle(long now) {
                for (Circle c : atoms) {
                    c.setTranslateY(c.getTranslateY() - 0.5);
                    if (c.getTranslateY() < 0)
                        c.setTranslateY(700);
                }
            }
        }.start();
    }
}