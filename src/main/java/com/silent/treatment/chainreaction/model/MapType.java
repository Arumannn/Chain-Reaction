package com.silent.treatment.chainreaction.model;

public enum MapType {
    
    SMALL("Small (9x6)", 9, 6),
    MEDIUM("Medium (10x10)", 10, 10),
    WIDE("Wide (6x9)", 9, 6),
    LONG("Long (6x12)", 12, 6),
    
    DONUT("Hollow Square", 8, 8) {
        @Override
        public boolean isValid(int x, int y) {
            if (x >= 3 && x <= 4 && y >= 3 && y <= 4) return false;
            return true;
        }
    },

    PLUS("Plus Shape", 9, 9) {
        @Override
        public boolean isValid(int x, int y) {
            boolean inVerticalBar = (x >= 3 && x <= 5);
            boolean inHorizontalBar = (y >= 3 && y <= 5);
            return inVerticalBar || inHorizontalBar;
        }
    };

    private final String label;
    private final int width;
    private final int height;

    MapType(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    public boolean isValid(int x, int y) {
        return true;
    }

    public String getLabel() { return label; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    @Override
    public String toString() { return label; }
}