package com.silent.treatment.chainreaction.model;

import javafx.scene.paint.Color;

public class Player {
    private String name;
    private Color color;
    private boolean isAlive;
    private boolean hasPlayed; // Track if player has made their first move

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.isAlive = true; // Default hidup saat game mulai
        this.hasPlayed = false;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public boolean hasPlayed() {
        return hasPlayed;
    }

    public void setHasPlayed(boolean hasPlayed) {
        this.hasPlayed = hasPlayed;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Player player = (Player) o;

        if (name != null ? !name.equals(player.name) : player.name != null)
            return false;
        return color != null ? color.equals(player.color) : player.color == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}