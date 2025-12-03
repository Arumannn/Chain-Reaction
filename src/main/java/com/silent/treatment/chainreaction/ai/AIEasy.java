package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIEasy {

    private final MinimaxEngine engine;
    private final Random random;

    public AIEasy() {        
        this.engine = new MinimaxEngine(new StandardEvaluator(), 1);
        this.random = new Random();
    }

    public Cell chooseMove(GameManager gm) {
        Player ai = gm.getCurrentPlayer();
        
        Player enemy = null;
        for (Player p : gm.getPlayers()) {
            if (!p.equals(ai) && p.isAlive()) {
                enemy = p;
                break;
            }
        }
       
        if (enemy == null) {
            if (gm.getPlayers().size() > 1) {
                enemy = gm.getPlayers().get(0).equals(ai) ? gm.getPlayers().get(1) : gm.getPlayers().get(0);
            } else {               
                return findAnyValidCell(gm, ai);
            }
        }

        AIMove bestMove = null;
        try {
            bestMove = engine.findBestMove(gm.getBoard(), ai, enemy);
        } catch (Exception e) {
            System.err.println("AI Error: " + e.getMessage());
        }

        if (bestMove == null) {            
            return findAnyValidCell(gm, ai);
        }

        return gm.getBoard().getCell(bestMove.x, bestMove.y);}
        private Cell findAnyValidCell(GameManager gm, Player ai) {
        List<Cell> emptyCells = new ArrayList<>();
        List<Cell> ownCells = new ArrayList<>();

        for (int x = 0; x < gm.getBoard().getWidth(); x++) {
            for (int y = 0; y < gm.getBoard().getHeight(); y++) {
                Cell c = gm.getBoard().getCell(x, y);
                if (c == null)
                    continue; 

                if (c.getOwner() == null) {
                    emptyCells.add(c);
                } else if (c.getOwner().equals(ai)) {
                    ownCells.add(c);
                }
            }
        }

        
        if (!emptyCells.isEmpty()) {
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }

        
        if (!ownCells.isEmpty()) {
            Cell bestOwn = ownCells.get(0);
            for (Cell c : ownCells) {
                if (c.getOrbs() > bestOwn.getOrbs()) {
                    bestOwn = c;
                }
            }
            return bestOwn;
        }

        
        return null;
    }
}