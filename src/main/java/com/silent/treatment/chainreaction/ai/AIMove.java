package com.silent.treatment.chainreaction.ai;

public class AIMove implements Comparable<AIMove> {
    public int x;
    public int y;
    public double score;

    public AIMove(int x, int y, double score) {
        this.x = x;
        this.y = y;
        this.score = score;
    }

    public AIMove(int x, int y) {
        this(x, y, 0.0);
    }

    @Override
    public int compareTo(AIMove other) {
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("Move(%d, %d) Score: %.2f", x, y, score);
    }
}