package com.silent.treatment.chainreaction.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testMapWithHoles() {
        // MapType.DONUT ukuran 8x8, lubang di tengah (x:3-4, y:3-4)
        Board board = new Board(MapType.DONUT);
        
        // Cek sel normal (Pojok kiri atas)
        assertNotNull(board.getCell(0, 0), "Sel (0,0) harusnya valid (ada)");
        
        // Cek sel di tengah (Lubang donat)
        assertNull(board.getCell(3, 3), "Sel (3,3) harusnya NULL (lubang)");
        assertNull(board.getCell(4, 4), "Sel (4,4) harusnya NULL (lubang)");
        
        // Cek sel valid di seberang lubang
        assertNotNull(board.getCell(7, 7), "Sel (7,7) harusnya valid");
    }

    @Test
    void testCriticalMassAssignment() {
        Board board = new Board(MapType.SMALL); // 9x6
        
        // Sudut (Corner) - Harusnya 2
        assertEquals(2, board.getCell(0, 0).getCriticalMass());
        
        // Pinggir (Edge) - Harusnya 3
        assertEquals(3, board.getCell(1, 0).getCriticalMass());
        
        // Tengah (Center) - Harusnya 4
        assertEquals(4, board.getCell(1, 1).getCriticalMass());
    }
}