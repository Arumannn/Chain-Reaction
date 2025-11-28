package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Player;

public interface BoardEvaluator {
    /**
     * Menilai posisi papan. Positif = bagus untuk AI, Negatif = bagus untuk Musuh.
     * @param board Papan permainan saat ini.
     * @param aiPlayer Pemain yang sedang dievaluasi (MAX).
     * @param enemyPlayer Pemain lawan (MIN).
     * @return Skor evaluasi posisi papan.
     */
    double evaluate(Board board, Player aiPlayer, Player enemyPlayer);
}