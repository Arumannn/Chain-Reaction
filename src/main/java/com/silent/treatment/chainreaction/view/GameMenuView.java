package com.silent.treatment.chainreaction.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
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
        menuBox.setMaxHeight(250);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));

        // Styling Panel (Neon Border)
        menuBox.setStyle(
                "-fx-background-color: #1e1e1e;" +
                "-fx-border-color: cyan;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(three-pass-box, cyan, 15, 0, 0, 0);"
        );

        // 3. Header "MENU"
        Label lblTitle = new Label("PAUSED");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("Impact", 40));
        lblTitle.setEffect(new DropShadow(5, Color.CYAN));

        // 4. Tombol-tombol
        StackPane btnResume = createStyledButton("RESUME", Color.LIME, onResume);
        StackPane btnExit = createStyledButton("EXIT TO MENU", Color.RED, onExit);

        menuBox.getChildren().addAll(lblTitle, btnResume, btnExit);
        this.getChildren().add(menuBox);
    }

    private StackPane createStyledButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFill(color);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Rectangle border = new Rectangle(200, 45);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(2);
        border.setArcWidth(10);
        border.setArcHeight(10);

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

        return btn;
    }
}