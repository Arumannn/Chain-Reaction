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
    },

    DIAMOND("Diamond", 11, 11) {
        @Override
        public boolean isValid(int x, int y) {
            int cx = 5; 
            int cy = 5; 
            int radius = 5;
            boolean baseDiamond = Math.abs(x - cx) + Math.abs(y - cy) <= radius;
            
            boolean topTipSupport = (y == 0 && (x == 4 || x == 6));
            boolean bottomTipSupport = (y == 10 && (x == 4 || x == 6));
            boolean leftTipSupport = (x == 0 && (y == 4 || y == 6));
            boolean rightTipSupport = (x == 10 && (y == 4 || y == 6));
            
            return baseDiamond || topTipSupport || bottomTipSupport || leftTipSupport || rightTipSupport;
        }
    },
    
    HOURGLASS("Hourglass", 9, 9) {
        @Override
        public boolean isValid(int x, int y) {
            boolean baseShape;
            if (y <= 4) {
                baseShape = x >= y && x <= (8 - y);
            } else {
                baseShape = x >= (8 - y) && x <= y;
            }

            boolean topCornersSupport = (y == 1 && (x == 0 || x == 8));
            
            boolean bottomCornersSupport = (y == 7 && (x == 0 || x == 8));
            
            return baseShape || topCornersSupport || bottomCornersSupport;
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