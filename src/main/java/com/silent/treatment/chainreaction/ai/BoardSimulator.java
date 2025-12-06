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
        BoardState newState = new BoardState(state); // Use copy constructor
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

        // Copy constructor
        public BoardState(BoardState other) {
            this.width = other.width;
            this.height = other.height;
            this.cells = new CellState[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (other.cells[x][y] != null) {
                        this.cells[x][y] = new CellState(other.cells[x][y]);
                    }
                }
            }
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

        /**
         * Evaluate board state untuk MULTIPLAYER (2-8 player).
         * Aggregates ALL non-AI players as enemies.
         */
        public double evaluate(Player aiPlayer) {
            int aiOrbs = 0;
            int totalEnemyOrbs = 0;
            int aiCells = 0;
            int totalEnemyCells = 0;
            double score = 0;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    CellState cell = cells[x][y];
                    if (cell == null || cell.owner == null) {
                        continue;
                    }

                    if (cell.owner.equals(aiPlayer)) {
                        score += evaluateAICell(cell);
                        aiOrbs += cell.orbs;
                        aiCells++;
                    } else {
                        score += evaluateEnemyCell(cell);
                        totalEnemyOrbs += cell.orbs;
                        totalEnemyCells++;
                    }
                }
            }

            score += calculateDominanceScore(aiOrbs, totalEnemyOrbs, aiCells, totalEnemyCells);
            score += calculateWinLoseBonus(aiOrbs, aiCells, totalEnemyOrbs, totalEnemyCells);

            return score;
        }

        private double evaluateAICell(CellState cell) {
            double cellScore = 0;

            if (cell.orbs == cell.criticalMass - 1) {
                cellScore += 15.0;
            }

            if (cell.criticalMass == 2) {
                cellScore += 5.0;
            } else if (cell.criticalMass == 3) {
                cellScore += 3.0;
            }

            return cellScore;
        }

        private double evaluateEnemyCell(CellState cell) {
            if (cell.orbs == cell.criticalMass - 1) {
                return -15.0; // Threat from any enemy
            }
            return 0;
        }

        private double calculateDominanceScore(int aiOrbs, int enemyOrbs, int aiCells, int enemyCells) {
            return (aiOrbs - enemyOrbs) * 10.0 + (aiCells - enemyCells) * 5.0;
        }

        private double calculateWinLoseBonus(int aiOrbs, int aiCells, int enemyOrbs, int enemyCells) {
            if (enemyOrbs == 0 && enemyCells == 0) {
                return 10000.0; // AI won (all enemies eliminated)
            } else if (aiOrbs == 0 && aiCells == 0) {
                return -10000.0; // AI lost
            }
            return 0;
        }
    }

    public static class CellState {
        int orbs;
        Player owner;
        int criticalMass;

        public CellState(int orbs, Player owner, int criticalMass) {
            this.orbs = orbs;
            this.owner = owner;
            this.criticalMass = criticalMass;
        }

        // Copy constructor
        public CellState(CellState other) {
            this.orbs = other.orbs;
            this.owner = other.owner;
            this.criticalMass = other.criticalMass;
        }

        public int getOrbs() {
            return orbs;
        }

        public Player getOwner() {
            return owner;
        }

        public int getCriticalMass() {
            return criticalMass;
        }

    }
}
