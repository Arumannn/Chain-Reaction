package com.silent.treatment.chainreaction.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test sederhana untuk Class Player.
 */
class PlayerTest {

    @Test
    void testPlayerInitialization() {
        // 1. Setup
        String name = "TestPlayer";
        Color color = Color.RED;

        // 2. Action
        Player player = new Player(name, color);

        // 3. Assertion (Verifikasi)
        assertNotNull(player, "Objek player tidak boleh null");
        assertEquals("TestPlayer", player.getName(), "Nama player harus sesuai");
        assertEquals(Color.RED, player.getColor(), "Warna player harus sesuai");
        assertTrue(player.isAlive(), "Player baru harusnya statusnya Alive");
    }

    @Test
    void testSetAlive() {
        Player player = new Player("Player1", Color.BLUE);
        
        // Ubah status jadi mati
        player.setAlive(false);
        assertFalse(player.isAlive(), "Status harusnya mati (false)");

        // Hidupkan kembali
        player.setAlive(true);
        assertTrue(player.isAlive(), "Status harusnya hidup (true)");
    }
}