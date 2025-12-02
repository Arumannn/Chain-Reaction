package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class GameController {

    protected Runnable onTurnChanged; // Callback untuk update UI luar
    protected Runnable onGameOver;
    protected Runnable onAnimationStart; // Callback untuk memulai animasi
    
    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }
    
    public void setOnAnimationStart(Runnable onAnimationStart) {
        this.onAnimationStart = onAnimationStart;
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
            
            // Trigger animasi jika ada explosions yang di-queue
            if (onAnimationStart != null) {
                onAnimationStart.run();
            }

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