package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

/**
 * MODE 2: Easy AI. Menggunakan Minimax Depth 1 dengan evaluasi standar.
 */
public class AIEasy {

    private final MinimaxEngine engine;

    public AIEasy() {
        // Depth 1 (Hanya evaluasi posisi saat ini)
        this.engine = new MinimaxEngine(new StandardEvaluator(), 1);
    }

    public Cell chooseMove(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        // Asumsi musuh adalah Player 1 jika AI adalah Player 2
        Player enemy = gm.getPlayers().get(0).equals(ai) ? gm.getPlayers().get(1) : gm.getPlayers().get(0);

        AIMove bestMove = engine.findBestMove(gm.getBoard(), ai, enemy);

        if (bestMove == null) {
            // Fallback: Cari sel kosong
            return findAnyEmptyCell(gm);
        }

        return gm.getBoard().getCell(bestMove.x, bestMove.y);
    }

    // Helper sederhana untuk fallback (diambil dari logika AIController lama Anda)
    private Cell findAnyEmptyCell(GameManager gm) {
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c.getOwner() == null) return c;
            }
        }
        return null;
    }
}