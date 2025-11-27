package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.function.Consumer;

public class TutorialController extends GameController {

    private int step = 0;
    private final Consumer<String> instructionCallback;
    private final Runnable onFinishCallback;
    private Runnable onTurnChanged;
    private Runnable onGameOver; // Callback untuk memunculkan Popup Win
    private boolean isAiTurn = false;

    public TutorialController(Consumer<String> instructionCallback, Runnable onFinishCallback) {
        this.instructionCallback = instructionCallback;
        this.onFinishCallback = onFinishCallback;

        // Delay sedikit sebelum mulai
        PauseTransition startDelay = new PauseTransition(Duration.seconds(0.5));
        startDelay.setOnFinished(e -> nextStep());
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

    @Override
    public void handleCellClick(Cell cell) {
        if (isAiTurn) return;

        GameManager gm = GameManager.getInstance();
        Player playerUser = gm.getPlayers().get(0);

        switch (step) {
            case 1: // STEP 1: Klik Pojok (0,0)
                if (cell.getOrbs() == 0 && cell.getX() == 0 && cell.getY() == 0) {
                    processMove(cell, playerUser);
                    performScriptedAiMove(1);
                } else {
                    instructionCallback.accept("Salah! Klik kotak KOSONG di POJOK KIRI ATAS (0,0).");
                }
                break;

            case 2: // STEP 2: Tumpuk di (0,0) jadi 2
                if (cell.getOwner() == playerUser && cell.getX() == 0 && cell.getY() == 0) {
                    processMove(cell, playerUser); // Sekarang (0,0) jadi 2 (Kritis)
                    // AI akan merespon dengan menumpuk punya dia juga
                    performScriptedAiMove(2);
                } else {
                    instructionCallback.accept("Klik lagi pada ORB MERAH di pojok (0,0) untuk menambahnya.");
                }
                break;

            case 3: // STEP 3: Ledakkan (0,0) -> Menyebar tapi tidak kena hijau
                if (cell.getX() == 0 && cell.getY() == 0) {
                    // Ledakan terjadi! (0,0) meledak mengisi (0,1) dan (1,0)
                    processMove(cell, playerUser);

                    // AI merespon dengan memperkuat pertahanannya
                    performScriptedAiMove(3);
                } else {
                    instructionCallback.accept("Klik (0,0) untuk MELEDAKKANNYA! Jangan khawatir, musuh masih jauh.");
                }
                break;

            case 4: // STEP 4: Musuh sudah kuat di (0,3). Kita ancam dari samping (1,3).
                // Kita minta user klik (1,3) yaitu sel kosong di bawah musuh
                if (cell.getOrbs() == 0 && cell.getX() == 1 && cell.getY() == 3) {
                    processMove(cell, playerUser);

                    // Lanjut ke step berikutnya (Finishing)
                    nextStep();
                } else {
                    instructionCallback.accept("Strategi: Dekati musuh! Klik kotak kosong di (1,3), tepat di bawah orb Hijau.");
                }
                break;

            case 5: // STEP 5: Isi terus (1,3) sampai meledak dan menang
                if (cell.getX() == 1 && cell.getY() == 3) {
                    processMove(cell, playerUser);

                    // Cek jika sudah menang (Logic GM akan otomatis cek winner)
                    if (gm.getWinner() != null) {
                        instructionCallback.accept("CHAINS REACTION BERHASIL!");
                        if (onGameOver != null) onGameOver.run(); // Panggil Popup Win
                    } else {
                        // Jika belum meledak, suruh klik lagi
                        instructionCallback.accept("Terus! Isi lagi sampai meledak (Kapasitas sel tengah = 4).");
                    }
                } else {
                    instructionCallback.accept("Fokus! Klik terus pada orbs barumu di (1,3) sampai meledak.");
                }
                break;
        }
    }

    private void performScriptedAiMove(int currentStep) {
        isAiTurn = true;
        instructionCallback.accept("Lawan (AI) sedang bergerak...");

        PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
        pause.setOnFinished(e -> {
            GameManager gm = GameManager.getInstance();
            Player aiPlayer = gm.getPlayers().get(1);

            if (currentStep == 1) {
                // AI Taruh agak jauh di (0,3) (Aman dari ledakan pertama user)
                Cell target = gm.getBoard().getCell(0, 3);
                processMove(target, aiPlayer);
                nextStep();
            }
            else if (currentStep == 2) {
                // AI Tumpuk di (0,3) -> Jadi 2 Orb
                Cell target = gm.getBoard().getCell(0, 3);

                // Cheat: Pastikan isinya 1 dulu biar nambah jadi 2
                if (target.getOrbs() == 0) target.setOrbs(1);

                target.addOrb(aiPlayer, gm.getBoard());
                if (onTurnChanged != null) onTurnChanged.run();

                nextStep();
            }
            else if (currentStep == 3) {
                // AI Tumpuk lagi di (0,3) -> Jadi 3 Orb (KRITIS untuk pinggir)
                // Ini membuat situasi tegang: Kalau meledak, efeknya besar.
                Cell target = gm.getBoard().getCell(0, 3);
                target.addOrb(aiPlayer, gm.getBoard());
                if (onTurnChanged != null) onTurnChanged.run();

                nextStep();
            }

            isAiTurn = false;
        });
        pause.play();
    }

    private void processMove(Cell cell, Player player) {
        GameManager gm = GameManager.getInstance();
        cell.addOrb(player, gm.getBoard());

        // Cek Win secara manual jika diperlukan, tapi biasanya GM handle
        Player winner = gm.checkWinner();

        if (onTurnChanged != null) onTurnChanged.run();
    }

    private void nextStep() {
        step++;
        switch (step) {
            case 1:
                instructionCallback.accept("STEP 1: POSISI AWAL\nKlik kotak pojok kiri atas (0,0).");
                break;
            case 2:
                instructionCallback.accept("STEP 2: PERSIAPAN\nMusuh mengambil posisi di kanan (0,3).\nKlik lagi (0,0) untuk menambah orb.");
                break;
            case 3:
                instructionCallback.accept("STEP 3: LEDAKAN AMAN\nOrb di (0,0) sudah penuh. Klik sekali lagi untuk MELEDAKKANNYA!\nLedakan ini akan menyebar ke dekatnya tapi belum mengenai musuh.");
                break;
            case 4:
                instructionCallback.accept("STEP 4: PENYERANGAN\nLihat! Orb Hijau di (0,3) sudah besar (isi 3).\nKita harus serang dari dekat.\nKlik kotak kosong di BAWAHNYA (1,3).");
                break;
            case 5:
                instructionCallback.accept("STEP 5: CHAIN REACTION\nSekarang isi terus orb di (1,3) sampai meledak!\nLedakanmu akan mengenai Orb Hijau -> Hijau Meledak -> Kamu Menang.");
                break;
        }
    }
}