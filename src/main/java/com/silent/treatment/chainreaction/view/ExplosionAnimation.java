package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.model.Cell;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplosionAnimation {
    
    private static final double WAVE_DURATION = 400.0;
    private static final double ORB_MOVEMENT_DURATION = 300.0;
    private static final double RIPPLE_DELAY = 50.0; 
    
    private static Map<Cell, Point2D> coordinateCache = new HashMap<>();
    private static Map<Cell, Node> cellToNodeMap = new HashMap<>();
    
    public static Timeline createWaveAnimation(Node cellView, Color color, Pane container, Runnable onComplete) {
        Timeline timeline = new Timeline();
        List<Circle> ripples = new ArrayList<>();
        
        javafx.geometry.Point2D centerPoint = getCenterInContainer(cellView, container);
        double centerX = centerPoint.getX();
        double centerY = centerPoint.getY();
        
        for (int i = 0; i < 3; i++) {
            Circle ripple = new Circle();
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(color);
            ripple.setStrokeWidth(2);
            ripple.setOpacity(0.8);
            ripple.setRadius(0);
            
            ripple.setLayoutX(centerX);
            ripple.setLayoutY(centerY);
            ripple.setCenterX(0);
            ripple.setCenterY(0);
            
            GaussianBlur blur = new GaussianBlur(5);
            ripple.setEffect(blur);
            
            ripples.add(ripple);
            container.getChildren().add(ripple);
        }
        
        for (int i = 0; i < ripples.size(); i++) {
            Circle ripple = ripples.get(i);
            double startTime = i * RIPPLE_DELAY;
            
            KeyValue scaleStart = new KeyValue(ripple.radiusProperty(), 0);
            KeyValue scaleEnd = new KeyValue(ripple.radiusProperty(), 40);
            
            KeyValue opacityStart = new KeyValue(ripple.opacityProperty(), 0.8);
            KeyValue opacityEnd = new KeyValue(ripple.opacityProperty(), 0.0);
            
            KeyValue strokeStart = new KeyValue(ripple.strokeWidthProperty(), 2);
            KeyValue strokeEnd = new KeyValue(ripple.strokeWidthProperty(), 0.5);
            
            KeyFrame startFrame = new KeyFrame(Duration.millis(startTime), scaleStart, opacityStart, strokeStart);
            KeyFrame endFrame = new KeyFrame(
                Duration.millis(startTime + WAVE_DURATION),
                scaleEnd, opacityEnd, strokeEnd
            );
            
            timeline.getKeyFrames().addAll(startFrame, endFrame);
        }
        
        timeline.setOnFinished(e -> {
            for (Circle ripple : ripples) {
                container.getChildren().remove(ripple);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return timeline;
    }
    
    public static void clearCoordinateCache() {
        coordinateCache.clear();
        cellToNodeMap.clear();
    }
    
    public static int getCacheSize() {
        return coordinateCache.size();
    }
    
    public static Point2D preCalculateCoordinate(Node cellView, Pane container, Cell cell) {
        if (cellView.getUserData() == null) {
            cellView.setUserData(cell);
        }
        
        if (coordinateCache.containsKey(cell)) {
            Point2D cached = coordinateCache.get(cell);
            // [CHANGED] Removed debug print
            return cached;
        }
        
        Point2D coord = calculateCoordinateDirectly(cellView, container);
        coordinateCache.put(cell, coord);
        cellToNodeMap.put(cell, cellView);
        
        // [CHANGED] Removed debug print
        
        return coord;
    }
    
    private static Point2D calculateCoordinateDirectly(Node cellView, Pane targetContainer) {
        if (cellView.getScene() == null || targetContainer.getScene() == null) {
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            
            double centerX = cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX();
            double centerY = cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY();
            
            return new javafx.geometry.Point2D(centerX, centerY);
        }
        
        javafx.geometry.Bounds localBounds = cellView.getBoundsInLocal();
        if (localBounds.getWidth() == 0 || localBounds.getHeight() == 0) {
            localBounds = cellView.getLayoutBounds();
        }
        
        double centerX = localBounds.getMinX() + localBounds.getWidth() / 2;
        double centerY = localBounds.getMinY() + localBounds.getHeight() / 2;
        
        javafx.geometry.Point2D scenePoint = cellView.localToScene(centerX, centerY);
        
        if (Double.isNaN(scenePoint.getX()) || Double.isNaN(scenePoint.getY()) ||
            Double.isInfinite(scenePoint.getX()) || Double.isInfinite(scenePoint.getY())) {
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            return new javafx.geometry.Point2D(
                cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX(),
                cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY()
            );
        }
        
        javafx.geometry.Point2D containerPoint = targetContainer.sceneToLocal(scenePoint);
        
        if (Double.isNaN(containerPoint.getX()) || Double.isNaN(containerPoint.getY()) ||
            Double.isInfinite(containerPoint.getX()) || Double.isInfinite(containerPoint.getY())) {
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            return new javafx.geometry.Point2D(
                cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX(),
                cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY()
            );
        }
        
        return containerPoint;
    }
    
    private static javafx.geometry.Point2D getCenterInContainer(Node cellView, Pane targetContainer) {
        Object cellData = cellView.getUserData();
        if (cellData instanceof Cell) {
            Cell cell = (Cell) cellData;
            cellToNodeMap.put(cell, cellView);
            
            if (coordinateCache.containsKey(cell)) {
                Point2D cached = coordinateCache.get(cell);
                // [CHANGED] Removed debug print
                return cached;
            } else {
                // [CHANGED] Removed debug print
            }
        } else {
            // [CHANGED] Removed debug print
        }
        
        Point2D containerPoint = calculateCoordinateDirectly(cellView, targetContainer);
        
        if (cellData instanceof Cell) {
            Cell cell = (Cell) cellData;
            coordinateCache.put(cell, containerPoint);
            
            AnimationDebugger.recordCoordinate(
                cell, cellView, targetContainer, containerPoint, 
                "getCenterInContainer"
            );
            
            // [CHANGED] Removed debug print
        }
        
        return containerPoint;
    }
    
    public static ParallelTransition createOrbMovementAnimation(
            Node fromCellView, Node toCellView,
            Node orbVisual, Color color,
            Pane container,
            Runnable onComplete) {
        
        javafx.geometry.Point2D fromPoint = getCenterInContainer(fromCellView, container);
        javafx.geometry.Point2D toPoint = getCenterInContainer(toCellView, container);
        
        double fromX = fromPoint.getX();
        double fromY = fromPoint.getY();
        double toX = toPoint.getX();
        double toY = toPoint.getY();
        
        orbVisual.setLayoutX(fromX);
        orbVisual.setLayoutY(fromY);
        orbVisual.setOpacity(1.0);
        
        if (!container.getChildren().contains(orbVisual)) {
            container.getChildren().add(orbVisual);
        }
        
        TranslateTransition translate = new TranslateTransition(
            Duration.millis(ORB_MOVEMENT_DURATION), orbVisual
        );
        translate.setFromX(0);
        translate.setFromY(0);
        translate.setToX(toX - fromX);
        translate.setToY(toY - fromY);
        translate.setInterpolator(Interpolator.EASE_OUT);
        
        Timeline scaleTimeline = new Timeline();
        KeyValue scaleStart = new KeyValue(orbVisual.scaleXProperty(), 1.0);
        KeyValue scaleMid = new KeyValue(orbVisual.scaleXProperty(), 1.3);
        KeyValue scaleEnd = new KeyValue(orbVisual.scaleXProperty(), 1.0);
        KeyValue scaleYStart = new KeyValue(orbVisual.scaleYProperty(), 1.0);
        KeyValue scaleYMid = new KeyValue(orbVisual.scaleYProperty(), 1.3);
        KeyValue scaleYEnd = new KeyValue(orbVisual.scaleYProperty(), 1.0);
        
        scaleTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, scaleStart, scaleYStart),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION / 2), scaleMid, scaleYMid),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION), scaleEnd, scaleYEnd)
        );
        
        Timeline opacityTimeline = new Timeline();
        KeyValue opacityStart = new KeyValue(orbVisual.opacityProperty(), 1.0);
        KeyValue opacityMid = new KeyValue(orbVisual.opacityProperty(), 0.7);
        KeyValue opacityEnd = new KeyValue(orbVisual.opacityProperty(), 1.0);
        
        opacityTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, opacityStart),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION / 3), opacityMid),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION), opacityEnd)
        );
        
        ParallelTransition parallel = new ParallelTransition(translate, scaleTimeline, opacityTimeline);
        
        parallel.setOnFinished(e -> {
            container.getChildren().remove(orbVisual);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return parallel;
    }
    
    public static ParallelTransition createSpreadingWaveAnimation(
            Node centerCellView, List<Node> neighborViews,
            Color color, Pane container,
            Runnable onComplete) {
        
        ParallelTransition parallel = new ParallelTransition();
        
        Timeline centerWave = createWaveAnimation(centerCellView, color, container, null);
        parallel.getChildren().add(centerWave);
        
        javafx.geometry.Point2D centerPoint = getCenterInContainer(centerCellView, container);
        double centerX = centerPoint.getX();
        double centerY = centerPoint.getY();
        
        for (Node neighborView : neighborViews) {
            if (neighborView == null) continue;
            
            javafx.geometry.Point2D targetPoint = getCenterInContainer(neighborView, container);
            double targetX = targetPoint.getX();
            double targetY = targetPoint.getY();
            
            // [CHANGED] Removed debug print for ripple creation
            
            double distance = Math.sqrt(Math.pow(targetX - centerX, 2) + Math.pow(targetY - centerY, 2));
            
            Timeline expandingRipple = createExpandingRipple(
                centerX, centerY, targetX, targetY, distance, color, container
            );
            parallel.getChildren().add(expandingRipple);
            
            Timeline targetWave = createWaveAnimation(neighborView, color, container, null);
            targetWave.setDelay(Duration.millis(ORB_MOVEMENT_DURATION * 0.8));
            parallel.getChildren().add(targetWave);
        }
        
        parallel.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return parallel;
    }
    
    private static Timeline createExpandingRipple(
            double fromX, double fromY, double toX, double toY,
            double distance, Color color, Pane container) {
        
        Timeline timeline = new Timeline();
        List<Circle> ripples = new ArrayList<>();
        
        int rippleLayers = 2;
        for (int layer = 0; layer < rippleLayers; layer++) {
            Circle ripple = new Circle();
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(color);
            ripple.setStrokeWidth(2 - (layer * 0.5));
            ripple.setOpacity(0.8 - (layer * 0.2));
            ripple.setRadius(0);
            
            ripple.setLayoutX(fromX);
            ripple.setLayoutY(fromY);
            ripple.setCenterX(0);
            ripple.setCenterY(0);
            
            GaussianBlur blur = new GaussianBlur(3 + layer);
            ripple.setEffect(blur);
            
            ripples.add(ripple);
            container.getChildren().add(ripple);
            
            double delay = layer * 30;
            double duration = ORB_MOVEMENT_DURATION;
            
            KeyValue radiusStart = new KeyValue(ripple.radiusProperty(), 0);
            KeyValue radiusEnd = new KeyValue(ripple.radiusProperty(), distance);
            
            KeyValue opacityStart = new KeyValue(ripple.opacityProperty(), 0.8 - (layer * 0.2));
            KeyValue opacityEnd = new KeyValue(ripple.opacityProperty(), 0.0);
            
            KeyValue strokeStart = new KeyValue(ripple.strokeWidthProperty(), 2 - (layer * 0.5));
            KeyValue strokeEnd = new KeyValue(ripple.strokeWidthProperty(), 0.3);
            
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(delay), radiusStart, opacityStart, strokeStart),
                new KeyFrame(Duration.millis(delay + duration), radiusEnd, opacityEnd, strokeEnd)
            );
        }
        
        timeline.setOnFinished(e -> {
            for (Circle ripple : ripples) {
                container.getChildren().remove(ripple);
            }
        });
        
        return timeline;
    }
    
    @Deprecated
    public static SequentialTransition createCompleteExplosionAnimation(
            Node fromCellView, Node toCellView,
            Node orbVisual, Color color,
            Pane container,
            Runnable onComplete) {
        
        SequentialTransition sequence = new SequentialTransition();
        
        Timeline waveFrom = createWaveAnimation(fromCellView, color, container, null);
        
        ParallelTransition orbMove = createOrbMovementAnimation(
            fromCellView, toCellView, orbVisual, color, container, null
        );
        
        Timeline waveTo = createWaveAnimation(toCellView, color, container, null);
        
        sequence.getChildren().addAll(waveFrom, orbMove, waveTo);
        
        sequence.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return sequence;
    }
    
    public static Timeline createPreExplosionPulse(Node cellView) {
        Timeline timeline = new Timeline();
        
        KeyValue scaleStart = new KeyValue(cellView.scaleXProperty(), 1.0);
        KeyValue scaleMid = new KeyValue(cellView.scaleXProperty(), 1.1);
        KeyValue scaleEnd = new KeyValue(cellView.scaleXProperty(), 1.0);
        KeyValue scaleYStart = new KeyValue(cellView.scaleYProperty(), 1.0);
        KeyValue scaleYMid = new KeyValue(cellView.scaleYProperty(), 1.1);
        KeyValue scaleYEnd = new KeyValue(cellView.scaleYProperty(), 1.0);
        
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, scaleStart, scaleYStart),
            new KeyFrame(Duration.millis(150), scaleMid, scaleYMid),
            new KeyFrame(Duration.millis(300), scaleEnd, scaleYEnd)
        );
        
        timeline.setCycleCount(2); 
        
        return timeline;
    }
}