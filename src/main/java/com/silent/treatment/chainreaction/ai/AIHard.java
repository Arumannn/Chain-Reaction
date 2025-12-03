package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIHard extends AIBase {

    private final MinimaxEngine minimaxEngine;
    private final Random random;

    public AIHard() {
        this.minimaxEngine = new MinimaxEngine(3); // Depth 3 untuk hard
        this.random = new Random();
    }

    @Override
    public Cell chooseMove(GameManager gm) {
        Player aiPlayer = gm.getCurrentPlayer();
        List<Cell> validMoves = collectValidMoves(gm, aiPlayer);

        if (validMoves.isEmpty()) {
            return null;
        }

        // Gunakan Minimax untuk mencari best move
        Cell bestMove = minimaxEngine.findBestMove(gm, aiPlayer, validMoves);

        if (bestMove != null) {
            return bestMove;
        }

        // Fallback jika minimax gagal
        return fallbackMove(validMoves, aiPlayer);
    }

    /**
     * Fallback move jika minimax gagal.
     * Pilih cell yang paling strategis (dekat critical mass atau blocking).
     */
    private Cell fallbackMove(List<Cell> validMoves, Player aiPlayer) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null; // Safety: no valid moves
        }

        List<Cell> bestCells = new ArrayList<>();
        int maxScore = Integer.MIN_VALUE;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            int score = 0;
            int orbs = cell.getOrbs();
            int criticalMass = cell.getCriticalMass();

            // Prioritaskan cell yang hampir critical
            if (cell.getOwner() != null && cell.getOwner().equals(aiPlayer)) {
                score += (orbs * 10);

                // Extra bonus jika hampir critical
                if (orbs == criticalMass - 1) {
                    score += 50;
                }
            } else if (cell.getOwner() == null) {
                // Empty cell, prioritaskan corner dan edge
                score += (4 - criticalMass) * 5; // Corner > Edge > Center
            }

            if (score > maxScore) {
                maxScore = score;
                bestCells.clear();
                bestCells.add(cell);
            } else if (score == maxScore) {
                bestCells.add(cell);
            }
        }

        // Pilih random dari best cells
        if (!bestCells.isEmpty()) {
            return bestCells.get(random.nextInt(bestCells.size()));
        }

        // Jika tidak ada, pilih random dari validMoves (filter null)
        List<Cell> nonNullMoves = new ArrayList<>();
        for (Cell c : validMoves) {
            if (c != null)
                nonNullMoves.add(c);
        }

        if (!nonNullMoves.isEmpty()) {
            return nonNullMoves.get(random.nextInt(nonNullMoves.size()));
        }

        return null; // No valid move at all
    }
}
