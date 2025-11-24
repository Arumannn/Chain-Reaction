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
        
        // Default Style (Dark Grey) saat awal game
        setBackgroundTheme(Color.valueOf("#222222"));
        
        initializeUI(board);
    }

    // Method Baru: Mengubah warna background grid menjadi soft mengikuti pemain
    public void setBackgroundTheme(Color playerColor) {
        // Konversi Color JavaFX ke format CSS rgba
        // Kita gunakan opacity 0.15 agar warnanya sangat lembut (soft)
        String rgba = String.format("rgba(%d, %d, %d, 0.15)", 
            (int)(playerColor.getRed() * 255),
            (int)(playerColor.getGreen() * 255),
            (int)(playerColor.getBlue() * 255)
        );
        
        // Terapkan style dengan warna border tetap abu-abu gelap
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
                CellView cellView = new CellView(cell);
                this.add(cellView, i, j);
            }
        }
    }

    // Inner Class: Tampilan Sel Individual
    private class CellView extends StackPane implements GameObserver {
        private Cell cell;
        private Rectangle border;
        private Circle orb;
        private Text countText;
        private DropShadow glowEffect;

        public CellView(Cell cell) {
            this.cell = cell;
            this.cell.attach(this); 

            // 1. Background Kotak (Rounded)
            border = new Rectangle(55, 55);
            border.setFill(Color.valueOf("#2b2b2b")); 
            border.setStroke(Color.valueOf("#444444")); 
            border.setStrokeWidth(2);
            border.setArcWidth(15); 
            border.setArcHeight(15);

            // 2. Efek Glow (Cahaya Neon)
            glowEffect = new DropShadow();
            glowEffect.setRadius(15);
            glowEffect.setSpread(0.4);

            // 3. Orb Representation
            orb = new Circle(12);
            orb.setFill(Color.TRANSPARENT);
            orb.setEffect(glowEffect); 

            // 4. Text Count
            countText = new Text("");
            countText.setFill(Color.WHITE);
            countText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            this.getChildren().addAll(border, orb, countText);

            // Hover Effect
            this.setOnMouseEntered(e -> {
                if(cell.getOwner() == null) border.setFill(Color.valueOf("#383838"));
            });
            this.setOnMouseExited(e -> {
                 if(cell.getOwner() == null) border.setFill(Color.valueOf("#2b2b2b"));
            });
            
            // Event Click
            this.setOnMouseClicked(e -> controller.handleCellClick(cell));
        }

        @Override
        public void update(Cell cell) {
            int count = cell.getOrbs();
            if (count > 0 && cell.getOwner() != null) {
                Color pColor = cell.getOwner().getColor();
                
                // Ubah warna Border sedikit mengikuti pemain
                border.setStroke(pColor.darker());
                
                // Orb menyala
                orb.setFill(pColor);
                glowEffect.setColor(pColor); 
                countText.setText(String.valueOf(count));
                
                // Logika Visual Sederhana: Ukuran orb berubah sesuai jumlah
                orb.setRadius(10 + (count * 3));
                
                // Jika sudah kritis (siap meledak), beri indikasi visual
                if (count >= cell.getCriticalMass()) {
                   orb.setStroke(Color.WHITE);
                   orb.setStrokeWidth(2);
                } else {
                   orb.setStroke(null);
                }
            } else {
                // Reset ke tampilan kosong
                border.setStroke(Color.valueOf("#444444"));
                orb.setFill(Color.TRANSPARENT);
                glowEffect.setColor(Color.TRANSPARENT);
                countText.setText("");
                orb.setStroke(null);
            }
        }
    }
}