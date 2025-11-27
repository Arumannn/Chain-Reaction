package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.controller.GameController;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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
                    CellView cellView = new CellView(cell, board);
                    this.add(cellView, i, j);
                }
            }
        }
    }

    // Inner Class: Tampilan Sel Individual dengan Stacked Balls dan Animasi
    private class CellView extends StackPane implements GameObserver {
        private Cell cell;
        private Board board;
        private Rectangle border;
        private Pane ballContainer; // Container untuk semua bola
        private List<Circle> balls; // List untuk menyimpan semua bola
        private DropShadow glowEffect;
        private Timeline currentAnimation; // Untuk tracking animasi yang sedang berjalan
        
        // Enum untuk tipe cell
        private enum CellType {
            CORNER,    // Sudut (2 tetangga)
            EDGE,      // Sisi (3 tetangga)
            CENTER     // Tengah (4 tetangga)
        }

        public CellView(Cell cell, Board board) {
            this.cell = cell;
            this.board = board;
            this.cell.attach(this);
            this.balls = new ArrayList<>();

            border = new Rectangle(55, 55);
            border.setFill(Color.valueOf("#2b2b2b"));
            border.setStroke(Color.valueOf("#444444"));
            border.setStrokeWidth(2);
            border.setArcWidth(15);
            border.setArcHeight(15);

            glowEffect = new DropShadow();
            glowEffect.setRadius(15);
            glowEffect.setSpread(0.4);

            // 3. Container untuk bola-bola
            ballContainer = new Pane();
            ballContainer.setPrefSize(55, 55);
            ballContainer.setMaxSize(55, 55);

            this.getChildren().addAll(border, ballContainer);

            this.setOnMouseEntered(e -> {
                if(cell.getOwner() == null) border.setFill(Color.valueOf("#383838"));
            });
            this.setOnMouseExited(e -> {
                if(cell.getOwner() == null) border.setFill(Color.valueOf("#2b2b2b"));
            });

            this.setOnMouseClicked(e -> controller.handleCellClick(cell));
        }

        /**
         * Menentukan tipe cell berdasarkan jumlah tetangga
         */
        private CellType getCellType() {
            int neighbors = cell.getNeighbors().size();
            switch (neighbors) {
                case 2: return CellType.CORNER;
                case 3: return CellType.EDGE;
                case 4: return CellType.CENTER;
                default: return CellType.CENTER;
            }
        }

        /**
         * Membuat bola baru dengan efek glow
         */
        private Circle createBall(Color color) {
            Circle ball = new Circle(8);
            ball.setFill(color);
            
            DropShadow ballGlow = new DropShadow();
            ballGlow.setRadius(12);
            ballGlow.setSpread(0.5);
            ballGlow.setColor(color);
            ball.setEffect(ballGlow);
            
            return ball;
        }

        /**
         * Mengatur posisi bola-bola dalam stack
         */
        private void positionBalls(int count) {
            // Posisi center dari container (55x55)
            double centerX = 27.5;
            double centerY = 27.5;
            
            switch (count) {
                case 1:
                    // Satu bola di tengah
                    balls.get(0).setLayoutX(centerX);
                    balls.get(0).setLayoutY(centerY);
                    break;
                    
                case 2:
                    // Dua bola berdampingan horizontal
                    balls.get(0).setLayoutX(centerX - 8);
                    balls.get(0).setLayoutY(centerY);
                    balls.get(1).setLayoutX(centerX + 8);
                    balls.get(1).setLayoutY(centerY);
                    break;
                    
                case 3:
                    // Tiga bola membentuk segitiga
                    balls.get(0).setLayoutX(centerX);
                    balls.get(0).setLayoutY(centerY - 8);
                    balls.get(1).setLayoutX(centerX - 8);
                    balls.get(1).setLayoutY(centerY + 6);
                    balls.get(2).setLayoutX(centerX + 8);
                    balls.get(2).setLayoutY(centerY + 6);
                    break;
                    
                case 4:
                    // Empat bola membentuk kotak (state kritis sebelum meledak)
                    balls.get(0).setLayoutX(centerX - 7);
                    balls.get(0).setLayoutY(centerY - 7);
                    balls.get(1).setLayoutX(centerX + 7);
                    balls.get(1).setLayoutY(centerY - 7);
                    balls.get(2).setLayoutX(centerX - 7);
                    balls.get(2).setLayoutY(centerY + 7);
                    balls.get(3).setLayoutX(centerX + 7);
                    balls.get(3).setLayoutY(centerY + 7);
                    break;
            }
        }

        /**
         * Membuat animasi shake untuk corner cells dengan 1 bola
         */
        private Timeline createShakeAnimation() {
            if (balls.isEmpty()) return null;
            
            Circle ball = balls.get(0);
            double originalX = ball.getLayoutX();
            double originalY = ball.getLayoutY();
            
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(ball.layoutXProperty(), originalX),
                    new KeyValue(ball.layoutYProperty(), originalY)),
                new KeyFrame(Duration.millis(50),
                    new KeyValue(ball.layoutXProperty(), originalX + 3),
                    new KeyValue(ball.layoutYProperty(), originalY + 2)),
                new KeyFrame(Duration.millis(100),
                    new KeyValue(ball.layoutXProperty(), originalX - 3),
                    new KeyValue(ball.layoutYProperty(), originalY - 2)),
                new KeyFrame(Duration.millis(150),
                    new KeyValue(ball.layoutXProperty(), originalX + 2),
                    new KeyValue(ball.layoutYProperty(), originalY + 3)),
                new KeyFrame(Duration.millis(200),
                    new KeyValue(ball.layoutXProperty(), originalX),
                    new KeyValue(ball.layoutYProperty(), originalY))
            );
            
            timeline.setCycleCount(Timeline.INDEFINITE);
            return timeline;
        }

        /**
         * Membuat animasi rotate untuk edge dan center cells
         */
        private Timeline createRotateAnimation() {
            Timeline timeline = new Timeline();
            
            // Pusat rotasi
            double centerX = 27.5;
            double centerY = 27.5;
            
            // Untuk setiap bola, buat animasi rotasi memutar center
            for (Circle ball : balls) {
                // Hitung posisi awal relatif terhadap center
                double startX = ball.getLayoutX();
                double startY = ball.getLayoutY();
                double radius = Math.sqrt(Math.pow(startX - centerX, 2) + Math.pow(startY - centerY, 2));
                double startAngle = Math.atan2(startY - centerY, startX - centerX);
                
                // Buat 360 derajat rotasi dalam 1 detik
                for (int i = 0; i <= 36; i++) {
                    double angle = startAngle + (i * Math.PI / 18); // 10 derajat per frame
                    double newX = centerX + radius * Math.cos(angle);
                    double newY = centerY + radius * Math.sin(angle);
                    
                    KeyFrame kf = new KeyFrame(
                        Duration.millis(i * 28), // 28ms * 36 = ~1 detik
                        new KeyValue(ball.layoutXProperty(), newX, Interpolator.LINEAR),
                        new KeyValue(ball.layoutYProperty(), newY, Interpolator.LINEAR)
                    );
                    timeline.getKeyFrames().add(kf);
                }
            }
            
            timeline.setCycleCount(Timeline.INDEFINITE);
            return timeline;
        }

        /**
         * Memulai animasi yang sesuai berdasarkan tipe cell dan jumlah bola
         */
        private void startAppropriateAnimation(int count) {
            // Stop animasi sebelumnya jika ada
            if (currentAnimation != null) {
                currentAnimation.stop();
                currentAnimation = null;
            }
            
            CellType type = getCellType();
            
            // Tentukan animasi berdasarkan tipe dan jumlah bola
            if (type == CellType.CORNER && count == 1) {
                // Corner dengan 1 bola: shake
                currentAnimation = createShakeAnimation();
            } else if (type == CellType.EDGE && count == 2) {
                // Edge dengan 2 bola: rotate
                currentAnimation = createRotateAnimation();
            } else if (type == CellType.CENTER && (count == 2 || count == 3)) {
                // Center dengan 2-3 bola: rotate
                currentAnimation = createRotateAnimation();
            }
            
            // Jalankan animasi jika ada
            if (currentAnimation != null) {
                currentAnimation.play();
            }
        }

        @Override
        public void update(Cell cell) {
            int count = cell.getOrbs();
            
            if (count > 0 && cell.getOwner() != null) {
                Color pColor = cell.getOwner().getColor();

                border.setStroke(pColor.darker());

                // Update jumlah bola
                ballContainer.getChildren().clear();
                balls.clear();
                
                for (int i = 0; i < count; i++) {
                    Circle ball = createBall(pColor);
                    balls.add(ball);
                    ballContainer.getChildren().add(ball);
                }
                
                // Atur posisi bola-bola
                positionBalls(count);
                
                // Jika sudah kritis (siap meledak), beri indikasi visual
                if (count >= cell.getCriticalMass()) {
                    // Tambahkan outline putih pada semua bola
                    for (Circle ball : balls) {
                        ball.setStroke(Color.WHITE);
                        ball.setStrokeWidth(2);
                    }
                }
                
                // Mulai animasi yang sesuai
                startAppropriateAnimation(count);
                
            } else {
                border.setStroke(Color.valueOf("#444444"));
                ballContainer.getChildren().clear();
                balls.clear();
                
                // Stop animasi
                if (currentAnimation != null) {
                    currentAnimation.stop();
                    currentAnimation = null;
                }
            }
        }
    }
}