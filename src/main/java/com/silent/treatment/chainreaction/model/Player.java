package com.silent.treatment.chainreaction.model;

import javafx.scene.paint.Color;

public class Player {
    private String name;
    private Color color;
    private boolean isAlive;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.isAlive = true; // Default hidup saat game mulai
    }

    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { this.isAlive = alive; }
    
    public String getName() { return name; }
    public Color getColor() { return color; }
}