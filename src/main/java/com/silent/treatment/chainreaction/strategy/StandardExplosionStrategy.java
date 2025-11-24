package com.silent.treatment.chainreaction.strategy;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class StandardExplosionStrategy implements ExplosionStrategy {

    @Override
    public void explode(Cell cell, Board board, Player player) {
        // 1. Kurangi orb di pusat
        // (Biasanya di chain reaction: orb dikurangi sebanyak critical mass)
        int remainingOrbs = cell.getOrbs() - cell.getCriticalMass();
        cell.setOrbs(remainingOrbs);

        // 2. Distribusi ke tetangga
        for (Cell neighbor : cell.getNeighbors()) {
            // Rekursi: addOrb akan memicu explode lagi di tetangga jika penuh
            neighbor.addOrb(player, board);
        }
    }
}