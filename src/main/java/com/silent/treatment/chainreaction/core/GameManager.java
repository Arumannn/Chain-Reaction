package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Player;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private int totalTurns = 0;
    private boolean isGameOver = false;

    // Singleton Boilerplate
    public static GameManager getInstance() { 
        if (instance == null) instance = new GameManager();
        return instance; 
    }

    public void initializeGame(int width, int height, int numPlayers) {
        List<Player> generatedPlayers = new ArrayList<>();
        
        // Palet warna default untuk pemain (FR-1.2)
        Color[] defaultColors = {
            Color.RED, Color.LIME, Color.BLUE, 
            Color.YELLOW, Color.MAGENTA, Color.CYAN, 
            Color.ORANGE, Color.WHITE
        };

        // Validasi jumlah pemain (min 2, max 8 sesuai palet)
        if (numPlayers < 2) numPlayers = 2;
        if (numPlayers > defaultColors.length) numPlayers = defaultColors.length;

        // Generate Pemain Otomatis
        for (int i = 0; i < numPlayers; i++) {
            String name = "Player " + (i + 1);
            generatedPlayers.add(new Player(name, defaultColors[i]));
        }

        // Panggil method inisialisasi utama
        initializeGame(width, height, generatedPlayers);
    }

    // Method Utama (yang sudah ada sebelumnya)
    public void initializeGame(int width, int height, List<Player> newPlayers) {
        this.board = new Board(width, height); // Membuat board sesuai ukuran input
        this.players = newPlayers;
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.totalTurns = 0;
        
        // Pastikan semua pemain hidup di awal
        for(Player p : players) {
            p.setAlive(true);
        }
        
        System.out.println("Game Initialized: " + width + "x" + height + " with " + players.size() + " players.");
    }

    // Method untuk validasi Win/Loss (FR-4.1 & FR-4.2)
    public void checkGameStatus() {
        if (isGameOver) return;

        // FR-3.3: Proteksi Putaran Pertama 
        // (Asumsi minimal setiap pemain sudah jalan 1x sebelum eliminasi dimulai)
        if (totalTurns < players.size()) return; 

        int activePlayersCount = 0;
        Player potentialWinner = null;

        for (Player p : players) {
            if (p.isAlive()) {
                int orbsOnBoard = board.getPlayerOrbCount(p);
                
                // FR-4.1: Eliminasi Pemain
                if (orbsOnBoard == 0) {
                    p.setAlive(false);
                    System.out.println("Pemain TERELIMINASI: " + p.getName());
                } else {
                    activePlayersCount++;
                    potentialWinner = p;
                }
            }
        }

        // FR-4.2: Kondisi Menang
        if (activePlayersCount == 1 && potentialWinner != null) {
            handleGameOver(potentialWinner);
        }
    }

    private void handleGameOver(Player winner) {
        isGameOver = true;
        System.out.println("GAME OVER! Pemenang: " + winner.getName());
        // TODO: Tampilkan Popup/Scene Game Over via JavaFX
    }
    

    public boolean isGameOver() {
        return isGameOver;
    }
    public void nextTurn() {
        if (isGameOver) return;

        totalTurns++;
        
        // Cari pemain berikutnya yang masih HIDUP (skip yang sudah mati)
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isAlive());
    }

    public Board getBoard() { return board; }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
}