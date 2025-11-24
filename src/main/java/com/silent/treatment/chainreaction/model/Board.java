package com.silent.treatment.chainreaction.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private int width;
    private int height;
    private Cell[][] grid;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        initializeGrid();
    }

    private void initializeGrid() {
        // 1. Buat Objek Cell
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }

        // 2. Hubungkan Tetangga (Penting untuk Critical Mass)
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j].setNeighbors(findNeighbors(i, j));
            }
        }
    }

    private List<Cell> findNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<>();
        // Atas, Bawah, Kiri, Kanan
        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                neighbors.add(grid[nx][ny]);
            }
        }
        return neighbors;
    }

    public int getPlayerOrbCount(Player player) {
    int count = 0;
    for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
            if (grid[i][j].getOwner() != null && grid[i][j].getOwner().equals(player)) {
                count += grid[i][j].getOrbs(); // Atau count++ jika yang dihitung jumlah sel, bukan total orb
            }
        }
    }
    return count;
}

    public Cell getCell(int x, int y) { return grid[x][y]; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
}