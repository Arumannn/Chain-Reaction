package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.ai.AITutorial;
import com.silent.treatment.chainreaction.ai.AIEasy;
import com.silent.treatment.chainreaction.ai.AIMedium;
import com.silent.treatment.chainreaction.ai.AIHard;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.DifficultySelectionView.Difficulty; // Menggunakan enum Difficulty yang sudah dibuat
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// AIController bertugas MENDISPATCH giliran ke Engine AI yang tepat
public class AIController {

    private final GameController gameController;
    private final Difficulty difficulty;

    // Safety flag untuk prevent multiple simultaneous AI moves
    private boolean isExecuting = false;

    // Inisialisasi Engine AI (Objek dibuat sekali)
    private final AITutorial aiTutorial;
    private final AIEasy aiEasy;
    private final AIMedium aiMedium;
    private final AIHard aiHard;

    public AIController(GameController gameController, Difficulty difficulty) {
        this.gameController = gameController;
        this.difficulty = difficulty;

        // Inisialisasi semua engine AI
        this.aiTutorial = new AITutorial();
        this.aiEasy = new AIEasy();
        this.aiMedium = new AIMedium();
        this.aiHard = new AIHard();
    }

    // Dipanggil saat giliran AI tiba
    public void performMove() {
        // Safety: Prevent multiple execution
        if (isExecuting) {
            System.out.println("AI already executing, skipping...");
            return;
        }

        GameManager gm = GameManager.getInstance();
        if (gm.isGameOver())
            return;

        // Tentukan delay agar terlihat natural (Thinking Time)
        double delayTime = 0.5;
        if (difficulty == Difficulty.EASY)
            delayTime = 0.8;
        else if (difficulty == Difficulty.MEDIUM)
            delayTime = 1.2;
        else if (difficulty == Difficulty.HARD)
            delayTime = 1.5;

        // Delay sebelum eksekusi move
        PauseTransition pause = new PauseTransition(Duration.seconds(delayTime));
        pause.setOnFinished(e -> executeMove());
        pause.play();
    }

    private void executeMove() {
        isExecuting = true;

        try {
            GameManager gm = GameManager.getInstance();
            Player aiPlayer = gm.getCurrentPlayer();

            // Safety check: Pastikan ini benar giliran AI (check by name pattern)
            // AI players have names like "AI Bot", "AI Bot 1", "AI Bot 2", etc.
            if (!aiPlayer.getName().contains("AI Bot")) {
                // Jika ini bukan AI Bot, jangan execute move
                return;
            }

            Cell targetCell = null;

            // Dispatching: Memilih engine yang tepat
            switch (difficulty) {
                case TUTORIAL_NOOB:
                    targetCell = aiTutorial.chooseMove(gm);
                    break;
                case EASY:
                    targetCell = aiEasy.chooseMove(gm);
                    break;
                case MEDIUM:
                    targetCell = aiMedium.chooseMove(gm);
                    break;
                case HARD:
                    targetCell = aiHard.chooseMove(gm);
                    break;
            }

            // Eksekusi Move
            if (targetCell != null) {
                // Eksekusi via GameController agar UI terupdate dan rule tervalidasi
                gameController.handleCellClick(targetCell);
            } else {
                // Fallback: Tidak ada move valid, skip turn atau end game
                System.err.println("AI Warning: No valid move found for " + aiPlayer.getName());

                // Force next turn untuk avoid freeze
                gm.nextTurn();
                if (gameController.onTurnChanged != null) {
                    gameController.onTurnChanged.run();
                }
            }
        } finally {
            // Always reset flag
            isExecuting = false;
        }
    }
}