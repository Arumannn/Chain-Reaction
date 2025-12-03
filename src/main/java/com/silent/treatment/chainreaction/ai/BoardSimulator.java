package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class BoardSimulator {

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

        CellState cell = newState.getCell(move.x, move.y);
        if (cell != null) {
            cell.owner = player;
            cell.orbs++;

            if (cell.orbs >= cell.criticalMass) {

                cell.orbs -= cell.criticalMass;

                int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
                for (int[] dir : directions) {
                    int nx = move.x + dir[0];
                    int ny = move.y + dir[1];

                    if (nx >= 0 && nx < newState.width && ny >= 0 && ny < newState.height) {
                        CellState neighbor = newState.getCell(nx, ny);
                        if (neighbor != null) {
                            neighbor.owner = player;
                            neighbor.orbs++;
                        }
                    }
                }
            }
        }

        return newState;
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

        public double evaluate(Player aiPlayer, Player enemyPlayer) {
            double score = 0;
            int aiOrbs = 0;
            int enemyOrbs = 0;
            int aiCells = 0;
            int enemyCells = 0;

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

                    } else if (cell.owner.equals(enemyPlayer)) {
                        enemyOrbs += cell.orbs;
                        enemyCells++;

                        if (cell.orbs == cell.criticalMass - 1) {
                            score -= 15.0;
                        }
                    }
                }
            }

            score += (aiOrbs - enemyOrbs) * 10.0;
            score += (aiCells - enemyCells) * 5.0;

            if (enemyOrbs == 0 && enemyCells == 0) {
                score += 10000.0;
            } else if (aiOrbs == 0 && aiCells == 0) {
                score -= 10000.0;
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
