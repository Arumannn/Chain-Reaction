package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class BoardSimulator {

    private BoardSimulator() {
        // Private constructor to prevent instantiation
    }

    public static BoardState cloneBoard(Board board) {
        int width = board.getWidth();
        int height = board.getHeight();

        CellState[][] cellStates = new CellState[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Cell cell = board.getCell(x, y);
                if (cell != null) {
                    cellStates[x][y] = new CellState(
                            cell.getOrbs(),
                            cell.getOwner(),
                            cell.getCriticalMass());
                } else {
                    cellStates[x][y] = null;
                }
            }
        }

        return new BoardState(cellStates, width, height);
    }

    public static BoardState simulateMove(BoardState state, AIMove move, Player player) {
        BoardState newState = state.clone();
        CellState cell = newState.getCell(move.getX(), move.getY());

        if (cell != null) {
            cell.owner = player;
            cell.orbs++;

            if (cell.orbs >= cell.criticalMass) {
                handleExplosion(newState, cell, move.getX(), move.getY(), player);
            }
        }

        return newState;
    }

    private static void handleExplosion(BoardState state, CellState cell, int x, int y, Player player) {
        cell.orbs -= cell.criticalMass;

        int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] dir : directions) {
            spreadToNeighbor(state, x + dir[0], y + dir[1], player);
        }
    }

    private static void spreadToNeighbor(BoardState state, int x, int y, Player player) {
        if (x >= 0 && x < state.width && y >= 0 && y < state.height) {
            CellState neighbor = state.getCell(x, y);
            if (neighbor != null) {
                neighbor.owner = player;
                neighbor.orbs++;
            }
        }
    }

    public static class BoardState {
        private CellState[][] cells;
        private int width;
        private int height;

        public BoardState(CellState[][] cells, int width, int height) {
            this.cells = cells;
            this.width = width;
            this.height = height;
        }

        public CellState getCell(int x, int y) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return cells[x][y];
            }
            return null;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public BoardState clone() {
            CellState[][] newCells = new CellState[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (cells[x][y] != null) {
                        newCells[x][y] = cells[x][y].clone();
                    }
                }
            }
            return new BoardState(newCells, width, height);
        }

        /**
         * Evaluate board state untuk MULTIPLAYER (2-8 player).
         * Parameter enemyPlayer diabaikan - aggregate SEMUA non-AI players.
         */
        public double evaluate(Player aiPlayer, Player enemyPlayer) {
            double score = 0;
            int aiOrbs = 0;
            int totalEnemyOrbs = 0; // Aggregate ALL enemies (not just one)
            int aiCells = 0;
            int totalEnemyCells = 0; // Aggregate ALL enemies (not just one)

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    CellState cell = cells[x][y];
                    if (cell == null || cell.owner == null)
                        continue;

                    if (cell.owner.equals(aiPlayer)) {
                        aiOrbs += cell.orbs;
                        aiCells++;

                        if (cell.orbs == cell.criticalMass - 1) {
                            score += 15.0;
                        }

                        if (cell.criticalMass == 2)
                            score += 5.0;
                        else if (cell.criticalMass == 3)
                            score += 3.0;

                    } else {
                        // ✅ ANY non-AI player = enemy (support 2-8 players)
                        totalEnemyOrbs += cell.orbs;
                        totalEnemyCells++;

                        if (cell.orbs == cell.criticalMass - 1) {
                            score -= 15.0; // Threat dari musuh MANAPUN
                        }
                    }
                }
            }

            // Score = AI dominance vs COLLECTIVE enemy strength
            score += (aiOrbs - totalEnemyOrbs) * 10.0;
            score += (aiCells - totalEnemyCells) * 5.0;

            // Win/lose conditions
            if (totalEnemyOrbs == 0 && totalEnemyCells == 0) {
                score += 10000.0; // AI won (all enemies eliminated)
            } else if (aiOrbs == 0 && aiCells == 0) {
                score -= 10000.0; // AI lost
            }

            return score;
        }
    }

    public static class CellState {
        public int orbs;
        public Player owner;
        public int criticalMass;

        public CellState(int orbs, Player owner, int criticalMass) {
            this.orbs = orbs;
            this.owner = owner;
            this.criticalMass = criticalMass;
        }

        public CellState clone() {
            return new CellState(orbs, owner, criticalMass);
        }
    }
}
