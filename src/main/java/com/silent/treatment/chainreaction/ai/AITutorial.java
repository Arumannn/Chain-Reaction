package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AITutorial extends AIBase {
    private final Random random = new Random();

    @Override
    public Cell chooseMove(GameManager gm) {
        Player ai = gm.getCurrentPlayer();

        List<AIMove> legalMoves = MoveGenerator.getLegalMoves(gm.getBoard(), ai);
        if (legalMoves.isEmpty())
            return null;

        // Tutorial AI: Pilih random move (sangat lemah)
        // Tapi hindari move yang terlalu bagus (explosion)
        List<AIMove> safeMovesWeakMoves = new ArrayList<>();
        List<AIMove> allMoves = new ArrayList<>();

        for (AIMove move : legalMoves) {
            Cell cell = gm.getBoard().getCell(move.x, move.y);
            if (cell == null)
                continue;

            allMoves.add(move);

            // Avoid explosion moves (too good for tutorial AI)
            if (cell.getOrbs() < cell.getCriticalMass() - 1) {
                safeMovesWeakMoves.add(move);
            }
        }

        // Prioritas: weak moves > all moves > random
        List<AIMove> chooseFrom = safeMovesWeakMoves.isEmpty() ? allMoves : safeMovesWeakMoves;

        if (chooseFrom.isEmpty()) {
            return null;
        }

        AIMove chosen = chooseFrom.get(random.nextInt(chooseFrom.size()));
        return gm.getBoard().getCell(chosen.x, chosen.y);
    }
}