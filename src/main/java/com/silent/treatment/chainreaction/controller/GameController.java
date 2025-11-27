package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class GameController {

    private Runnable onTurnChanged; // Callback untuk update UI luar
    private Runnable onGameOver;
    // Method ini yang hilang dan menyebabkan error
    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }


    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();
        
        if(gm.isGameOver()){
            System.out.println("Game is Over. Please Reset");
        }


        Player currentPlayer = gm.getCurrentPlayer();

        // Validasi Move
        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            // Eksekusi Logika
            cell.addOrb(currentPlayer, gm.getBoard());

            gm.checkEliminations();
            Player winner = gm.checkWinner();

            if (winner != null) {
                // Handle Game Over
                if (onGameOver != null) {
                    onGameOver.run();
                }
            } else {
                // 3. Ganti Giliran jika game belum selesai
                gm.nextTurn();
                
                // Beritahu UI kalau giliran berubah
                if (onTurnChanged != null) {
                    onTurnChanged.run();
                }
            }

        } else {
            System.out.println("Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }
}