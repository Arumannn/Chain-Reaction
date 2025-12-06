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

                if (cell == null || cell.getOwner() == null) {
                    continue;
                }

                double cellValue = evaluateCell(cell, i, j, enemyPlayer);
                score += cell.getOwner().equals(aiPlayer) ? cellValue : -cellValue;
            }
        }
        return score;
    }

    private double evaluateCell(Cell cell, int x, int y, Player enemyPlayer) {
        double cellValue = calculateBaseCellValue(cell, x, y);
        cellValue -= calculateEnemyThreat(cell, enemyPlayer);
        return cellValue;
    }

    private double calculateBaseCellValue(Cell cell, int x, int y) {
        double value = cell.getOrbs() * 10.0;

        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            value += 25.0;
        }

        value += calculatePositionBonus(cell.getCriticalMass());
        value += (x + y) * 0.5; // Spread move bonus

        return value;
    }

    private double calculatePositionBonus(int criticalMass) {
        if (criticalMass == 2) {
            return 5.0; // Corner bonus
        } else if (criticalMass == 3) {
            return 3.0; // Edge bonus
        }
        return 0;
    }

    private double calculateEnemyThreat(Cell cell, Player enemyPlayer) {
        double threat = 0;
        for (Cell neighbor : cell.getNeighbors()) {
            if (isEnemyThreat(neighbor, enemyPlayer)) {
                threat += 50.0;
            }
        }
        return threat;
    }

    private boolean isEnemyThreat(Cell cell, Player enemyPlayer) {
        return cell.getOwner() != null
                && cell.getOwner().equals(enemyPlayer)
                && cell.getOrbs() == cell.getCriticalMass() - 1;
    }
}