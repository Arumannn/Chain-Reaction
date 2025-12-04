package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.core.SoundManager;
import com.silent.treatment.chainreaction.model.MapType;
import com.silent.treatment.chainreaction.view.DifficultySelectionView.Difficulty;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
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
import java.util.function.Consumer;

public class SetupView extends StackPane {

    // Struktur Data untuk dikirim ke MainApp
    public static class GameConfig {
        public MapType mapType;
        public List<Player> players;
        public List<Boolean> isBot; // Track player mana yang bot
        public Difficulty botDifficulty; // Difficulty untuk semua bot

        public GameConfig(MapType map, List<Player> p, List<Boolean> bots, Difficulty diff) {
            this.mapType = map;
            this.players = p;
            this.isBot = bots;
            this.botDifficulty = diff;
        }
    }

    private final Consumer<GameConfig> onStartGame;
    private ComboBox<MapType> boardSizeCombo;
    private final Runnable onBack;

    // UI Components
    // private ComboBox<String> boardSizeCombo;
    private Spinner<Integer> playerCountSpinner;
    private Spinner<Integer> botCountSpinner;
    private ComboBox<Difficulty> botDifficultyCombo;
    private VBox playersContainer;

    public SetupView(Consumer<GameConfig> onStartGame, Runnable onBack) {
        this.onStartGame = onStartGame;
        this.onBack = onBack;

        // --- 1. Background Animasi (Konsisten dengan Menu) ---
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: #0a0a0a;");
        createFloatingAtoms(backgroundLayer);

        // --- 2. Panel Utama (Glass Effect) ---
        VBox mainPanel = new VBox(20);
        mainPanel.setMaxWidth(600);
        mainPanel.setMaxHeight(650);
        mainPanel.setPadding(new Insets(30));
        mainPanel.setAlignment(Pos.TOP_CENTER);

        // Styling Panel: Background semi-transparan dengan border tipis
        mainPanel.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #444444;" +
                        "-fx-border-radius: 0;" +
                        "-fx-border-width: 4;");
        mainPanel.getStyleClass().add("panel-main");
        // Efek bayangan panel
        mainPanel.setEffect(new DropShadow(20, Color.BLACK));

        // --- 3. Header ---
        Text title = new Text("MATCH SETUP");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 40));
        title.setEffect(new DropShadow(10, Color.CYAN));

        // --- 4. Game Settings Section ---
        VBox settingsBox = new VBox(15);
        settingsBox.setAlignment(Pos.CENTER);

        // Input: Board Size
        VBox sizeBox = createStyledInputGroup("Board Size", createCustomComboBox());

        // Input: Player Count
        VBox countBox = createStyledInputGroup("Total Players", createCustomSpinner());

        // Input: Bot Count
        VBox botCountBox = createStyledInputGroup("Number of Bots", createBotCountSpinner());

        // Input: Bot Difficulty
        VBox botDiffBox = createStyledInputGroup("Bot Difficulty", createBotDifficultyCombo());

        HBox topSettings = new HBox(20, sizeBox, countBox);
        topSettings.setAlignment(Pos.CENTER);

        HBox botSettings = new HBox(20, botCountBox, botDiffBox);
        botSettings.setAlignment(Pos.CENTER);

        // --- 5. Player List Section ---
        Label lblPlayers = new Label("PLAYER CONFIGURATION");
        lblPlayers.setTextFill(Color.CYAN);
        lblPlayers.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        playersContainer = new VBox(10);
        playersContainer.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(playersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #333333; -fx-border-width: 2; -fx-border-radius: 0;");
        scrollPane.getStylesheets()
                .add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; }"); // Hack CSS
                                                                                                       // inline

        // Init awal (2 pemain)
        refreshPlayerInputs(2);

        // --- 6. Action Buttons ---
        HBox actionBox = new HBox(20);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(20, 0, 0, 0));

        Button btnBack = createStyledButton("BACK", Color.GRAY, onBack);
        Button btnStart = createStyledButton("START GAME", Color.LIME, this::processStart);

        actionBox.getChildren().addAll(btnBack, btnStart);

        // Susun semua ke Panel Utama
        mainPanel.getChildren().addAll(title, topSettings, botSettings, new Separator(), lblPlayers, scrollPane,
                actionBox);

        // Susun ke Root StackPane
        this.getChildren().addAll(backgroundLayer, mainPanel);
    }

    // --- Helper Logic: Refresh List Player ---
    private void refreshPlayerInputs(int count) {
        playersContainer.getChildren().clear();
        Color[] defaults = { Color.RED, Color.LIME, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE,
                Color.WHITE };

        for (int i = 0; i < count; i++) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            // Style tiap baris player
            row.setStyle(
                    "-fx-background-color: #1a1a1a; -fx-background-radius: 0; -fx-border-color: #333333; -fx-border-width: 2; -fx-border-radius: 0;");
            row.getStyleClass().add("player-row");

            Label lbl = new Label("#" + (i + 1));
            lbl.setTextFill(Color.GRAY);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lbl.setPrefWidth(30);

            TextField nameField = new TextField("Player " + (i + 1));
            nameField.setPrefWidth(200);
            nameField.setStyle("-fx-background-color: #0a0a0a; -fx-text-fill: white; -fx-background-radius: 0; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");

            ColorPicker colorPicker = new ColorPicker(defaults[i % defaults.length]);
            colorPicker.setPrefWidth(120);
            colorPicker.setStyle("-fx-background-color: #0a0a0a; -fx-text-fill: white; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");

            // Auto-detect bot berdasarkan bot count
            int botCount = botCountSpinner.getValue();
            Label typeLabel = new Label("");
            typeLabel.setPrefWidth(60);
            typeLabel.setAlignment(Pos.CENTER);
            typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

            if (i >= count - botCount) {
                nameField.setText("AI Bot " + (i - (count - botCount) + 1));
                typeLabel.setText("🤖 BOT");
                typeLabel.setTextFill(Color.CYAN);
                typeLabel.setStyle(
                        "-fx-background-color: rgba(0,255,255,0.1); -fx-background-radius: 5; -fx-padding: 3;");
            } else {
                typeLabel.setText("👤 HUMAN");
                typeLabel.setTextFill(Color.LIME);
                typeLabel
                        .setStyle("-fx-background-color: rgba(0,255,0,0.1); -fx-background-radius: 5; -fx-padding: 3;");
            }

            row.getChildren().addAll(lbl, nameField, colorPicker, typeLabel);
            playersContainer.getChildren().add(row);
        }
    }

    // --- Helper UI: Input Group Label ---
    private VBox createStyledInputGroup(String labelText, Control input) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setFont(Font.font("Arial", 12));

        VBox box = new VBox(5, lbl, input);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // --- Helper UI: Custom ComboBox ---
    private ComboBox<MapType> createCustomComboBox() {
        boardSizeCombo = new ComboBox<>();
        boardSizeCombo.getItems().addAll(MapType.values());
        boardSizeCombo.setValue(MapType.SMALL);
        boardSizeCombo.setPrefWidth(180);
        boardSizeCombo.setStyle("-fx-background-color: #0a0a0a; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");
        return boardSizeCombo;
    }

    // --- Helper UI: Custom Spinner ---
    private Spinner<Integer> createCustomSpinner() {
        playerCountSpinner = new Spinner<>(2, 8, 2);
        playerCountSpinner.setPrefWidth(120);
        playerCountSpinner.setStyle("-fx-background-color: #0a0a0a; -fx-body-color: #0a0a0a; -fx-text-fill: white; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");
        // Update list saat angka berubah
        playerCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Pastikan bot count tidak melebihi total players
            if (botCountSpinner != null && botCountSpinner.getValue() > newVal - 1) {
                botCountSpinner.getValueFactory().setValue(Math.max(0, newVal - 1));
            }
            refreshPlayerInputs(newVal);
        });
        return playerCountSpinner;
    }

    // --- Helper UI: Bot Count Spinner ---
    private Spinner<Integer> createBotCountSpinner() {
        botCountSpinner = new Spinner<>(0, 7, 0);
        botCountSpinner.setEditable(false);
        botCountSpinner.setPrefWidth(120);
        botCountSpinner.setStyle("-fx-background-color: #0a0a0a; -fx-text-fill: white; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");

        // Event: Refresh UI saat bot count berubah
        botCountSpinner.valueProperty().addListener((obs, old, newVal) -> {
            // Pastikan bot count tidak melebihi total players - 1 (minimal 1 human)
            int maxBots = playerCountSpinner.getValue() - 1;
            if (newVal > maxBots) {
                botCountSpinner.getValueFactory().setValue(maxBots);
                return;
            }
            refreshPlayerInputs(playerCountSpinner.getValue());
        });

        return botCountSpinner;
    }

    // --- Helper UI: Bot Difficulty ComboBox ---
    private ComboBox<Difficulty> createBotDifficultyCombo() {
        botDifficultyCombo = new ComboBox<>();
        botDifficultyCombo.getItems().addAll(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        botDifficultyCombo.setValue(Difficulty.EASY);
        botDifficultyCombo.setPrefWidth(120);
        botDifficultyCombo.setStyle("-fx-background-color: #0a0a0a; -fx-text-fill: white; -fx-border-color: #444444; -fx-border-width: 3; -fx-border-radius: 0;");
        return botDifficultyCombo;
    }

    // --- Helper UI: Styled Button ---
    private Button createStyledButton(String text, Color baseColor, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setPrefWidth(150);
        btn.setPrefHeight(40);

        String hexColor = toHexString(baseColor);
        String styleNormal = String.format(
                "-fx-background-color: transparent; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-width: 3; -fx-border-radius: 0; -fx-cursor: hand;",
                hexColor, hexColor);
        String styleHover = String.format(
                "-fx-background-color: %s; -fx-text-fill: black; -fx-border-color: %s; -fx-border-width: 3; -fx-border-radius: 0; -fx-cursor: hand;",
                hexColor, hexColor);

        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));

        btn.setOnAction(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });

        return btn;
    }

    // --- Logic Start ---
    private void processStart() {
        MapType selectedMap = boardSizeCombo.getValue();
        Difficulty botDifficulty = botDifficultyCombo.getValue();
        int totalPlayers = playerCountSpinner.getValue();
        int botCount = botCountSpinner.getValue();

        List<Player> players = new ArrayList<>();
        List<Boolean> botFlags = new ArrayList<>();

        int index = 0;
        for (javafx.scene.Node node : playersContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                TextField tf = (TextField) row.getChildren().get(1);
                ColorPicker cp = (ColorPicker) row.getChildren().get(2);

                players.add(new Player(tf.getText(), cp.getValue()));
                // Player terakhir sebanyak botCount adalah bot
                botFlags.add(index >= totalPlayers - botCount);
                index++;
            }
        }
        onStartGame.accept(new GameConfig(selectedMap, players, botFlags, botDifficulty));
    }

    // --- Background Animation (Sama dgn Menu) ---
    private void createFloatingAtoms(Pane pane) {
        Random rand = new Random();
        List<Circle> atoms = new ArrayList<>();
        Color[] colors = { Color.RED, Color.CYAN, Color.LIME, Color.YELLOW };

        for (int i = 0; i < 20; i++) {
            Circle c = new Circle(rand.nextInt(4) + 2, colors[rand.nextInt(4)]);
            c.setOpacity(0.2);
            c.setTranslateX(rand.nextInt(800));
            c.setTranslateY(rand.nextInt(600));
            pane.getChildren().add(c);
            atoms.add(c);
        }
        new AnimationTimer() {
            public void handle(long now) {
                for (Circle c : atoms) {
                    c.setTranslateY(c.getTranslateY() - 0.3);
                    if (c.getTranslateY() < 0)
                        c.setTranslateY(pane.getHeight() + 10);
                }
            }
        }.start();
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}