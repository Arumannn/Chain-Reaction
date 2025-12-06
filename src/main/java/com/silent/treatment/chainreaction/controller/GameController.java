package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.application.Platform;
import java.util.logging.Logger;

public class GameController {

    private static final Logger logger = Logger.getLogger(GameController.class.getName());

    protected Runnable onTurnChanged;
    protected Runnable onGameOver;
    protected Runnable onHumansDefeated;
    protected Runnable onAnimationStart;
    protected Runnable onGameStateUpdated;

    public GameController() {
        ExplosionQueue.getInstance().setOnQueueEmpty(this::checkGameStatus);
    }

    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    public void setOnHumansDefeated(Runnable onHumansDefeated) {
        this.onHumansDefeated = onHumansDefeated;
    }

    public void setOnAnimationStart(Runnable onAnimationStart) {
        this.onAnimationStart = onAnimationStart;
    }

    public void setOnGameStateUpdated(Runnable onGameStateUpdated) {
        this.onGameStateUpdated = onGameStateUpdated;
    }

    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();

        // Jika semua human sudah kalah, klik apa pun hanya akan memunculkan kembali
        // lose screen
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
        logger.info(String.format("[CLICK] Player clicked cell (%d,%d)", cell.getX(), cell.getY()));
        logger.info(() -> "[CLICK] Status ExplosionQueue.isProcessing(): " + isBusy);

        if (isBusy) {
            logger.info("[CLICK] BLOCKED! Input ditolak karena sedang ada animasi/ledakan.");
            return;
        }

        if (gm.isGameOver()) {
            logger.info("Game is Over. Please Reset");
            return;
        }

        Player currentPlayer = gm.getCurrentPlayer();

        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            logger.info("[LOGIC] Move Valid. Adding Orb...");

            // 2. Eksekusi Move
            cell.addOrb(currentPlayer, gm.getBoard());
            currentPlayer.setHasPlayed(true);
            notifyGameStateUpdated();

            // 3. Cek Status Ledakan SETELAH addOrb
            boolean isExploding = ExplosionQueue.getInstance().isProcessing();
            logger.info(() -> "[LOGIC] Apakah move ini memicu ledakan? " + isExploding);

            if (isExploding) {
                logger.info("[LOGIC] MELEDAK! Memicu animasi & Menunggu selesai...");
                if (onAnimationStart != null) {
                    onAnimationStart.run();
                }
            } else {
                logger.info("[LOGIC] AMAN. Pindah giliran manual.");
                gm.nextTurn();
                if (onTurnChanged != null) {
                    onTurnChanged.run();
                }
            }

        } else {
            logger.info(() -> "Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }

    private void checkGameStatus() {
        logger.info("[STATUS] checkGameStatus dipanggil (Semua ledakan selesai).");
        Platform.runLater(this::processPostExplosionStatus);
    }

    private void processPostExplosionStatus() {
        GameManager gm = GameManager.getInstance();

        if (gm.isGameOver())
            return;

        gm.checkEliminations();
        notifyGameStateUpdated();

        handleGameFlow(gm);
    }

    private void handleGameFlow(GameManager gm) {
        Player winner = gm.checkWinner();

        // Cek apakah semua pemain human sudah kalah
        if (gm.consumeHumansDefeatedFlag() && onHumansDefeated != null) {
            onHumansDefeated.run();
        }

        if (winner != null) {
            if (onGameOver != null)
                onGameOver.run();
        } else {
            handleNextTurn(gm);
        }
    }

    private void handleNextTurn(GameManager gm) {
        // Safety check: jika game di-reset saat animasi berjalan (players empty)
        if (gm.getPlayers().isEmpty())
            return;

        logger.info("[STATUS] Pindah giliran otomatis setelah ledakan.");
        gm.nextTurn();
        if (onTurnChanged != null)
            onTurnChanged.run();
    }

    private void notifyGameStateUpdated() {
        if (onGameStateUpdated != null) {
            Platform.runLater(onGameStateUpdated);
        }
    }
}