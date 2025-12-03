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

    // Tracking state game
    private int totalTurns;
    private boolean isGameOver;
    private Player winner;
    private boolean humansDefeatedFlag;

    // Set to false for production/normal gameplay
    private static final boolean DEBUG_MODE = false;

    private GameManager() {
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeGame(MapType mapType, List<Player> customPlayers) {
        board = new Board(mapType);
        this.players = customPlayers;
        currentPlayerIndex = 0;

        // Reset state saat game baru
        this.totalTurns = 0;
        this.isGameOver = false;
        this.winner = null;
        this.humansDefeatedFlag = false;
        for (Player p : players) {
            p.setAlive(true);
            p.setHasPlayed(false); // Reset status sudah main
        }
        log("[INIT] Game initialized with " + players.size() + " players.");
    }

    public void initializeGame(MapType mapType, int numPlayers) {
        List<Player> defaultPlayers = new ArrayList<>();
        Color[] colors = { Color.RED, Color.LIME, Color.CYAN, Color.YELLOW };
        for (int i = 0; i < numPlayers; i++) {
            defaultPlayers.add(new Player("Player " + (i + 1), colors[i % 4]));
        }
        initializeGame(mapType, defaultPlayers);
    }

    public void nextTurn() {
        if (isGameOver) return;

        totalTurns++;
        log("\n[TURN] Ending turn for " + getCurrentPlayer().getName());

        int attempts = 0;
        
        // Cari pemain berikutnya yang masih Alive
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
        } while (!players.get(currentPlayerIndex).isAlive() && attempts < players.size());

        // Safety check
        if (attempts >= players.size()) {
            isGameOver = true;
            log("[GAME OVER] No active players found during turn switch!");
        } else {
            log("[TURN] New Turn: " + getCurrentPlayer().getName() + " (Turn #" + totalTurns + ")");
        }
    }

    public void checkEliminations() {
        if (isGameOver) return;

        log("[CHECK] Checking eliminations...");

        for (Player p : players) {
            // Skip jika sudah mati
            if (!p.isAlive()) {
                continue;
            }

            // Eliminasi terjadi JIKA:
            // 1. Pemain sudah pernah melakukan giliran (hasPlayed = true)
            // 2. Jumlah Orb pemain tersebut 0
            if (p.hasPlayed()) {
                int orbCount = getPlayerOrbCount(p);
                
                if (orbCount == 0) {
                    p.setAlive(false);
                    // Kita tetap print info penting ini meskipun DEBUG_MODE false
                    System.out.println("!!! ELIMINATION !!! " + p.getName() + " has been ELIMINATED!");
                }
            }
        }
    }

    public Player checkWinner() {
        if (isGameOver) return winner;

        int activePlayers = 0;
        Player potentialWinner = null;

        boolean anyHumanAlive = false;
        boolean anyBotAlive = false;
        Player firstAliveBot = null;

        for (Player p : players) {
            if (p.isAlive()) {
                activePlayers++;
                potentialWinner = p;

                boolean isBot = isBotPlayer(p);
                if (isBot) {
                    anyBotAlive = true;
                    if (firstAliveBot == null) {
                        firstAliveBot = p;
                    }
                } else {
                    anyHumanAlive = true;
                }
            }
        }

        // Kondisi LOSE: semua pemain human mati tetapi masih ada bot hidup
        if (!anyHumanAlive && anyBotAlive) {
            humansDefeatedFlag = true; // Game terus berjalan, tapi tandai defeat
        }

        // Kondisi menang normal: hanya 1 pemain (human atau bot) yang tersisa
        if (activePlayers == 1) {
            this.isGameOver = true;
            this.winner = potentialWinner;
            System.out.println("GAME OVER! Winner: " + winner.getName());
            return winner;
        }

        return null;
    }

    /**
     * Sementara: identifikasi bot berdasarkan pola nama.
     * Semua pemain dengan nama yang mengandung "AI Bot" dianggap bot.
     */
    private boolean isBotPlayer(Player p) {
        if (p == null || p.getName() == null) return false;
        return p.getName().toLowerCase().contains("ai bot");
    }

    /**
     * Mengembalikan true sekali saat semua human telah dieliminasi.
     * Flag akan otomatis di-reset setelah dibaca.
     */
    public boolean consumeHumansDefeatedFlag() {
        if (humansDefeatedFlag) {
            humansDefeatedFlag = false;
            return true;
        }
        return false;
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

    private void log(String msg) {
        if (DEBUG_MODE) {
            System.out.println(msg);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Player getWinner() {
        return winner;
    }
}