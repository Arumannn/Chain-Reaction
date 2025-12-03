package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.core.SoundManager;
import com.silent.treatment.chainreaction.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class GameOverView extends StackPane {

    // UPDATE: Constructor sekarang menerima 'onClose' (aksi untuk menutup dialog)
    public GameOverView(Player winner, Runnable onBackToMenu, Runnable onClose) {
        // 1. Overlay Gelap
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        // 2. Panel Dialog
        VBox dialog = new VBox(20);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(300);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPadding(new Insets(30));

        // Deteksi apakah pemenang adalah BOT (lose condition untuk human)
        boolean isBotWinner = winner.getName() != null && winner.getName().toLowerCase().contains("ai bot");

        // Warna border menyesuaikan warna pemenang
        String hexColor = toHexString(winner.getColor());
        dialog.setStyle(
                (isBotWinner ? "-fx-background-color: #1a0a0a;" : "-fx-background-color: #1e1e1e;") +
                        "-fx-border-color: " + hexColor + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, " + hexColor + ", 20, 0, 0, 0);"
        );

        // 3. Teks Pemenang
        Label lblTitle = new Label(isBotWinner ? "DEFEAT" : "WINNER!");
        lblTitle.setTextFill(isBotWinner ? Color.web("#ff4c4c") : Color.WHITE);
        lblTitle.setFont(Font.font("Impact", 40));

        Label lblName = null;
        if (!isBotWinner) {
            lblName = new Label(winner.getName().toUpperCase());
            lblName.setTextFill(winner.getColor());
            lblName.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        }

        Label lblDesc = new Label(
                isBotWinner
                        ? "All human players have been eliminated.\nThe AI has dominated the chain reaction."
                        : "Congratulations! The chain reaction is complete."
        );
        lblDesc.setTextFill(isBotWinner ? Color.web("#ffb3b3") : Color.LIGHTGRAY);
        lblDesc.setWrapText(true);

        // 4. Tombol Aksi
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        // Tombol Back to Menu
        StackPane btnBack = createStyledButton("BACK TO MENU", winner.getColor(), onBackToMenu);

        // [BARU] Tombol View Board (Hanya tutup dialog)
        StackPane btnView = createStyledButton("VIEW BOARD", Color.GRAY, onClose);

        buttonBox.getChildren().addAll(btnView, btnBack);

        if (isBotWinner) {
            dialog.getChildren().addAll(lblTitle, lblDesc, buttonBox);
        } else {
            dialog.getChildren().addAll(lblTitle, lblName, lblDesc, buttonBox);
        }
        this.getChildren().add(dialog);
    }

    private StackPane createStyledButton(String text, Color color, Runnable action) {
        Text txt = new Text(text);
        txt.setFill(color);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Rectangle border = new Rectangle(140, 40);
        border.setFill(null);
        border.setStroke(color);
        border.setStrokeWidth(2);
        border.setArcWidth(10);
        border.setArcHeight(10);

        StackPane btn = new StackPane(border, txt);
        btn.setOnMouseClicked(e -> action.run());
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setOnMouseEntered(e -> {
            border.setFill(color);
            txt.setFill(Color.BLACK);
        });
        btn.setOnMouseExited(e -> {
            border.setFill(null);
            txt.setFill(color);
        });

        btn.setOnMouseClicked(e -> {
            SoundManager.getInstance().playSFX(SoundManager.SFX_CLICK);
            action.run();
        });

        return btn;
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}