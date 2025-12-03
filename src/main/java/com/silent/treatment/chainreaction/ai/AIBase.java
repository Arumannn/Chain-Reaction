package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AIBase {
    protected abstract Cell chooseMove(GameManager gm);

    protected List<Cell> collectValidMoves(GameManager gm, Player player) {
        List<Cell> validMoves = new ArrayList<>();
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell cell = gm.getBoard().getCell(x, y);

                if (cell == null) {
                    continue;
                }

                if (cell.getOwner() == null || cell.getOwner().equals(player)) {
                    validMoves.add(cell);
                }
            }
        }

        return validMoves;
    }

    protected Cell findCriticalThreat(Player player, GameManager gm) {
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue;

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
        int maxX = gm.getBoard().getWidth() - 1;
        int maxY = gm.getBoard().getHeight() - 1;

        int[][] corners = {
                { 0, 0 },
                { maxX, 0 },
                { 0, maxY },
                { maxX, maxY }
        };

        for (int[] pos : corners) {
            int x = pos[0], y = pos[1];
            Cell c = gm.getBoard().getCell(x, y);
            if (c == null)
                continue;
            if (c.getOwner() == null)
                return c;
        }
        return null;
    }

    protected Cell findOwnCellToStack(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue;
                if (ai.equals(c.getOwner()))
                    return c;
            }
        }
        return null;
    }

    protected Cell findAnyEmptyCell(GameManager gm) {
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue;
                if (c.getOwner() == null)
                    return c;
            }
        }
        return null;
    }
}