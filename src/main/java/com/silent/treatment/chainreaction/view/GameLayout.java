package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.SoundManager;
import com.silent.treatment.chainreaction.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameLayout extends BorderPane {
    
    private final GameManager gm;
    private final GridPanel gridPanel;
    private final Runnable onMenuClick;

    // Komponen UI
    private Label turnLabel;
    private Circle turnIndicatorCircle;
    private VBox playersStatusBox;

    public GameLayout(GameManager gm, GridPanel gridPanel, Runnable onMenuClick) {
        this.gm = gm;
        this.gridPanel = gridPanel;
        this.onMenuClick = onMenuClick;

        this.setStyle("-fx-background-color: #0a0a0a;");
        
        // Susun Layout
        this.setTop(createHeader());
        this.setRight(createPlayerSidebar());
        
        VBox centerBox = new VBox(gridPanel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        this.setCenter(centerBox);
    }

    public void updateGameInfo() {
        Player current = gm.getCurrentPlayer();

        // Update Header Info
        turnLabel.setText(current.getName().toUpperCase());
        turnLabel.setTextFill(current.getColor());
        turnIndicatorCircle.setFill(current.getColor());
        turnIndicatorCircle.setEffect(new DropShadow(10, current.getColor()));

        // Update Grid Theme
        gridPanel.setBackgroundTheme(current.getColor());

        // Update Sidebar List
        playersStatusBox.getChildren().clear();
        for (Player p : gm.getPlayers()) {
            HBox row = createPlayerRow(p, current);
            playersStatusBox.getChildren().add(row);
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333333; -fx-border-width: 0 0 4 0;");
        header.getStyleClass().add("header-panel");

        HBox turnInfoBox = new HBox(10);
        turnInfoBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("CURRENT TURN:");
        titleLabel.setTextFill(Color.GRAY);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        turnIndicatorCircle = new Circle(8);
        turnIndicatorCircle.setStroke(Color.WHITE);

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        turnInfoBox.getChildren().addAll(titleLabel, turnIndicatorCircle, turnLabel);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnMenu = new Button("MENU");
        btnMenu.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #888888; -fx-border-radius: 0; -fx-border-width: 3; -fx-cursor: hand;");
        btnMenu.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnMenu.setOnAction(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            onMenuClick.run();
        });

        // Spacer kiri kanan agar turn info tetap di tengah (kurang lebih)
        Pane leftSpacer = new Pane();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Pane rightSpacer = new Pane();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        header.getChildren().addAll(leftSpacer, turnInfoBox, rightSpacer, btnMenu);
        return header;
    }

    private VBox createPlayerSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #151515; -fx-border-color: #333333; -fx-border-width: 0 0 0 4;");
        sidebar.getStyleClass().add("sidebar-panel");

        Label title = new Label("PLAYER STATUS");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));

        playersStatusBox = new VBox(10);
        sidebar.getChildren().addAll(title, new Separator(), playersStatusBox);
        return sidebar;
    }

    private HBox createPlayerRow(Player p, Player current) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        if (p.equals(current)) {
            row.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 0; -fx-border-color: #00ff00; -fx-border-width: 3;");
            row.getStyleClass().add("current");
        }
        row.getStyleClass().add("player-row");

        Circle icon = new Circle(5, p.getColor());
        VBox infoBox = new VBox(2);

        Label name = new Label(p.getName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Label status = new Label();
        status.setFont(Font.font("Arial", 11));

        if (p.isAlive()) {
            name.setTextFill(Color.LIGHTGRAY);
            status.setText(gm.getPlayerOrbCount(p) + " Orbs");
            status.setTextFill(Color.GRAY);
        } else {
            name.setTextFill(Color.DARKGRAY);
            icon.setFill(Color.DARKGRAY);
            status.setText("GAME OVER");
            status.setTextFill(Color.RED);
            status.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        }

        infoBox.getChildren().addAll(name, status);
        row.getChildren().addAll(icon, infoBox);
        return row;
    }
}