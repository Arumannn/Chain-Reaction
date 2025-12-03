package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class StandardEvaluator implements BoardEvaluator {

    @Override
    public double evaluate(Board board, Player aiPlayer, Player enemyPlayer) {
        double score = 0;

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);

                if (cell == null)
                    continue;

                if (cell.getOwner() == null)
                    continue;

                boolean isAI = cell.getOwner().equals(aiPlayer);
                double cellValue = 0;

                cellValue += cell.getOrbs() * 10.0;

                if (cell.getOrbs() == cell.getCriticalMass() - 1) {
                    cellValue += 25.0;
                }

                // Reduced corner bonus untuk variasi move (tidak selalu pojok)
                if (cell.getCriticalMass() == 2)
                    cellValue += 5.0; // Corner: bonus dikurangi dari 20 → 5
                else if (cell.getCriticalMass() == 3)
                    cellValue += 3.0; // Edge: bonus dikurangi dari 10 → 3

                // Tambah variasi based on position (spread move)
                cellValue += (i + j) * 0.5;

                for (Cell neighbor : cell.getNeighbors()) {
                    if (neighbor.getOwner() != null && neighbor.getOwner().equals(enemyPlayer)) {
                        if (neighbor.getOrbs() == neighbor.getCriticalMass() - 1) {
                            cellValue -= 50.0;
                        }
                    }
                }

                if (isAI)
                    score += cellValue;
                else
                    score -= cellValue;
            }
        }
        return score;
    }
}