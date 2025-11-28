package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.ai.AITutorial;
import com.silent.treatment.chainreaction.ai.AIEasy;
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

    // Inisialisasi Engine AI (Objek dibuat sekali)
    private final AITutorial aiTutorial;
    private final AIEasy aiEasy;
    // TBD: private final AIMedium aiMedium;
    // TBD: private final AIHard aiHard;

    public AIController(GameController gameController, Difficulty difficulty) {
        this.gameController = gameController;
        this.difficulty = difficulty;

        // Inisialisasi semua engine AI
        this.aiTutorial = new AITutorial();
        this.aiEasy = new AIEasy();
        // TBD: this.aiMedium = new AIMedium();
        // TBD: this.aiHard = new AIHard();
    }

    // Dipanggil saat giliran AI tiba
    public void performMove() {
        GameManager gm = GameManager.getInstance();
        if (gm.isGameOver()) return;

        // Tentukan delay agar terlihat natural (Thinking Time)
        double delayTime = 0.5;
        if (difficulty == Difficulty.EASY) delayTime = 0.8;
        else if (difficulty == Difficulty.MEDIUM) delayTime = 1.2;
        else if (difficulty == Difficulty.HARD) delayTime = 1.5;

        // Delay sebelum eksekusi move
        PauseTransition pause = new PauseTransition(Duration.seconds(delayTime));
        pause.setOnFinished(e -> executeMove());
        pause.play();
    }

    private void executeMove() {
        GameManager gm = GameManager.getInstance();
        Player aiPlayer = gm.getCurrentPlayer();

        // Safety check: Pastikan ini benar giliran AI
        if (!aiPlayer.getName().equals("AI Bot")) {
            // Jika ini bukan AI Bot, kembalikan giliran (atau biarkan GameController yang urus)
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
                // TBD: targetCell = aiMedium.chooseMove(gm);
                targetCell = aiEasy.chooseMove(gm); // Placeholder
                break;
            case HARD:
                // TBD: targetCell = aiHard.chooseMove(gm);
                targetCell = aiEasy.chooseMove(gm); // Placeholder
                break;
        }

        // Eksekusi Move
        if (targetCell != null) {
            // Eksekusi via GameController agar UI terupdate dan rule tervalidasi
            gameController.handleCellClick(targetCell);
        } else {
            // Fallback: Lanjut giliran jika tidak ada move (board penuh)
            gm.nextTurn();
            if (gameController.onTurnChanged != null) {
                gameController.onTurnChanged.run();
            }
        }
    }
}