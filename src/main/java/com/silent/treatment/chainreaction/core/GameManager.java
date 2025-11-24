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

    private GameManager() {}

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // Overload method: Bisa menerima list pemain custom (dari Revan SetupView)
    public void initializeGame(int width, int height, List<Player> customPlayers) {
        board = new Board(width, height);
        this.players = customPlayers;
        currentPlayerIndex = 0;
    }

    // Method lama (fallback/default)
    public void initializeGame(int width, int height, int numPlayers) {
        List<Player> defaultPlayers = new ArrayList<>();
        Color[] colors = {Color.RED, Color.LIME, Color.CYAN, Color.YELLOW};
        for (int i = 0; i < numPlayers; i++) {
            defaultPlayers.add(new Player("Player " + (i+1), colors[i % 4]));
        }
        initializeGame(width, height, defaultPlayers);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // Fitur dari Faris: Hitung total orb
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

    public List<Player> getPlayers() { return players; }
    public Board getBoard() { return board; }
}