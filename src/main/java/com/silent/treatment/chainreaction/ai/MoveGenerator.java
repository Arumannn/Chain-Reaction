package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    /**
     * Menghasilkan semua langkah legal yang mungkin dilakukan oleh pemain.
     * Langkah legal adalah sel yang kosong ATAU sudah dimiliki oleh pemain.
     */
    public static List<AIMove> getLegalMoves(Board board, Player player) {
        List<AIMove> moves = new ArrayList<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Cell cell = board.getCell(i, j);

                // Syarat: Cell kosong ATAU milik sendiri
                if (cell.getOwner() == null || cell.getOwner().equals(player)) {
                    // Skor awal diatur 0, nanti akan diisi oleh MinimaxEngine
                    moves.add(new AIMove(i, j, 0));
                }
            }
        }
        return moves;
    }
}