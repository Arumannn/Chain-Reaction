package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class GameController {

    private Runnable onTurnChanged; // Callback untuk update UI luar

    public void setOnTurnChanged(Runnable onTurnChanged) {
        this.onTurnChanged = onTurnChanged;
    }

    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();
        Player currentPlayer = gm.getCurrentPlayer();

        // Validasi Move
        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            // Eksekusi Logika
            cell.addOrb(currentPlayer, gm.getBoard());

            // Ganti Giliran
            gm.nextTurn();

            // Beritahu UI kalau giliran berubah
            if (onTurnChanged != null) {
                onTurnChanged.run();
            }
            
        } else {
            System.out.println("Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }
}