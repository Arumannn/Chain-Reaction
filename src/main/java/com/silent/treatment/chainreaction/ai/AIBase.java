package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AIBase {
    protected abstract Cell chooseMove(GameManager gm);

    /**
     * Mengumpulkan semua valid moves untuk player.
     * Valid move = cell kosong atau cell yang dimiliki player.
     */
    protected List<Cell> collectValidMoves(GameManager gm, Player player) {
        List<Cell> validMoves = new ArrayList<>();

        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell cell = gm.getBoard().getCell(x, y);

                // Skip null cells (untuk custom maps)
                if (cell == null) {
                    continue;
                }

                // Valid jika kosong atau milik player
                if (cell.getOwner() == null || cell.getOwner().equals(player)) {
                    validMoves.add(cell);
                }
            }
        }

        return validMoves;
    }

    // Metode Helper yang Anda buat (dipindahkan ke sini)
    protected Cell findCriticalThreat(Player player, GameManager gm) {
        // ... (Logika sama persis) ...
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue; // Skip null cells di custom maps

                if (player.equals(c.getOwner()) &&
                        c.getOrbs() >= c.getCriticalMass() - 1) {
                    for (Cell n : c.getNeighbors()) {
                        if (n.getOwner() == null)
                            return n;
                    }
                }
            }
        }
        return null;
    }

    protected Cell findCornerMove(GameManager gm) {
        // Calculate corners based on actual board size (not hardcoded)
        int maxX = gm.getBoard().getWidth() - 1;
        int maxY = gm.getBoard().getHeight() - 1;

        int[][] corners = {
                { 0, 0 }, // Top-left
                { maxX, 0 }, // Top-right
                { 0, maxY }, // Bottom-left
                { maxX, maxY } // Bottom-right
        };

        for (int[] pos : corners) {
            int x = pos[0], y = pos[1];
            Cell c = gm.getBoard().getCell(x, y);
            if (c == null)
                continue; // Skip null cells (custom maps)
            if (c.getOwner() == null)
                return c;
        }
        return null;
    }

    // TAMBAHKAN HELPER BARU: Pilih Sel Sendiri (Untuk Stacking)
    protected Cell findOwnCellToStack(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue; // Skip null cells
                if (ai.equals(c.getOwner()))
                    return c;
            }
        }
        return null;
    }

    protected Cell findAnyEmptyCell(GameManager gm) {
        // ... (Logika sama persis) ...
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue; // Skip null cells di custom maps
                if (c.getOwner() == null)
                    return c;
            }
        }
        return null;
    }
}