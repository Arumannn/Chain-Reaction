package com.silent.treatment.chainreaction.view;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuView extends StackPane {

    private final Runnable onNewGame;
    private final Runnable onExit;
    private final Runnable onTutorial;
    private final Runnable onVsAI;

    private final List<StackPane> menuButtons = new ArrayList<>();
    private int selectedIndex = 0;

    // Container untuk konten utama menu
    private final VBox menuContainer;

    // [PERBAIKAN] Constructor sudah benar sesuai snippet Anda
    public MenuView(Runnable onNewGame, Runnable onExit, Runnable onTutorial, Runnable onVsAI) {
        this.onNewGame = onNewGame;
        this.onExit = onExit;
        this.onTutorial = onTutorial;
        this.onVsAI = onVsAI;

        // --- 1. Background Animasi ---
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: #121212;");
        createFloatingAtoms(backgroundLayer);
        this.getChildren().add(backgroundLayer);

        // --- 2. Judul ---
        Text title = new Text("CHAIN\nREACTION");
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setEffect(new DropShadow(20, Color.CYAN));

        // --- 3. Opsi Menu ---
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);

        // Tombol-tombol menu
        menuButtons.add(createButton("NEW GAME", Color.LIME, onNewGame));
        menuButtons.add(createButton("HOW TO PLAY", Color.YELLOW, onTutorial));
        menuButtons.add(createButton("EXIT", Color.RED, this::showExitConfirmation));

        menuBox.getChildren().addAll(menuButtons);

        // --- Layout Utama Menu ---
        menuContainer = new VBox(40, title, menuBox);
        menuContainer.setAlignment(Pos.CENTER);
        this.getChildren().add(menuContainer);

        // --- 4. Navigasi Keyboard ---
        this.setFocusTraversable(true);
        this.setOnKeyPressed(event -> {
            if (this.getChildren().size() > 2) return; // Stop jika ada dialog

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

    // --- Dialog Konfirmasi Exit ---
    private void showExitConfirmation() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        VBox dialog = new VBox(20);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(250);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPadding(new javafx.geometry.Insets(30));

        dialog.setStyle(
                "-fx-background-color: #1e1e1e;" +
                        "-fx-border-color: red;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;"
        );
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

    // --- Helper UI ---
    private void navigate(int direction) {
        selectedIndex += direction;
        if (selectedIndex < 0) selectedIndex = menuButtons.size() - 1;
        if (selectedIndex >= menuButtons.size()) selectedIndex = 0;
        updateSelectionVisuals();
    }

    private void updateSelectionVisuals() {
        for (int i = 0; i < menuButtons.size(); i++) {
            StackPane btn = menuButtons.get(i);
            Rectangle border = (Rectangle) btn.getChildren().get(0);
            Color baseColor = (Color) border.getStroke(); // Ambil warna dari stroke border

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
        border.setStrokeWidth(2);
        border.setArcWidth(15);
        border.setArcHeight(15);
        border.setEffect(new DropShadow(10, color));

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> action.run());
        btn.setOnMouseEntered(e -> {
            selectedIndex = menuButtons.indexOf(btn);
            updateSelectionVisuals();
        });
        return btn;
    }

    private StackPane createMiniButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        txt.setFill(Color.WHITE);

        Rectangle border = new Rectangle(120, 40);
        border.setFill(color.deriveColor(0, 1, 1, 0.2));
        border.setStroke(color);
        border.setStrokeWidth(2);
        border.setArcWidth(10);
        border.setArcHeight(10);

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> action.run());

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
        Color[] colors = {Color.RED, Color.CYAN, Color.LIME, Color.YELLOW};
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
                    if (c.getTranslateY() < 0) c.setTranslateY(700);
                }
            }
        }.start();
    }
}