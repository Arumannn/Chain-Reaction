package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

/**
 * Evaluator dasar yang menilai berdasarkan Orb Count dan posisi strategis.
 */
public class StandardEvaluator implements BoardEvaluator {

    @Override
    public double evaluate(Board board, Player aiPlayer, Player enemyPlayer) {
        double score = 0;

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);

                if (cell.getOwner() == null) continue;

                boolean isAI = cell.getOwner().equals(aiPlayer);
                double cellValue = 0;

                // 1. Nilai Orb Count (Setiap Orb bernilai 10)
                cellValue += cell.getOrbs() * 10.0;

                // 2. Proximity ke Critical Mass (Hampir meledak: bonus 25)
                if (cell.getOrbs() == cell.getCriticalMass() - 1) {
                    cellValue += 25.0;
                }

                // 3. Nilai Posisi (Sudut > Pinggir)
                if (cell.getCriticalMass() == 2) cellValue += 20.0; // Sudut
                else if (cell.getCriticalMass() == 3) cellValue += 10.0; // Pinggir

                // 4. Danger Check (Cek ancaman dari Musuh yang siap meledak)
                for (Cell neighbor : cell.getNeighbors()) {
                    if (neighbor.getOwner() != null && neighbor.getOwner().equals(enemyPlayer)) {
                        if (neighbor.getOrbs() == neighbor.getCriticalMass() - 1) {
                            cellValue -= 50.0; // SANGAT BAHAYA
                        }
                    }
                }

                // Akumulasi skor: Tambah jika milik AI, Kurangi jika milik Musuh
                if (isAI) score += cellValue;
                else score -= cellValue;
            }
        }
        return score;
    }
}