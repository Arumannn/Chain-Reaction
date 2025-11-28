package com.silent.treatment.chainreaction.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class DifficultySelectionView extends VBox {

    public enum Difficulty {
        TUTORIAL_NOOB, EASY, MEDIUM, HARD
    }

    // Callback akan mengirimkan Difficulty yang dipilih
    public DifficultySelectionView(Consumer<Difficulty> onDifficultySelected, Runnable onBack) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(25);
        this.setPadding(new Insets(40));
        this.setStyle("-fx-background-color: #121212;");

        Label title = new Label("PILIH TINGKAT KESULITAN AI");
        title.setTextFill(Color.CYAN);
        title.setFont(Font.font("Impact", 30));

        VBox buttons = new VBox(15);
        buttons.setAlignment(Pos.CENTER);

        // Buttons for Difficulty
        buttons.getChildren().add(createDifficultyButton("MODE TUTORIAL (Noob)", Difficulty.TUTORIAL_NOOB, onDifficultySelected, Color.YELLOWGREEN));
        buttons.getChildren().add(createDifficultyButton("MODE MUDAH (Easy)", Difficulty.EASY, onDifficultySelected, Color.LIMEGREEN));
        buttons.getChildren().add(createDifficultyButton("MODE SEDANG (Medium)", Difficulty.MEDIUM, onDifficultySelected, Color.ORANGE));
        buttons.getChildren().add(createDifficultyButton( "MODE SULIT (Hard)", Difficulty.HARD, onDifficultySelected, Color.RED));

        Button backButton = new Button("KEMBALI");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-border-color: gray; -fx-border-width: 1; -fx-background-radius: 5;");
        backButton.setOnAction(e -> onBack.run());

        this.getChildren().addAll(title, buttons, backButton);
    }

    private StackPane createDifficultyButton(String text, Difficulty difficulty, Consumer<Difficulty> onSelect, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.WHITE);

        StackPane button = new StackPane(label);
        button.setPrefSize(250, 50);

        String hexColor = String.format("#%02x%02x%02x", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));

        button.setStyle(String.format("-fx-background-color: #333333; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 5;", hexColor));

        button.setOnMouseEntered(e -> button.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 5;", hexColor, hexColor)));
        button.setOnMouseExited(e -> button.setStyle(String.format("-fx-background-color: #333333; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 5;", hexColor)));

        button.setOnMouseClicked(e -> onSelect.accept(difficulty));

        return button;
    }
}