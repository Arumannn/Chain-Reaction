package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class GridPanel extends GridPane {

    private GameController controller;

    public GridPanel(Board board, GameController controller) {
        this.controller = controller;
        setHgap(5);
        setVgap(5);
        initializeUI(board);
    }

    private void initializeUI(Board board) {
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);
                CellView cellView = new CellView(cell);
                this.add(cellView, i, j);
            }
        }
    }

    // Inner Class untuk Tampilan per Cell
    private class CellView extends StackPane implements GameObserver {
        private Cell cell;
        private Rectangle border;
        private Circle orb;
        private Text countText;

        public CellView(Cell cell) {
            this.cell = cell;
            this.cell.attach(this); // Register Observer

            // 1. Background / Border
            border = new Rectangle(50, 50);
            border.setFill(Color.BLACK);
            border.setStroke(Color.GRAY);
            border.setStrokeWidth(2);
            border.setArcWidth(10);
            border.setArcHeight(10);

            // 2. Orb Representation
            orb = new Circle(15);
            orb.setFill(Color.TRANSPARENT); // Invisible initially

            // 3. Text Count
            countText = new Text("");
            countText.setFill(Color.WHITE);

            this.getChildren().addAll(border, orb, countText);

            // Event Click -> Kirim ke Controller
            this.setOnMouseClicked(e -> controller.handleCellClick(cell));
        }

        @Override
        public void update(Cell cell) {
            // Update Visual berdasarkan Data Model (FR-2.3)
            int count = cell.getOrbs();

            if (count > 0 && cell.getOwner() != null) {
                Color pColor = cell.getOwner().getColor();
                border.setStroke(pColor);
                orb.setFill(pColor);
                countText.setText(String.valueOf(count));

                // Efek visual sederhana: ukuran orb berubah sesuai jumlah
                orb.setRadius(10 + (count * 2));
            } else {
                border.setStroke(Color.GRAY);
                orb.setFill(Color.TRANSPARENT);
                countText.setText("");
            }
        }
    }
}