package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIMedium extends AIBase {

    private final MinimaxEngine minimaxEngine;
    private final Random random;

    public AIMedium() {
        this.minimaxEngine = new MinimaxEngine(2);
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

        List<Cell> bestCells = new ArrayList<>();
        int maxOrbs = -1;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            int orbs = cell.getOrbs();

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

        if (!bestCells.isEmpty()) {
            return bestCells.get(random.nextInt(bestCells.size()));
        }

        List<Cell> nonNullMoves = new ArrayList<>();
        for (Cell c : validMoves) {
            if (c != null)
                nonNullMoves.add(c);
        }

        if (!nonNullMoves.isEmpty()) {
            return nonNullMoves.get(random.nextInt(nonNullMoves.size()));
        }

        return null;
    }
}
