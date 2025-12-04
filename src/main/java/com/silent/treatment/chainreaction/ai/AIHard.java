package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI Hard - Advanced depth 3 dengan multiplayer support (2-8 player).
 * Memikirkan 3 langkah ke depan dengan strategic planning melawan SEMUA musuh.
 */
public class AIHard extends AIBase {

    private final MinimaxEngine minimaxEngine;
    private final Random random;

    public AIHard() {
        this.minimaxEngine = new MinimaxEngine(3);
        this.random = new Random();
    }

    @Override
    public Cell chooseMove(GameManager gm) {
        Player aiPlayer = gm.getCurrentPlayer();
        List<Cell> validMoves = collectValidMoves(gm, aiPlayer);

        if (validMoves.isEmpty()) {
            return null;
        }

        Cell bestMove = minimaxEngine.findBestMove(gm, aiPlayer, validMoves);

        if (bestMove != null) {
            return bestMove;
        }

        return fallbackMove(validMoves, aiPlayer);
    }

    private Cell fallbackMove(List<Cell> validMoves, Player aiPlayer) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }

        List<Cell> bestCells = findBestScoredCells(validMoves, aiPlayer);

        if (!bestCells.isEmpty()) {
            return bestCells.get(random.nextInt(bestCells.size()));
        }

        return selectRandomNonNullCell(validMoves);
    }

    private List<Cell> findBestScoredCells(List<Cell> validMoves, Player aiPlayer) {
        List<Cell> bestCells = new ArrayList<>();
        int maxScore = Integer.MIN_VALUE;

        for (Cell cell : validMoves) {
            if (cell == null) {
                continue;
            }

            int score = calculateCellScore(cell, aiPlayer);

            if (score > maxScore) {
                maxScore = score;
                bestCells.clear();
                bestCells.add(cell);
            } else if (score == maxScore) {
                bestCells.add(cell);
            }
        }
        return bestCells;
    }

    private int calculateCellScore(Cell cell, Player aiPlayer) {
        int score = 0;
        int orbs = cell.getOrbs();
        int criticalMass = cell.getCriticalMass();

        if (cell.getOwner() != null && cell.getOwner().equals(aiPlayer)) {
            score += (orbs * 10);
            if (orbs == criticalMass - 1) {
                score += 50;
            }
        } else if (cell.getOwner() == null) {
            score += (4 - criticalMass) * 5;
        }

        return score;
    }

    private Cell selectRandomNonNullCell(List<Cell> validMoves) {
        List<Cell> nonNullMoves = new ArrayList<>();
        for (Cell c : validMoves) {
            if (c != null) {
                nonNullMoves.add(c);
            }
        }

        if (!nonNullMoves.isEmpty()) {
            return nonNullMoves.get(random.nextInt(nonNullMoves.size()));
        }
        return null;
    }
}