package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import com.silent.treatment.chainreaction.model.MapType;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private int turnCounter;
    
    // [BARU] Variabel untuk tracking state game
    private int totalTurns; 
    private boolean isGameOver;
    private Player winner;

    private GameManager() {}

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeGame(MapType mapType, List<Player> customPlayers) {
        board = new Board(mapType);
        this.players = customPlayers;
        this.turnCounter = 1;
        currentPlayerIndex = 0;

        // [BARU] Reset state saat game baru
        this.totalTurns = 0;
        this.isGameOver = false;
        this.winner = null;
        for (Player p : players) {
            p.setAlive(true);
        }
    }

    public void initializeGame(MapType mapType, int numPlayers) {
        List<Player> defaultPlayers = new ArrayList<>();
        Color[] colors = {Color.RED, Color.LIME, Color.CYAN, Color.YELLOW};
        for (int i = 0; i < numPlayers; i++) {
            defaultPlayers.add(new Player("Player " + (i+1), colors[i % 4]));
        }
        initializeGame(mapType, defaultPlayers);
    }

    // [MODIFIKASI] Pindah giliran hanya ke pemain yang masih hidup (Alive)
    public void nextTurn() {
        if (isGameOver) return;

        totalTurns++; // Increment total turn
        
        int attempts = 0;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
        } while (!players.get(currentPlayerIndex).isAlive() && attempts < players.size());
        
        // Safety check: jika semua mati (tidak mungkin terjadi jika logic benar), game over
        if (attempts >= players.size()) {
            isGameOver = true;
        }
    }

    // [BARU] Cek apakah ada pemain yang tereliminasi (FR-4.1)
    public void checkEliminations() {
        // FR-3.3: First Move Protection (Jangan eliminasi di ronde 1)
        // Kita anggap ronde 1 selesai jika semua pemain sudah jalan sekali
        if (totalTurns < players.size()) {
            return;
        }

        for (Player p : players) {
            if (p.isAlive()) {
                int orbCount = getPlayerOrbCount(p);
                if (orbCount == 0) {
                    p.setAlive(false);
                    System.out.println(p.getName() + " has been ELIMINATED!");
                }
            }
        }
    }

    // [BARU] Cek kondisi menang (FR-4.2)
    public Player checkWinner() {
        if (isGameOver) return winner;

        int activePlayers = 0;
        Player potentialWinner = null;

        for (Player p : players) {
            if (p.isAlive()) {
                activePlayers++;
                potentialWinner = p;
            }
        }

        // Jika hanya 1 pemain tersisa, dia menang
        if (activePlayers == 1 && totalTurns >= players.size()) {
            this.isGameOver = true;
            this.winner = potentialWinner;
            System.out.println("GAME OVER! Winner: " + winner.getName());
            return winner;
        }
        
        return null;
    }

    public int getPlayerOrbCount(Player player) {
        int count = 0;
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);
                if (cell != null && player.equals(cell.getOwner())) {
                    count += cell.getOrbs();
                }
            }
        }
        return count;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() { return board; }
    public boolean isGameOver() { return isGameOver; }
    public Player getWinner() { return winner; }
}