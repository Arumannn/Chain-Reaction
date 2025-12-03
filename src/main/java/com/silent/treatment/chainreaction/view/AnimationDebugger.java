package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.model.Cell;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.*;

public class AnimationDebugger {
    
    // [CHANGED] Set to false to disable debug reports
    private static final boolean DEBUG_ENABLED = false; 
    
    // Tracking data
    private static Map<Cell, List<CoordinateRecord>> cellCoordinateHistory = new HashMap<>();
    private static Map<Cell, List<ExplosionRecord>> cellExplosionHistory = new HashMap<>();
    private static Map<String, Long> timingRecords = new HashMap<>();
    private static int coordinateCalculationCount = 0;
    
    public static void recordCoordinate(Cell cell, Node cellView, Pane container, 
                                       Point2D calculatedPoint, String method) {
        if (!DEBUG_ENABLED) return;
        
        coordinateCalculationCount++;
        long timestamp = System.currentTimeMillis();
        
        CoordinateRecord record = new CoordinateRecord(
            cell, calculatedPoint, method, timestamp, coordinateCalculationCount
        );
        
        cellCoordinateHistory.computeIfAbsent(cell, k -> new ArrayList<>()).add(record);
        
        List<CoordinateRecord> history = cellCoordinateHistory.get(cell);
        if (history.size() > 1) {
            CoordinateRecord prev = history.get(history.size() - 2);
            long timeDiff = timestamp - prev.timestamp;
            if (timeDiff < 100) {
                System.out.println(String.format(
                    "[DEBUG] Multiple coordinate calculations for Cell(%d,%d) within %dms:\n" +
                    "  Previous: %s at count %d\n" +
                    "  Current:  %s at count %d\n" +
                    "  Difference: (%.2f, %.2f)",
                    cell.getX(), cell.getY(), timeDiff,
                    prev.point, prev.calculationCount,
                    calculatedPoint, coordinateCalculationCount,
                    calculatedPoint.getX() - prev.point.getX(),
                    calculatedPoint.getY() - prev.point.getY()
                ));
            }
        }
    }
    
    public static void recordExplosion(Cell explodingCell, List<Cell> targetCells, 
                                      String animationType, long timestamp) {
        if (!DEBUG_ENABLED) return;
        
        ExplosionRecord record = new ExplosionRecord(
            explodingCell, targetCells, animationType, timestamp
        );
        
        for (Cell target : targetCells) {
            cellExplosionHistory.computeIfAbsent(target, k -> new ArrayList<>()).add(record);
            
            List<ExplosionRecord> explosions = cellExplosionHistory.get(target);
            if (explosions.size() > 1) {
                ExplosionRecord prev = explosions.get(explosions.size() - 2);
                long timeDiff = timestamp - prev.timestamp;
                if (timeDiff < 200) {
                    System.out.println(String.format(
                        "[DEBUG] ⚠️ MULTIPLE EXPLOSIONS to Cell(%d,%d) within %dms:\n" +
                        "  Explosion 1: From Cell(%d,%d) at %d\n" +
                        "  Explosion 2: From Cell(%d,%d) at %d",
                        target.getX(), target.getY(), timeDiff,
                        prev.explodingCell.getX(), prev.explodingCell.getY(), prev.timestamp,
                        explodingCell.getX(), explodingCell.getY(), timestamp
                    ));
                }
            }
        }
    }
    
    public static void recordTiming(String event, long timestamp) {
        if (!DEBUG_ENABLED) return;
        
        if (timingRecords.containsKey(event)) {
            long prevTime = timingRecords.get(event);
            long diff = timestamp - prevTime;
            System.out.println(String.format(
                "[DEBUG] Timing: %s - %dms since last occurrence",
                event, diff
            ));
        }
        timingRecords.put(event, timestamp);
    }
    
    public static void printCellSummary(Cell cell) {
        if (!DEBUG_ENABLED) return;
        
        System.out.println(String.format(
            "\n[DEBUG] === Cell(%d,%d) Summary ===",
            cell.getX(), cell.getY()
        ));
        
        List<CoordinateRecord> coords = cellCoordinateHistory.get(cell);
        if (coords != null && !coords.isEmpty()) {
            System.out.println("  Coordinate Calculations: " + coords.size());
            for (int i = 0; i < coords.size(); i++) {
                CoordinateRecord r = coords.get(i);
                System.out.println(String.format(
                    "    %d. %s: (%.2f, %.2f) at count %d, time %d",
                    i + 1, r.method, r.point.getX(), r.point.getY(), 
                    r.calculationCount, r.timestamp
                ));
            }
        }
        
        List<ExplosionRecord> explosions = cellExplosionHistory.get(cell);
        if (explosions != null && !explosions.isEmpty()) {
            System.out.println("  Explosions Received: " + explosions.size());
            for (int i = 0; i < explosions.size(); i++) {
                ExplosionRecord r = explosions.get(i);
                System.out.println(String.format(
                    "    %d. From Cell(%d,%d), type: %s, time: %d",
                    i + 1, r.explodingCell.getX(), r.explodingCell.getY(),
                    r.animationType, r.timestamp
                ));
            }
        }
        System.out.println();
    }
    
    public static void clear() {
        cellCoordinateHistory.clear();
        cellExplosionHistory.clear();
        timingRecords.clear();
        coordinateCalculationCount = 0;
    }
    
    public static void printProblematicCells() {
        if (!DEBUG_ENABLED) return;
        
        System.out.println("\n[DEBUG] === Problematic Cells Report ===");
        
        for (Map.Entry<Cell, List<ExplosionRecord>> entry : cellExplosionHistory.entrySet()) {
            Cell cell = entry.getKey();
            List<ExplosionRecord> explosions = entry.getValue();
            
            if (explosions.size() > 1) {
                System.out.println(String.format(
                    "  Cell(%d,%d): Received %d explosions",
                    cell.getX(), cell.getY(), explosions.size()
                ));
                
                List<CoordinateRecord> coords = cellCoordinateHistory.get(cell);
                if (coords != null && coords.size() > 1) {
                    Set<Point2D> uniqueCoords = new HashSet<>();
                    for (CoordinateRecord cr : coords) {
                        uniqueCoords.add(cr.point);
                    }
                    if (uniqueCoords.size() > 1) {
                        System.out.println(String.format(
                            "    ⚠️ INCONSISTENT COORDINATES: %d unique positions found!",
                            uniqueCoords.size()
                        ));
                        for (Point2D p : uniqueCoords) {
                            System.out.println(String.format("      - (%.2f, %.2f)", p.getX(), p.getY()));
                        }
                    }
                }
            }
        }
        System.out.println();
    }
    
    private static class CoordinateRecord {
        Cell cell;
        Point2D point;
        String method;
        long timestamp;
        int calculationCount;
        
        CoordinateRecord(Cell cell, Point2D point, String method, long timestamp, int count) {
            this.cell = cell;
            this.point = point;
            this.method = method;
            this.timestamp = timestamp;
            this.calculationCount = count;
        }
    }
    
    private static class ExplosionRecord {
        Cell explodingCell;
        List<Cell> targetCells;
        String animationType;
        long timestamp;
        
        ExplosionRecord(Cell explodingCell, List<Cell> targetCells, String animationType, long timestamp) {
            this.explodingCell = explodingCell;
            this.targetCells = targetCells;
            this.animationType = animationType;
            this.timestamp = timestamp;
        }
    }
}