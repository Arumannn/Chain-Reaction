package com.silent.treatment.chainreaction.model;

import javafx.scene.paint.Color;

public class Player {
    private final int id;
    private final String name;
    private final Color color;
    private boolean isActive;

    public Player(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isActive = true;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}