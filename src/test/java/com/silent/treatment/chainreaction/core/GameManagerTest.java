package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.MapType;
import com.silent.treatment.chainreaction.model.Player;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test untuk memverifikasi logika Game Over dan Eliminasi di GameManager.
 */
class GameManagerTest {

    private GameManager gm;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        gm = GameManager.getInstance();
        
        // Setup 2 Pemain: P1 (Red) dan P2 (Blue)
        List<Player> players = new ArrayList<>();
        p1 = new Player("P1", Color.RED);
        p2 = new Player("P2", Color.BLUE);
        players.add(p1);
        players.add(p2);
        
        // Inisialisasi game dengan map kecil (9x6)
        gm.initializeGame(MapType.SMALL, players);
    }

    @Test
    void testEliminationAndWinner() {
        // 1. Kondisi Awal: Game baru mulai, belum ada pemenang
        assertFalse(gm.isGameOver(), "Game seharusnya belum berakhir di awal");
        assertNull(gm.getWinner(), "Belum ada pemenang di awal");
        assertTrue(p1.isAlive(), "P1 harusnya hidup");
        assertTrue(p2.isAlive(), "P2 harusnya hidup");

        // 2. Simulasi Gameplay:
        // Beri P1 satu orb di papan
        gm.getBoard().getCell(0, 0).addOrb(p1, gm.getBoard());
        
        // Biarkan P2 tidak punya orb sama sekali (0 orb)

        // 3. Simulasi Putaran (Turn):
        // GameManager butuh beberapa putaran (minimal > jumlah pemain) 
        // sebelum fitur eliminasi aktif (First Move Protection).
        // Kita panggil nextTurn() 3 kali (lebih dari 2 pemain)
        gm.nextTurn(); // Turn 1
        gm.nextTurn(); // Turn 2
        gm.nextTurn(); // Turn 3 (Sekarang totalTurns > players.size())

        // 4. Cek Eliminasi
        // P2 punya 0 orb, harusnya tereliminasi sekarang
        gm.checkEliminations();
        
        assertFalse(p2.isAlive(), "P2 harusnya tereliminasi (mati) karena tidak punya orb");
        assertTrue(p1.isAlive(), "P1 harusnya masih hidup karena punya 1 orb");

        // 5. Cek Pemenang
        // Karena P2 mati dan cuma sisa P1, P1 harus menang
        Player winner = gm.checkWinner();
        
        assertNotNull(winner, "Harusnya ada pemenang");
        assertEquals(p1, winner, "Pemenang harusnya P1");
        assertTrue(gm.isGameOver(), "Status game harusnya GameOver");
    }

    @Test
    void testNoWinnerIfMultiplePlayersAlive() {
        // Beri kedua pemain orb
        gm.getBoard().getCell(0, 0).addOrb(p1, gm.getBoard());
        gm.getBoard().getCell(1, 1).addOrb(p2, gm.getBoard());

        // Majukan turn
        gm.nextTurn();
        gm.nextTurn();
        gm.nextTurn();

        // Cek kondisi
        gm.checkEliminations();
        Player winner = gm.checkWinner();

        // Keduanya masih punya orb, jadi belum ada pemenang
        assertTrue(p1.isAlive());
        assertTrue(p2.isAlive());
        assertNull(winner, "Tidak boleh ada pemenang jika >1 pemain masih hidup");
        assertFalse(gm.isGameOver());
    }
}