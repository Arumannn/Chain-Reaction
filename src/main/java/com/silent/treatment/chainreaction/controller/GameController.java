package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.application.Platform;

public class GameController {

    protected Runnable onTurnChanged; 
    protected Runnable onGameOver;
    protected Runnable onAnimationStart; 
    protected Runnable onGameStateUpdated; 

    public GameController() {
        // Callback ini dipanggil saat animasi ledakan selesai
        ExplosionQueue.getInstance().setOnQueueEmpty(this::checkGameStatus);
    }

    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    public void setOnAnimationStart(Runnable onAnimationStart) {
        this.onAnimationStart = onAnimationStart;
    }

    public void setOnGameStateUpdated(Runnable onGameStateUpdated) {
        this.onGameStateUpdated = onGameStateUpdated;
    }

    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();

        // [CHANGED] Removed the 'isProcessing' check to allow moves during explosions
        // if (ExplosionQueue.getInstance().isProcessing()) { return; }

        if (gm.isGameOver()) {
            System.out.println("Game is Over. Please Reset");
            return;
        }

        Player currentPlayer = gm.getCurrentPlayer();

        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            // 1. Eksekusi Move
            cell.addOrb(currentPlayer, gm.getBoard());
            currentPlayer.setHasPlayed(true); 
            notifyGameStateUpdated();

            // 2. Trigger Animasi (jika ada ledakan)
            if (onAnimationStart != null) {
                onAnimationStart.run();
            }

            // [CHANGED] IMMEDIATE TURN SWITCH
            // Kita langsung oper giliran tanpa menunggu ledakan selesai
            gm.nextTurn();
            if (onTurnChanged != null) {
                onTurnChanged.run();
            }

        } else {
            System.out.println("Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }

    /**
     * Dipanggil ketika serangkaian ledakan selesai.
     * Cek apakah ada yang mati atau menang.
     */
    private void checkGameStatus() {
        Platform.runLater(() -> {
            GameManager gm = GameManager.getInstance();

            if (gm.isGameOver()) return;

            // 1. Cek Eliminasi (Siapa yang orbs-nya habis)
            gm.checkEliminations();
            notifyGameStateUpdated();
            
            // 2. Cek Pemenang
            Player winner = gm.checkWinner();

            if (winner != null) {
                if (onGameOver != null) {
                    onGameOver.run();
                }
            } else {
                // [NEW] Cek Keadaan Khusus:
                // Jika pemain yang "Sedang Jalan" (CurrentPlayer) tiba-tiba mati 
                // karena ledakan susulan dari pemain sebelumnya, kita harus skip dia.
                if (!gm.getCurrentPlayer().isAlive()) {
                    gm.nextTurn(); 
                    if (onTurnChanged != null) {
                        onTurnChanged.run();
                    }
                }
            }
        });
    }

    private void notifyGameStateUpdated() {
        if (onGameStateUpdated != null) {
            Platform.runLater(onGameStateUpdated);
        }
    }
}