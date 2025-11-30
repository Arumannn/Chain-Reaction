package com.silent.treatment.chainreaction.strategy;

import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;

/**
 * Strategy untuk explosion dengan animasi.
 * Menggunakan ExplosionQueue untuk sequential processing dan
 * memicu animasi melalui observer pattern.
 */
public class AnimatedExplosionStrategy implements ExplosionStrategy {
    
    private ExplosionQueue explosionQueue;
    
    public AnimatedExplosionStrategy() {
        this.explosionQueue = ExplosionQueue.getInstance();
    }
    
    @Override
    public void explode(Cell cell, Board board, Player player) {
        // Queue explosion instead of immediate execution
        // Animasi akan dipicu oleh view layer, dan setelah selesai,
        // queue akan memproses explosion logic
        explosionQueue.enqueueExplosion(cell, board, player);
    }
}

