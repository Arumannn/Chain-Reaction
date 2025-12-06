package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.model.Board;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class TutorialController extends GameController {

    private enum Phase {
        DEMO_2_EXPLOSION,
        DEMO_3_EXPLOSION,
        DEMO_4_EXPLOSION,
        INTERACTIVE_NOOB
    }

    private Phase currentPhase;
    private boolean isProcessing;
    private final Random random;
    private PauseTransition currentTransition; // Track active transition

    private final Consumer<String> instructionCallback;
    private Runnable onTurnChanged;
    private Runnable onGameOver;

    public TutorialController(Consumer<String> instructionCallback, Runnable onFinishCallback) {
        // Disable GameController's auto-turn logic for tutorial to prevent double turns
        ExplosionQueue.getInstance().setOnQueueEmpty(this::checkTutorialAnimationStatus);

        this.instructionCallback = instructionCallback;
        this.currentPhase = Phase.DEMO_2_EXPLOSION;
        this.isProcessing = true;
        this.random = new Random();

        this.onGameOver = onFinishCallback;

        this.currentTransition = new PauseTransition(Duration.seconds(1));
        this.currentTransition.setOnFinished(e -> startDemoPhase());
        this.currentTransition.play();
    }

    public void stop() {
        if (currentTransition != null) {
            currentTransition.stop();
            currentTransition = null;
        }
        // Force reset isProcessing agar UI tidak terkunci jika masuk tutorial lagi
        isProcessing = false;
    }

    @Override
    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    @Override
    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    private void startDemoPhase() {
        GameManager gm = GameManager.getInstance();
        Player demoPlayer = gm.getPlayers().get(0);

        switch (currentPhase) {
            case DEMO_2_EXPLOSION:
                instructionCallback.accept("DEMO 1: LEDAKAN SUDUT (kapasitas 2)");
                demo2Explosion(demoPlayer);
                break;
            case DEMO_3_EXPLOSION:
                instructionCallback.accept("DEMO 2: LEDAKAN PINGGIR (kapasitas 3)");
                demo3Explosion(demoPlayer);
                break;
            case DEMO_4_EXPLOSION:
                instructionCallback.accept("DEMO 3: LEDAKAN TENGAH (kapasitas 4)");
                demo4Explosion(demoPlayer);
                break;
            default:
                break;
        }
    }

    private void demo2Explosion(Player player) {
        GameManager gm = GameManager.getInstance();
        Cell target = gm.getBoard().getCell(0, 0);

        if (target == null) {
            transitionToNextPhase(0);
            return;
        }

        executeDelayedMove(target, player, 1.5, false, () -> {
            instructionCallback.accept("Menambah orb kedua...");
            executeDelayedMove(target, player, 1.5, false, () -> {
                instructionCallback.accept("💥 BOOM! Pojok meledak! Kapasitas pojok = 2 orb.");
                transitionToNextPhase(4.0);
            });
        });
    }

    private void demo3Explosion(Player player) {
        GameManager gm = GameManager.getInstance();
        Cell target = gm.getBoard().getCell(0, 2);

        if (target == null) {
            transitionToNextPhase(0);
            return;
        }

        executeDelayedMove(target, player, 1.0, false, () -> executeDelayedMove(target, player, 1.0, false, () -> {
            instructionCallback.accept("Orb 3/3... Siap meledak!");
            executeDelayedMove(target, player, 1.0, false, () -> {
                instructionCallback.accept("💥 LEDAKAN 3 ARAH! Menyebar ke 3 arah.");
                transitionToNextPhase(4.0);
            });
        }));
    }

    private void demo4Explosion(Player player) {
        GameManager gm = GameManager.getInstance();
        Cell target = gm.getBoard().getCell(2, 2);

        if (target == null) {
            transitionToNextPhase(0);
            return;
        }

        executeDelayedMove(target, player, 0.8, false, () -> executeDelayedMove(target, player, 0.8, false,
                () -> executeDelayedMove(target, player, 0.8, false, () -> {
                    instructionCallback.accept("Orb 4/4... KRITIS!");
                    executeDelayedMove(target, player, 0.8, false, () -> {
                        instructionCallback.accept("💥💥 LEDAKAN MAKSIMAL! Meledak ke SEMUA 4 arah!");
                        transitionToNextPhase(5.0);
                    });
                })));
    }

    private void transitionToNextPhase(double delay) {
        currentTransition = new PauseTransition(Duration.seconds(delay));
        currentTransition.setOnFinished(e -> {
            resetBoardData();
            if (onTurnChanged != null)
                onTurnChanged.run();

            switch (currentPhase) {
                case DEMO_2_EXPLOSION:
                    currentPhase = Phase.DEMO_3_EXPLOSION;
                    instructionCallback.accept("Membersihkan papan untuk demo berikutnya...");
                    startDemoPhase();
                    break;
                case DEMO_3_EXPLOSION:
                    currentPhase = Phase.DEMO_4_EXPLOSION;
                    instructionCallback.accept("Bersiap untuk demo terakhir...");
                    startDemoPhase();
                    break;
                case DEMO_4_EXPLOSION:
                    currentPhase = Phase.INTERACTIVE_NOOB;
                    startInteractiveMode();
                    break;
                default:
                    break;
            }
        });
        currentTransition.play();
    }

    private void resetBoardData() {
        GameManager gm = GameManager.getInstance();
        for (int i = 0; i < gm.getBoard().getWidth(); i++) {
            for (int j = 0; j < gm.getBoard().getHeight(); j++) {
                Cell cell = gm.getBoard().getCell(i, j);

                if (cell != null) {
                    cell.setOrbs(0);
                }
            }
        }
    }

    private void startInteractiveMode() {
        instructionCallback.accept(
                "🎮 MODE LATIHAN\n\nGiliranmu! Kalahkan AI.\n(AI di mode ini sangat lemah, manfaatkan untuk belajar menang)");
        isProcessing = false;
        // Make sure queue is empty and listener is ready
        if (ExplosionQueue.getInstance().peekNext() == null) {
            ExplosionQueue.getInstance().setOnQueueEmpty(this::checkTutorialAnimationStatus);
        }

        // Ensure UI reflects the starting player (YOU)
        if (onTurnChanged != null)
            onTurnChanged.run();
    }

    private void checkTutorialAnimationStatus() {
        if (currentPhase != Phase.INTERACTIVE_NOOB)
            return;

        Platform.runLater(() -> {
            if (checkGameEnd())
                return;

            GameManager gm = GameManager.getInstance();
            Player currentPlayer = gm.getCurrentPlayer();

            // If it is now AI's turn (Index 1), it means User just finished
            // moving/exploding
            if (gm.getPlayers().indexOf(currentPlayer) == 1) {
                // FORCE UI UPDATE: Ensure color changes to AI's color
                if (onTurnChanged != null)
                    onTurnChanged.run();
                scheduleAIMove();
            }
            // If it is now User's turn (Index 0), it means AI just finished
            // moving/exploding
            else if (gm.getPlayers().indexOf(currentPlayer) == 0) {
                isProcessing = false;
                // FORCE UI UPDATE: Ensure color changes to User's color
                if (onTurnChanged != null)
                    onTurnChanged.run();
                instructionCallback.accept("Giliranmu! Klik untuk menyerang.");
            }
        });
    }

    private void scheduleAIMove() {
        instructionCallback.accept("AI sedang berpikir...");
        currentTransition = new PauseTransition(Duration.seconds(1.0));
        currentTransition.setOnFinished(e -> performNoobAIMove());
        currentTransition.play();
    }

    @Override
    public void handleCellClick(Cell cell) {
        if (currentPhase != Phase.INTERACTIVE_NOOB || isProcessing)
            return;

        GameManager gm = GameManager.getInstance();
        Player human = gm.getPlayers().get(0);

        if (cell.getOwner() != null && !cell.getOwner().equals(human)) {
            instructionCallback.accept("❌ Tidak bisa! Sel ini milik musuh.");
            return;
        }

        isProcessing = true;
        processMove(cell, human, true);

        // If explosion started, wait for checkTutorialAnimationStatus
        if (ExplosionQueue.getInstance().isProcessing()) {
            return;
        }

        // If no explosion, trigger AI manually
        if (checkGameEnd())
            return;
        scheduleAIMove();
    }

    private void performNoobAIMove() {
        GameManager gm = GameManager.getInstance();
        Player aiPlayer = gm.getPlayers().get(1);

        Cell worstMove = findWorstMove(gm.getBoard(), aiPlayer);

        if (worstMove != null) {
            final Cell finalTarget = worstMove;
            currentTransition = new PauseTransition(Duration.seconds(0.8));
            currentTransition.setOnFinished(e -> {
                processMove(finalTarget, aiPlayer, true);

                // If explosion started, wait for callback to unlock UI
                if (ExplosionQueue.getInstance().isProcessing()) {
                    return;
                }

                // If no explosion, unlock UI manually
                if (!checkGameEnd()) {
                    isProcessing = false;
                    instructionCallback.accept("Giliranmu! Klik untuk menyerang.");
                }
            });
            currentTransition.play();
        }
    }

    private void processMove(Cell cell, Player player, boolean advanceTurn) {
        GameManager gm = GameManager.getInstance();
        cell.addOrb(player, gm.getBoard());

        // Update informasi turn / UI
        if (onTurnChanged != null) {
            onTurnChanged.run();
        }

        // Trigger animasi ledakan jika ada chain reaction yang berjalan
        boolean isExploding = ExplosionQueue.getInstance().isProcessing();
        if (isExploding && onAnimationStart != null) {
            onAnimationStart.run();
        }

        // Tutorial tetap mengatur turn secara manual
        if (advanceTurn) {
            gm.nextTurn();
        }
    }

    private boolean checkGameEnd() {
        GameManager gm = GameManager.getInstance();
        gm.checkEliminations();
        Player winner = gm.checkWinner();

        if (winner != null) {
            String msg = winner.getName().equals("YOU")
                    ? "🎉 SELAMAT! Anda Menang Tutorial!"
                    : "💀 AI Menang! Jangan menyerah, coba lagi.";

            instructionCallback.accept(msg);

            instructionCallback.accept(msg);

            currentTransition = new PauseTransition(Duration.seconds(2));
            currentTransition.setOnFinished(e -> onGameOver.run());
            currentTransition.play();
            return true;
        }
        return false;
    }

    private Cell findWorstMove(Board board, Player ai) {
        List<Cell> legalMoves = new ArrayList<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell c = board.getCell(i, j);

                if (c != null && (c.getOwner() == null || c.getOwner().equals(ai))) {
                    legalMoves.add(c);
                }
            }
        }

        if (legalMoves.isEmpty())
            return null;

        return legalMoves.get(random.nextInt(legalMoves.size()));
    }

    private void executeDelayedMove(Cell cell, Player player, double delaySeconds, boolean advanceTurn,
            Runnable onComplete) {
        currentTransition = new PauseTransition(Duration.seconds(delaySeconds));
        currentTransition.setOnFinished(e -> {
            processMove(cell, player, advanceTurn);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        currentTransition.play();
    }
}