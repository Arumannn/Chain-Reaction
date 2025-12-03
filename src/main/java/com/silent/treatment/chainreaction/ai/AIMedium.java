package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI Medium menggunakan Minimax dengan depth 2.
 * Lebih pintar dari Easy karena bisa melihat 2 langkah ke depan.
 */
public class AIMedium extends AIBase {

    private final MinimaxEngine minimaxEngine;
    private final Random random;

    public AIMedium() {
        this.minimaxEngine = new MinimaxEngine(2); // Depth 2 untuk medium
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
     * Pilih cell yang paling dekat ke critical mass.
     */
    private Cell fallbackMove(List<Cell> validMoves, Player aiPlayer) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null; // Safety: no valid moves
        }

        List<Cell> bestCells = new ArrayList<>();
        int maxOrbs = -1;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            int orbs = cell.getOrbs();
            // Prioritaskan cell yang sudah dimiliki dan hampir penuh
            if (cell.getOwner() != null && cell.getOwner().equals(aiPlayer)) {
                if (orbs > maxOrbs) {
                    maxOrbs = orbs;
                    bestCells.clear();
                    bestCells.add(cell);
                } else if (orbs == maxOrbs) {
                    bestCells.add(cell);
                }
            }
        }

        // Jika ada cell yang dimiliki, pilih random dari best
        if (!bestCells.isEmpty()) {
            return bestCells.get(random.nextInt(bestCells.size()));
        }

        // Jika tidak ada cell yang dimiliki, pilih random dari validMoves
        // Filter null cells
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
