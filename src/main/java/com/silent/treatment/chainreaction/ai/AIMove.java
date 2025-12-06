package com.silent.treatment.chainreaction.ai;

public class AIMove implements Comparable<AIMove> {
    private int x;
    private int y;
    private double score;

    public AIMove(int x, int y, double score) {
        this.x = x;
        this.y = y;
        this.score = score;
    }

    public AIMove(int x, int y) {
        this(x, y, 0.0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(AIMove other) {
        return Double.compare(other.score, this.score);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AIMove aiMove = (AIMove) obj;
        return x == aiMove.x && y == aiMove.y && Double.compare(aiMove.score, score) == 0;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        result = 31 * result + Double.hashCode(score);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Move(%d, %d) Score: %.2f", x, y, score);
    }
}