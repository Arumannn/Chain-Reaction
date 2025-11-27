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
    }

    public void initializeGame(MapType mapType, int numPlayers) {
        List<Player> defaultPlayers = new ArrayList<>();
        Color[] colors = {Color.RED, Color.LIME, Color.CYAN, Color.YELLOW};
        for (int i = 0; i < numPlayers; i++) {
            defaultPlayers.add(new Player("Player " + (i+1), colors[i % 4]));
        }
        initializeGame(mapType, defaultPlayers);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
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
}