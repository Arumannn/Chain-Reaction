package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public abstract class AIBase {
    protected abstract Cell chooseMove(GameManager gm);

    // Metode Helper yang Anda buat (dipindahkan ke sini)
    protected Cell findCriticalThreat(Player player, GameManager gm) {
        // ... (Logika sama persis) ...
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (player.equals(c.getOwner()) &&
                        c.getOrbs() >= c.getCriticalMass() - 1) {
                    for (Cell n : c.getNeighbors()) {
                        if (n.getOwner() == null) return n;
                    }
                }
            }
        }
        return null;
    }

    protected Cell findCornerMove(GameManager gm) {
        // ... (Logika sama persis) ...
        int[][] corners = {{0,0}, {0,3}, {3,0}, {3,3}};
        // ... dst
        for (int[] pos : corners) {
            int x = pos[0], y = pos[1];
            if (x < gm.getBoard().getWidth() && y < gm.getBoard().getHeight()) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c.getOwner() == null) return c;
            }
        }
        return null;
    }

    // TAMBAHKAN HELPER BARU: Pilih Sel Sendiri (Untuk Stacking)
    protected Cell findOwnCellToStack(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (ai.equals(c.getOwner())) return c;
            }
        }
        return null;
    }

    protected Cell findAnyEmptyCell(GameManager gm) {
        // ... (Logika sama persis) ...
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c.getOwner() == null) return c;
            }
        }
        return null;
    }
}