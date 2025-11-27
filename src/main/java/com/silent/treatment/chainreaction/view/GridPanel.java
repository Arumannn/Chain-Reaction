package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class GridPanel extends GridPane {

    private GameController controller;

    public GridPanel(Board board, GameController controller) {
        this.controller = controller;

        this.setAlignment(Pos.CENTER);
        setHgap(5);
        setVgap(5);

        setBackgroundTheme(Color.valueOf("#222222"));

        initializeUI(board);
    }

    public void setBackgroundTheme(Color playerColor) {
        String rgba = String.format("rgba(%d, %d, %d, 0.15)",
                (int)(playerColor.getRed() * 255),
                (int)(playerColor.getGreen() * 255),
                (int)(playerColor.getBlue() * 255)
        );

        this.setStyle(
                "-fx-background-color: " + rgba + ";" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #444;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 2;"
        );
    }

    private void initializeUI(Board board) {
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);
                if (cell != null) {
                    CellView cellView = new CellView(cell);
                    this.add(cellView, i, j);
                }
            }
        }
    }

    private class CellView extends StackPane implements GameObserver {
        private Cell cell;
        private Rectangle border;
        private Circle orb;
        private Text countText;
        private DropShadow glowEffect;

        public CellView(Cell cell) {
            this.cell = cell;
            this.cell.attach(this);

            border = new Rectangle(55, 55);
            border.setFill(Color.valueOf("#2b2b2b"));
            border.setStroke(Color.valueOf("#444444"));
            border.setStrokeWidth(2);
            border.setArcWidth(15);
            border.setArcHeight(15);

            glowEffect = new DropShadow();
            glowEffect.setRadius(15);
            glowEffect.setSpread(0.4);

            orb = new Circle(12);
            orb.setFill(Color.TRANSPARENT);
            orb.setEffect(glowEffect);

            countText = new Text("");
            countText.setFill(Color.WHITE);
            countText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            this.getChildren().addAll(border, orb, countText);

            this.setOnMouseEntered(e -> {
                if(cell.getOwner() == null) border.setFill(Color.valueOf("#383838"));
            });
            this.setOnMouseExited(e -> {
                if(cell.getOwner() == null) border.setFill(Color.valueOf("#2b2b2b"));
            });

            this.setOnMouseClicked(e -> controller.handleCellClick(cell));
        }

        @Override
        public void update(Cell cell) {
            int count = cell.getOrbs();
            if (count > 0 && cell.getOwner() != null) {
                Color pColor = cell.getOwner().getColor();

                border.setStroke(pColor.darker());

                orb.setFill(pColor);
                glowEffect.setColor(pColor);
                countText.setText(String.valueOf(count));

                orb.setRadius(10 + (count * 3));

                if (count >= cell.getCriticalMass()) {
                    orb.setStroke(Color.WHITE);
                    orb.setStrokeWidth(2);
                } else {
                    orb.setStroke(null);
                }
            } else {
                border.setStroke(Color.valueOf("#444444"));
                orb.setFill(Color.TRANSPARENT);
                glowEffect.setColor(Color.TRANSPARENT);
                countText.setText("");
                orb.setStroke(null);
            }
        }
    }
}