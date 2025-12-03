package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.model.Board;
import javafx.animation.PauseTransition;
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

    private final Consumer<String> instructionCallback;
    private final Runnable onFinishCallback;
    private Runnable onTurnChanged;
    private Runnable onGameOver;

    public TutorialController(Consumer<String> instructionCallback, Runnable onFinishCallback) {
        this.instructionCallback = instructionCallback;
        this.onFinishCallback = onFinishCallback;
        this.currentPhase = Phase.DEMO_2_EXPLOSION;
        this.isProcessing = true;

        this.onGameOver = onFinishCallback;

        PauseTransition startDelay = new PauseTransition(Duration.seconds(1));
        startDelay.setOnFinished(e -> startDemoPhase());
        startDelay.play();
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

        executeDelayedMove(target, player, 1.0, false, () -> {
            executeDelayedMove(target, player, 1.0, false, () -> {
                instructionCallback.accept("Orb 3/3... Siap meledak!");
                executeDelayedMove(target, player, 1.0, false, () -> {
                    instructionCallback.accept("💥 LEDAKAN 3 ARAH! Menyebar ke 3 arah.");
                    transitionToNextPhase(4.0);
                });
            });
        });
    }

    private void demo4Explosion(Player player) {
        GameManager gm = GameManager.getInstance();
        Cell target = gm.getBoard().getCell(2, 2);

        if (target == null) {
            transitionToNextPhase(0);
            return;
        }

        executeDelayedMove(target, player, 0.8, false, () -> {
            executeDelayedMove(target, player, 0.8, false, () -> {
                executeDelayedMove(target, player, 0.8, false, () -> {
                    instructionCallback.accept("Orb 4/4... KRITIS!");
                    executeDelayedMove(target, player, 0.8, false, () -> {
                        instructionCallback.accept("💥💥 LEDAKAN MAKSIMAL! Meledak ke SEMUA 4 arah!");
                        transitionToNextPhase(5.0);
                    });
                });
            });
        });
    }

    private void transitionToNextPhase(double delay) {
        PauseTransition transition = new PauseTransition(Duration.seconds(delay));
        transition.setOnFinished(e -> {
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
            }
        });
        transition.play();
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

        if (checkGameEnd())
            return;

        instructionCallback.accept("AI sedang berpikir...");
        PauseTransition aiThink = new PauseTransition(Duration.seconds(1.0));
        aiThink.setOnFinished(e -> {
            performNoobAIMove();
            isProcessing = false;
        });
        aiThink.play();
    }

    private void performNoobAIMove() {
        GameManager gm = GameManager.getInstance();
        Player aiPlayer = gm.getPlayers().get(1);
        Player humanPlayer = gm.getPlayers().get(0);

        Cell worstMove = findWorstMove(gm.getBoard(), aiPlayer, humanPlayer);

        if (worstMove != null) {
            final Cell finalTarget = worstMove;
            PauseTransition moveDelay = new PauseTransition(Duration.seconds(0.8));
            moveDelay.setOnFinished(e -> {
                processMove(finalTarget, aiPlayer, true);
                if (!checkGameEnd()) {
                    instructionCallback.accept("Giliranmu! Klik untuk menyerang.");
                }
            });
            moveDelay.play();
        }
    }

    private void processMove(Cell cell, Player player, boolean advanceTurn) {
        GameManager gm = GameManager.getInstance();
        cell.addOrb(player, gm.getBoard());

        if (onTurnChanged != null) {
            onTurnChanged.run();
        }

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

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> onGameOver.run());
            delay.play();
            return true;
        }
        return false;
    }

    private Cell findWorstMove(Board board, Player ai, Player human) {

        Random rand = new Random();
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

        return legalMoves.get(rand.nextInt(legalMoves.size()));
    }

    private void executeDelayedMove(Cell cell, Player player, double delaySeconds, boolean advanceTurn,
            Runnable onComplete) {
        PauseTransition pause = new PauseTransition(Duration.seconds(delaySeconds));
        pause.setOnFinished(e -> {
            processMove(cell, player, advanceTurn);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        pause.play();
    }
}