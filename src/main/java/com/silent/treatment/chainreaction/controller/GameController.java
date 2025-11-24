package com.silent.treatment.chainreaction.controller;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

public class GameController {

    public void handleCellClick(Cell cell) {
        GameManager gm = GameManager.getInstance();
        Player currentPlayer = gm.getCurrentPlayer();

        // Validasi Move (FR-2.1)
        // Boleh isi jika kosong ATAU milik sendiri
        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            System.out.println(currentPlayer.getName() + " clicked " + cell.getX() + "," + cell.getY());

            // Eksekusi Logika
            cell.addOrb(currentPlayer, gm.getBoard());

            // Ganti Giliran
            gm.nextTurn();

            // Debug Log
            System.out.println("Next Turn: " + gm.getCurrentPlayer().getName());
        } else {
            System.out.println("Invalid Move! Cell owned by " + cell.getOwner().getName());
        }
    }
}