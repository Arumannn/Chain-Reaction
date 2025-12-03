package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.application.Platform;

public class GameController {

    protected Runnable onTurnChanged; 
    protected Runnable onGameOver;
    protected Runnable onHumansDefeated;
    protected Runnable onAnimationStart; 
    protected Runnable onGameStateUpdated; 

    public GameController() {
        ExplosionQueue.getInstance().setOnQueueEmpty(this::checkGameStatus);
    }

    public void setOnTurnChanged(Runnable onTurnChanged) { this.onTurnChanged = onTurnChanged; }
    public void setOnGameOver(Runnable onGameOver) { this.onGameOver = onGameOver; }
    public void setOnHumansDefeated(Runnable onHumansDefeated) { this.onHumansDefeated = onHumansDefeated; }
    public void setOnAnimationStart(Runnable onAnimationStart) { this.onAnimationStart = onAnimationStart; }
    public void setOnGameStateUpdated(Runnable onGameStateUpdated) { this.onGameStateUpdated = onGameStateUpdated; }

    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();

        // Jika semua human sudah kalah, klik apa pun hanya akan memunculkan kembali lose screen
        if (gm.areHumansDefeated()) {
            if (onHumansDefeated != null) {
                onHumansDefeated.run();
            }
            return;
        }

        processMove(cell, gm);
    }

    /**
     * Jalur khusus untuk AI: tetap memproses move meskipun human sudah kalah.
     */
    public void handleAICellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();
        processMove(cell, gm);
    }

    private void processMove(Cell cell, GameManager gm) {
        // [DEBUG] Cek status saat klik
        boolean isBusy = ExplosionQueue.getInstance().isProcessing();
        System.out.println("\n[CLICK] Player clicked cell (" + cell.getX() + "," + cell.getY() + ")");
        System.out.println("[CLICK] Status ExplosionQueue.isProcessing(): " + isBusy);

        if (isBusy) {
            System.out.println("[CLICK] BLOCKED! Input ditolak karena sedang ada animasi/ledakan.");
            return;
        }

        if (gm.isGameOver()) {
            System.out.println("Game is Over. Please Reset");
            return;
        }

        Player currentPlayer = gm.getCurrentPlayer();

        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            System.out.println("[LOGIC] Move Valid. Adding Orb...");

            // 2. Eksekusi Move
            cell.addOrb(currentPlayer, gm.getBoard());
            currentPlayer.setHasPlayed(true);
            notifyGameStateUpdated();

            // 3. Cek Status Ledakan SETELAH addOrb
            boolean isExploding = ExplosionQueue.getInstance().isProcessing();
            System.out.println("[LOGIC] Apakah move ini memicu ledakan? " + isExploding);

            if (isExploding) {
                System.out.println("[LOGIC] MELEDAK! Memicu animasi & Menunggu selesai...");
                if (onAnimationStart != null) {
                    onAnimationStart.run();
                }
            } else {
                System.out.println("[LOGIC] AMAN. Pindah giliran manual.");
                gm.nextTurn();
                if (onTurnChanged != null) {
                    onTurnChanged.run();
                }
            }

        } else {
            System.out.println("Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }

    private void checkGameStatus() {
        System.out.println("[STATUS] checkGameStatus dipanggil (Semua ledakan selesai).");
        Platform.runLater(() -> {
            GameManager gm = GameManager.getInstance();

            if (gm.isGameOver()) return;

            gm.checkEliminations();
            notifyGameStateUpdated();
            
            Player winner = gm.checkWinner();

            // Cek apakah semua pemain human sudah kalah
            if (gm.consumeHumansDefeatedFlag() && onHumansDefeated != null) {
                onHumansDefeated.run();
            }

            if (winner != null) {
                if (onGameOver != null) onGameOver.run();
            } else {
                System.out.println("[STATUS] Pindah giliran otomatis setelah ledakan.");
                gm.nextTurn(); 
                if (onTurnChanged != null) onTurnChanged.run();
            }
        });
    }

    private void notifyGameStateUpdated() {
        if (onGameStateUpdated != null) {
            Platform.runLater(onGameStateUpdated);
        }
    }
}