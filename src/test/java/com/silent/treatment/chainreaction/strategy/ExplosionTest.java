package com.silent.treatment.chainreaction.strategy;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.MapType;
import com.silent.treatment.chainreaction.model.Player;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExplosionTest {

    @Test
    void testExplosionLogic() {
        // Setup manual tanpa animasi agar bisa ditest logic-nya
        Board board = new Board(MapType.SMALL);
        Player p1 = new Player("P1", Color.RED);
        
        // Kita pakai StandardStrategy untuk tes logika murni (bukan Animated)
        StandardExplosionStrategy strategy = new StandardExplosionStrategy();
        
        Cell center = board.getCell(1, 1); // Center cell, critical mass 4
        Cell neighbor = board.getCell(1, 2);
        
        // Isi cell sampai hampir meledak (3 orb)
        center.setOrbs(3);
        center.addOrb(p1, board); // Orb ke-4, harusnya trigger explode
        
        // Simulasi explode manual karena di unit test Strategy mungkin belum ter-inject otomatis
        if (center.getOrbs() >= center.getCriticalMass()) {
            strategy.explode(center, board, p1);
        }
        
        // Assertions:
        // 1. Cell pusat harusnya berkurang orbs-nya (4 - 4 = 0)
        // Tapi karena addOrb nambah dulu, logic explode biasanya: current - critical
        assertEquals(0, center.getOrbs(), "Cell pusat harusnya kosong setelah meledak");
        
        // 2. Tetangga harusnya dapat 1 orb
        assertEquals(1, neighbor.getOrbs(), "Tetangga harusnya menerima 1 orb");
        
        // 3. Tetangga harusnya jadi milik P1
        assertEquals(p1, neighbor.getOwner(), "Tetangga harusnya jadi milik P1");
    }
}