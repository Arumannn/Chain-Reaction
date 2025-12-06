package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.ai.AITutorial;
import com.silent.treatment.chainreaction.ai.AIEasy;
import com.silent.treatment.chainreaction.ai.AIMedium;
import com.silent.treatment.chainreaction.ai.AIHard;
import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.view.DifficultySelectionView.Difficulty;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.logging.Logger;

public class AIController {

    private static final Logger logger = Logger.getLogger(AIController.class.getName());
    private final GameController gameController;
    private final Difficulty difficulty;

    private boolean isExecuting = false;

    private final AITutorial aiTutorial;
    private final AIEasy aiEasy;
    private final AIMedium aiMedium;
    private final AIHard aiHard;

    public AIController(GameController gameController, Difficulty difficulty) {
        this.gameController = gameController;
        this.difficulty = difficulty;

        this.aiTutorial = new AITutorial();
        this.aiEasy = new AIEasy();
        this.aiMedium = new AIMedium();
        this.aiHard = new AIHard();
    }

    public void performMove() {

        if (isExecuting) {
            logger.info("AI already executing, skipping...");
            return;
        }

        GameManager gm = GameManager.getInstance();
        if (gm.isGameOver())
            return;

        double delayTime = 0.5;
        if (difficulty == Difficulty.EASY)
            delayTime = 0.8;
        else if (difficulty == Difficulty.MEDIUM)
            delayTime = 1.2;
        else if (difficulty == Difficulty.HARD)
            delayTime = 1.5;

        PauseTransition pause = new PauseTransition(Duration.seconds(delayTime));
        pause.setOnFinished(e -> executeMove());
        pause.play();
    }

    private void executeMove() {
        isExecuting = true;

        try {
            GameManager gm = GameManager.getInstance();

            if (gm.getPlayers() == null || gm.getPlayers().isEmpty()) {
                logger.info("[AI] Game reset detected. Cancelling move.");
                return;
            }

            Player aiPlayer = gm.getCurrentPlayer();

            if (!aiPlayer.getName().contains("AI Bot")) {
                return;
            }

            Cell targetCell = null;

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

            if (targetCell != null) {
                // Gunakan jalur khusus AI agar tetap bisa bermain meskipun human sudah kalah
                gameController.handleAICellClick(targetCell);
            } else {
                logger.warning(() -> "AI Warning: No valid move found for " + aiPlayer.getName());

                gm.nextTurn();
                if (gameController.onTurnChanged != null) {
                    gameController.onTurnChanged.run();
                }
            }
        } finally {
            isExecuting = false;
        }
    }
}