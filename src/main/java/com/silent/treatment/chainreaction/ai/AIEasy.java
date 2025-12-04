package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI Easy - Greedy strategy dengan multiplayer support (2-8 player).
 * Menggunakan MinimaxEngine depth 1 dan evaluasi terhadap SEMUA musuh.
 */
public class AIEasy extends AIBase {

    private final MinimaxEngine engine;
    private final Random random;

    public AIEasy() {
        this.engine = new MinimaxEngine(1); // Depth 1 for greedy approach
        this.random = new Random();
    }

    @Override
    public Cell chooseMove(GameManager gm) {
        Player aiPlayer = gm.getCurrentPlayer();

        // Kumpulkan semua valid moves
        List<Cell> validMoves = collectValidMoves(gm, aiPlayer);

        if (validMoves.isEmpty()) {
            return null;
        }

        // Gunakan MinimaxEngine yang sudah support multiplayer
        Cell bestMove = engine.findBestMove(gm, aiPlayer, validMoves);

        if (bestMove != null) {
            return bestMove;
        }

        // Fallback ke random move
        return findAnyValidCell(gm, aiPlayer);
    }

    private Cell findAnyValidCell(GameManager gm, Player aiPlayer) {
        List<Cell> emptyCells = new ArrayList<>();
        List<Cell> ownCells = new ArrayList<>();

        collectCells(gm, aiPlayer, emptyCells, ownCells);

        // Prioritas: empty cells dulu
        if (!emptyCells.isEmpty()) {
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }

        // Kalau tidak ada empty, pilih cell sendiri dengan orbs terbanyak
        return findBestOwnedCell(ownCells);
    }

    private void collectCells(GameManager gm, Player aiPlayer, List<Cell> emptyCells, List<Cell> ownCells) {
        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue;

                if (c.getOwner() == null) {
                    emptyCells.add(c);
                } else if (c.getOwner().equals(aiPlayer)) {
                    ownCells.add(c);
                }
            }
        }
    }

    private Cell findBestOwnedCell(List<Cell> ownCells) {
        if (ownCells.isEmpty()) {
            return null;
        }

        Cell bestOwn = ownCells.get(0);
        for (Cell c : ownCells) {
            if (c.getOrbs() > bestOwn.getOrbs()) {
                bestOwn = c;
            }
        }
        return bestOwn;
    }
}