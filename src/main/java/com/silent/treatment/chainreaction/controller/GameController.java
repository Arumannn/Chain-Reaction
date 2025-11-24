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
        
        // Cegah input jika game sudah selesai
        if (gm.isGameOver()) {
            System.out.println("Game sudah berakhir.");
            return;
        }

        Player currentPlayer = gm.getCurrentPlayer();

        // Validasi Move
        if (cell.getOwner() == null || cell.getOwner().equals(currentPlayer)) {
            
            // 1. Eksekusi Logika (akan memicu rekursi Strategy Pattern)
            cell.addOrb(currentPlayer, gm.getBoard());

            // 2. Cek Status Game (Eliminasi & Win Condition) - FR-4.1 & FR-4.2
            gm.checkGameStatus();

            // 3. Ganti Giliran (jika game belum berakhir)
            if (!gm.isGameOver()) {
                gm.nextTurn();
                // Notifikasi ke UI bahwa giliran berubah (Observer Pattern di GameManager bisa digunakan di sini)
            }

            // Beritahu UI kalau giliran berubah
            if (onTurnChanged != null) {
                onTurnChanged.run();
            }
            
        } else {
            System.out.println("Invalid Move!");
        }
    }
}