package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private int turnCounter; // Tambahan: Menghitung giliran

    private GameManager() {
        // Private Constructor
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeGame(int width, int height, int numPlayers) {
        board = new Board(width, height);
        players = new ArrayList<>();
        turnCounter = 1; // Mulai dari giliran 1

        // Setup Players (Warna disesuaikan agar kontras dengan mode gelap)
        Color[] colors = {Color.RED, Color.LIME, Color.CYAN, Color.YELLOW};
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player(i, "Player " + (i+1), colors[i % 4]));
        }
        currentPlayerIndex = 0;
    }

    public void nextTurn() {
        // Logika rotasi pemain sederhana
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        // Tambahkan counter giliran
        turnCounter++;
        
        // Note: Di sini nanti bisa ditambahkan logika skip pemain yang sudah kalah (isActive == false)
    }

    // Fitur Baru: Menghitung total orb milik pemain tertentu di papan
    public int getPlayerOrbCount(Player player) {
        int count = 0;
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);
                if (player.equals(cell.getOwner())) {
                    count += cell.getOrbs();
                }
            }
        }
        return count;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() { // Getter untuk list pemain
        return players;
    }

    public int getTurnNumber() { // Getter Turn Number
        return turnCounter;
    }

    public Board getBoard() { return board; }
}