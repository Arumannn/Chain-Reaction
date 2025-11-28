package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AITutorial extends AIBase {
    private final Random random = new Random();

    @Override
    public Cell chooseMove(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        Player human = gm.getPlayers().get(0);

        List<AIMove> legalMoves = MoveGenerator.getLegalMoves(gm.getBoard(), ai);
        if (legalMoves.isEmpty()) return null;

        AIMove worstMove = null;
        double maxScore = Double.NEGATIVE_INFINITY; // Mencari skor terendah

        for (AIMove move : legalMoves) {
            Cell cell = gm.getBoard().getCell(move.x, move.y);
            double score = 0;

            // Logika Evaluasi TERBALIK (Noob Logic)

            // 1. Hindari Menciptakan Chain Reaction (Skor Tinggi agar tidak dipilih)
            if (cell.getOrbs() == cell.getCriticalMass() - 1) score = -100;

            // 2. Dekati Ancaman Musuh (Skor Rendah = Langkah Buruk)
            if (findCriticalThreat(human, gm) != null) score = 100;

            // 3. Ambil Sel Aman (Skor Tinggi = Buruk)
            if (cell.getCriticalMass() == 4) score += 50;

            // Pilih langkah yang paling sedikit merugikan
            if (score > maxScore) {
                maxScore = score;
                worstMove = move;
            }
        }

        // Final: Ambil langkah terburuk (skor tertinggi) atau random jika semua langkah buruk
        return worstMove != null ? gm.getBoard().getCell(worstMove.x, worstMove.y) : gm.getBoard().getCell(legalMoves.get(0).x, legalMoves.get(0).y);
    }
}