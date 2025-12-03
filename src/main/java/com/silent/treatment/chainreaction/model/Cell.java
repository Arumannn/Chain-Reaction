package com.silent.treatment.chainreaction.model;

import com.silent.treatment.chainreaction.core.SoundManager;

import com.silent.treatment.chainreaction.strategy.ExplosionStrategy;
import com.silent.treatment.chainreaction.strategy.StandardExplosionStrategy;
import com.silent.treatment.chainreaction.view.GameObserver;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private final int x, y;
    private int currentOrbs;
    private int criticalMass;
    private Player owner;
    private List<Cell> neighbors;

    // Strategy Pattern
    private ExplosionStrategy explosionStrategy;

    // Observer Pattern
    private List<GameObserver> observers;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.currentOrbs = 0;
        this.neighbors = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.explosionStrategy = new StandardExplosionStrategy(); // Default Strategy
    }

    // FR-1.1: Set Tetangga & Tentukan Critical Mass
    public void setNeighbors(List<Cell> neighbors) {
        this.neighbors = neighbors;
        this.criticalMass = neighbors.size();
    }

    // FR-2.1 & FR-2.2: Tambah Orb & Cek Ledakan
    public void addOrb(Player player, Board board) {
        this.owner = player; // Update Owner
        this.currentOrbs++;

        SoundManager.getInstance().playSFX(SoundManager.SFX_POP);

        notifyObservers(); // Update UI

        // Cek Overload
        if (this.currentOrbs >= this.criticalMass) {
            explosionStrategy.explode(this, board, player);
        }
    }
    
    /**
     * Menambahkan multiple orbs sekaligus.
     * Digunakan untuk batch processing ketika multiple explosions menargetkan cell yang sama.
     * 
     * @param count Jumlah orb yang akan ditambahkan
     * @param player Player pemilik orb
     * @param board Board reference
     */
    public void addOrbs(int count, Player player, Board board) {
        if (count <= 0) return;
        
        this.owner = player; // Update Owner
        this.currentOrbs += count;

        notifyObservers(); // Update UI

        // Cek Overload - hanya cek sekali setelah semua orb ditambahkan
        if (this.currentOrbs >= this.criticalMass) {
            explosionStrategy.explode(this, board, player);
        }
    }

    // Setter Getter
    public void setOrbs(int orbs) {
        this.currentOrbs = orbs;
        if (currentOrbs == 0) this.owner = null; // Reset owner jika kosong (opsional)
        notifyObservers();
    }

    public int getOrbs() { return currentOrbs; }
    public int getCriticalMass() { return criticalMass; }
    public Player getOwner() { return owner; }
    public List<Cell> getNeighbors() { return neighbors; }
    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setExplosionStrategy(ExplosionStrategy strategy) {
        this.explosionStrategy = strategy;
    }

    // Observer Methods
    public void attach(GameObserver observer) { observers.add(observer); }
    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.update(this);
        }
    }
}