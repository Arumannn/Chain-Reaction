package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.List;
import java.util.Collections;

/**
 * Mesin pencarian yang menggunakan Minimax (saat ini Depth 1/Static Evaluation).
 */
public class MinimaxEngine {

    private final BoardEvaluator evaluator;
    private final int maxDepth;

    public MinimaxEngine(BoardEvaluator evaluator, int depth) {
        this.evaluator = evaluator;
        this.maxDepth = depth;
    }

    /**
     * Menemukan langkah terbaik untuk pemain AI saat ini.
     * Untuk Minimax Depth > 1, kode ini perlu mekanisme untuk mengkloning papan (Board).
     * Saat ini, ini adalah Minimax Depth 1 (Static Evaluation).
     */
    public AIMove findBestMove(Board board, Player aiPlayer, Player enemyPlayer) {
        List<AIMove> moves = MoveGenerator.getLegalMoves(board, aiPlayer);

        AIMove bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        if (moves.isEmpty()) return null;

        for (AIMove move : moves) {
            // Nilai papan jika langkah ini diambil.
            // Papan tidak dikloning, skor dihitung dari potensi saat ini.
            double score = evaluateMovePotential(board, move, aiPlayer, enemyPlayer);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * Menghitung skor potensi dari suatu langkah (Static Evaluation + Heuristik).
     */
    private double evaluateMovePotential(Board board, AIMove move, Player ai, Player enemy) {
        // 1. Dapatkan Cell target
        Cell cell = board.getCell(move.x, move.y);

        // 2. Nilai posisi papan saat ini (Dasar)
        double score = evaluator.evaluate(board, ai, enemy);

        // 3. Heuristik: Chain Reaction Potential
        // Jika langkah ini menyebabkan ledakan, beri bonus besar (Minimax harus agresif)
        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += 1000; // Bonus Ledakan Besar

            // Cek apakah ledakan ini berpotensi mengenai musuh (Capture bonus)
            for (Cell n : cell.getNeighbors()) {
                if (n.getOwner() != null && n.getOwner().equals(enemy)) {
                    score += 500; // Capture bonus
                }
            }
        }

        return score;
    }
}