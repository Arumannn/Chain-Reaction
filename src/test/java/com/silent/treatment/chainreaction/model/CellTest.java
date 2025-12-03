package com.silent.treatment.chainreaction.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    private Cell cell;
    private Player p1;
    private Board mockBoard; // Board bisa di-mock atau null jika method addOrb butuh

    @BeforeEach
    void setUp() {
        // Inisialisasi sebelum setiap test
        cell = new Cell(0, 0);
        p1 = new Player("P1", Color.RED);
        
        // Kita butuh neighbors untuk menentukan critical mass
        List<Cell> neighbors = new ArrayList<>();
        neighbors.add(new Cell(0, 1));
        neighbors.add(new Cell(1, 0));
        cell.setNeighbors(neighbors); // Critical mass = 2
    }

    @Test
    void testAddOrbUpdatesOwner() {
        // Pastikan awal kosong
        assertNull(cell.getOwner());
        assertEquals(0, cell.getOrbs());

        // Action: Tambah 1 orb (belum meledak karena critical mass 2)
        // Kita bisa pass null ke board jika logic explode belum dipanggil
        cell.addOrb(p1, null); 

        // Verifikasi
        assertEquals(p1, cell.getOwner(), "Owner harusnya P1");
        assertEquals(1, cell.getOrbs(), "Jumlah orb harusnya 1");
    }
}