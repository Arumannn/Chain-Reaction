package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Player;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;

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

        // Setup Players Dummy (Bisa diganti input user)
        Color[] colors = {Color.RED, Color.LIMEGREEN, Color.BLUE, Color.YELLOW};
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player(i, "Player " + (i+1), colors[i % 4]));
        }
        currentPlayerIndex = 0;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        // Disini bisa ditambahkan logika cek pemain aktif/eliminasi
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Board getBoard() { return board; }
}