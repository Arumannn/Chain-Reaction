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

/**
 * Class untuk menangani animasi wave/ripple effect saat orb berpindah.
 * 
 * Fitur:
 * - Wave/ripple animation dengan efek scaling dan opacity
 * - Path transition untuk pergerakan orb dari cell asal ke tujuan
 * - Sequential animation support untuk multiple orbs
 * - Callback system untuk synchronization
 */
public class ExplosionAnimation {
    
    // Durasi animasi (dalam milliseconds)
    private static final double WAVE_DURATION = 400.0;
    private static final double ORB_MOVEMENT_DURATION = 300.0;
    private static final double RIPPLE_DELAY = 50.0; // Delay antar ripple
    
    // Coordinate cache untuk mencegah inconsistent coordinates ketika multiple explosions
    // Key: Cell, Value: Cached coordinate
    private static Map<Cell, Point2D> coordinateCache = new HashMap<>();
    private static Map<Cell, Node> cellToNodeMap = new HashMap<>(); // Mapping untuk cache lookup
    
    /**
     * Membuat animasi wave/ripple effect di cell yang meledak.
     * 
     * @param cellView Node visual dari cell (StackPane atau Pane)
     * @param color Warna player untuk efek wave
     * @param container Container untuk menambahkan ripple effects
     * @param onComplete Callback ketika animasi selesai
     * @return Timeline animation
     */
    public static Timeline createWaveAnimation(Node cellView, Color color, Pane container, Runnable onComplete) {
        Timeline timeline = new Timeline();
        
        // Buat ripple circles untuk efek gelombang
        List<Circle> ripples = new ArrayList<>();
        
        // Hitung posisi center cell relatif terhadap container (menggunakan konversi koordinat yang benar)
        javafx.geometry.Point2D centerPoint = getCenterInContainer(cellView, container);
        double centerX = centerPoint.getX();
        double centerY = centerPoint.getY();
        
        // Buat 3 layer ripple dengan delay berbeda
        for (int i = 0; i < 3; i++) {
            Circle ripple = new Circle();
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(color);
            ripple.setStrokeWidth(2);
            ripple.setOpacity(0.8);
            ripple.setRadius(0);
            
            // Gunakan layoutX/layoutY untuk konsistensi dengan orb movement
            ripple.setLayoutX(centerX);
            ripple.setLayoutY(centerY);
            ripple.setCenterX(0); // Center relatif terhadap layout position
            ripple.setCenterY(0);
            
            // Tambahkan blur effect untuk smoothness
            GaussianBlur blur = new GaussianBlur(5);
            ripple.setEffect(blur);
            
            ripples.add(ripple);
            container.getChildren().add(ripple);
        }
        
        // Animasi untuk setiap ripple
        for (int i = 0; i < ripples.size(); i++) {
            Circle ripple = ripples.get(i);
            double startTime = i * RIPPLE_DELAY;
            
            // Scale animation: dari 0 ke max radius
            KeyValue scaleStart = new KeyValue(ripple.radiusProperty(), 0);
            KeyValue scaleEnd = new KeyValue(ripple.radiusProperty(), 40);
            
            // Opacity animation: fade out
            KeyValue opacityStart = new KeyValue(ripple.opacityProperty(), 0.8);
            KeyValue opacityEnd = new KeyValue(ripple.opacityProperty(), 0.0);
            
            // Stroke width animation: menipis saat membesar
            KeyValue strokeStart = new KeyValue(ripple.strokeWidthProperty(), 2);
            KeyValue strokeEnd = new KeyValue(ripple.strokeWidthProperty(), 0.5);
            
            KeyFrame startFrame = new KeyFrame(Duration.millis(startTime), scaleStart, opacityStart, strokeStart);
            KeyFrame endFrame = new KeyFrame(
                Duration.millis(startTime + WAVE_DURATION),
                scaleEnd, opacityEnd, strokeEnd
            );
            
            timeline.getKeyFrames().addAll(startFrame, endFrame);
        }
        
        // Cleanup dan callback
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
    
    /**
     * Clear coordinate cache (dipanggil saat game reset atau batch baru).
     */
    public static void clearCoordinateCache() {
        coordinateCache.clear();
        cellToNodeMap.clear();
    }
    
    /**
     * Get cache size untuk debugging.
     */
    public static int getCacheSize() {
        return coordinateCache.size();
    }
    
    /**
     * Pre-calculate coordinate untuk cell tertentu.
     * Digunakan untuk memastikan koordinat konsisten sebelum membuat animasi.
     * Method ini memastikan koordinat dihitung sekali dan di-cache untuk digunakan
     * oleh semua animasi yang menargetkan cell yang sama.
     * 
     * IMPORTANT: Method ini akan menggunakan cache jika sudah ada untuk konsistensi antar batch.
     * Hanya menghitung ulang jika cache belum ada atau koordinat berubah signifikan.
     */
    public static Point2D preCalculateCoordinate(Node cellView, Pane container, Cell cell) {
        // Set userData jika belum ada (untuk cache lookup)
        if (cellView.getUserData() == null) {
            cellView.setUserData(cell);
        }
        
        // Cek cache dulu - jika sudah ada, gunakan untuk konsistensi
        if (coordinateCache.containsKey(cell)) {
            Point2D cached = coordinateCache.get(cell);
            System.out.println(String.format(
                "[DEBUG] ✅ Using existing cache for Cell(%d,%d): (%.2f, %.2f)",
                cell.getX(), cell.getY(), cached.getX(), cached.getY()
            ));
            return cached;
        }
        
        // Jika belum di-cache, hitung dan cache
        Point2D coord = calculateCoordinateDirectly(cellView, container);
        coordinateCache.put(cell, coord);
        cellToNodeMap.put(cell, cellView);
        
        System.out.println(String.format(
            "[DEBUG] ✅ Pre-calculated NEW coordinate for Cell(%d,%d): (%.2f, %.2f)",
            cell.getX(), cell.getY(), coord.getX(), coord.getY()
        ));
        
        return coord;
    }
    
    /**
     * Menghitung koordinat secara langsung tanpa cek cache.
     * Digunakan untuk pre-calculation yang memastikan konsistensi.
     */
    private static Point2D calculateCoordinateDirectly(Node cellView, Pane targetContainer) {
        // Pastikan node sudah di-layout dan di scene
        if (cellView.getScene() == null || targetContainer.getScene() == null) {
            // Fallback: gunakan boundsInParent jika belum di scene
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            
            // Hitung posisi relatif
            double centerX = cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX();
            double centerY = cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY();
            
            return new javafx.geometry.Point2D(centerX, centerY);
        }
        
        // Dapatkan bounds cellView dalam local coordinates
        javafx.geometry.Bounds localBounds = cellView.getBoundsInLocal();
        if (localBounds.getWidth() == 0 || localBounds.getHeight() == 0) {
            // Jika bounds belum valid, gunakan layoutBounds
            localBounds = cellView.getLayoutBounds();
        }
        
        double centerX = localBounds.getMinX() + localBounds.getWidth() / 2;
        double centerY = localBounds.getMinY() + localBounds.getHeight() / 2;
        
        // Konversi ke scene coordinates
        javafx.geometry.Point2D scenePoint = cellView.localToScene(centerX, centerY);
        
        // Validasi scenePoint
        if (Double.isNaN(scenePoint.getX()) || Double.isNaN(scenePoint.getY()) ||
            Double.isInfinite(scenePoint.getX()) || Double.isInfinite(scenePoint.getY())) {
            // Fallback ke boundsInParent jika konversi gagal
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            return new javafx.geometry.Point2D(
                cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX(),
                cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY()
            );
        }
        
        // Konversi dari scene ke container coordinates
        javafx.geometry.Point2D containerPoint = targetContainer.sceneToLocal(scenePoint);
        
        // Validasi containerPoint
        if (Double.isNaN(containerPoint.getX()) || Double.isNaN(containerPoint.getY()) ||
            Double.isInfinite(containerPoint.getX()) || Double.isInfinite(containerPoint.getY())) {
            // Fallback ke boundsInParent
            javafx.geometry.Bounds cellBounds = cellView.getBoundsInParent();
            javafx.geometry.Bounds containerBounds = targetContainer.getBoundsInParent();
            return new javafx.geometry.Point2D(
                cellBounds.getMinX() + cellBounds.getWidth() / 2 - containerBounds.getMinX(),
                cellBounds.getMinY() + cellBounds.getHeight() / 2 - containerBounds.getMinY()
            );
        }
        
        return containerPoint;
    }
    
    /**
     * Helper method untuk mendapatkan center point dari cellView relatif terhadap container.
     * Menggunakan cache untuk mencegah inconsistent coordinates ketika multiple explosions.
     * Mengkonversi koordinat melalui scene untuk akurasi.
     * Memastikan layout sudah selesai sebelum menghitung koordinat.
     * 
     * IMPORTANT: Method ini SELALU cek cache dulu sebelum menghitung.
     */
    private static javafx.geometry.Point2D getCenterInContainer(Node cellView, Pane targetContainer) {
        // Cek cache dulu (dengan atau tanpa cellData)
        Object cellData = cellView.getUserData();
        if (cellData instanceof Cell) {
            Cell cell = (Cell) cellData;
            
            // Update node mapping
            cellToNodeMap.put(cell, cellView);
            
            // Cek cache - PRIORITAS UTAMA
            if (coordinateCache.containsKey(cell)) {
                Point2D cached = coordinateCache.get(cell);
                // Debug: Log cache hit
                System.out.println(String.format(
                    "[DEBUG] ✅ Cache HIT for Cell(%d,%d): Using cached coordinate (%.2f, %.2f)",
                    cell.getX(), cell.getY(), cached.getX(), cached.getY()
                ));
                return cached;
            } else {
                // Cache miss - ini seharusnya tidak terjadi jika pre-calculation benar
                System.out.println(String.format(
                    "[DEBUG] ❌ Cache MISS for Cell(%d,%d) - Pre-calculation mungkin belum dilakukan!",
                    cell.getX(), cell.getY()
                ));
            }
        } else {
            // Cell data tidak ada - ini masalah
            System.out.println(String.format(
                "[DEBUG] ⚠️ WARNING: cellView.getUserData() is null - cannot use cache!"
            ));
        }
        
        // Jika tidak ada di cache, hitung menggunakan method yang sama dengan pre-calculation
        Point2D containerPoint = calculateCoordinateDirectly(cellView, targetContainer);
        
        // Cache koordinat jika cell tersedia
        if (cellData instanceof Cell) {
            Cell cell = (Cell) cellData;
            coordinateCache.put(cell, containerPoint);
            
            // Debug: Record coordinate calculation
            AnimationDebugger.recordCoordinate(
                cell, cellView, targetContainer, containerPoint, 
                "getCenterInContainer"
            );
            
            System.out.println(String.format(
                "[DEBUG] ⚠️ Cache MISS - Calculated new coordinate for Cell(%d,%d): (%.2f, %.2f)",
                cell.getX(), cell.getY(), containerPoint.getX(), containerPoint.getY()
            ));
        }
        
        return containerPoint;
    }
    
    /**
     * Membuat animasi pergerakan orb dari cell asal ke cell tujuan.
     * Menggunakan PathTransition untuk smooth movement dengan efek wave.
     * 
     * @param fromCellView Node visual cell asal
     * @param toCellView Node visual cell tujuan
     * @param orbVisual Visual representation dari orb (Circle)
     * @param color Warna orb
     * @param container Container parent untuk menambahkan orb visual
     * @param onComplete Callback ketika animasi selesai
     * @return ParallelTransition yang menggabungkan movement dan wave effect
     */
    public static ParallelTransition createOrbMovementAnimation(
            Node fromCellView, Node toCellView,
            Node orbVisual, Color color,
            Pane container,
            Runnable onComplete) {
        
        // Konversi koordinat dari cellView (di GridPane) ke container (animationLayer di StackPane)
        // Keduanya adalah sibling di StackPane, jadi kita perlu konversi melalui scene
        
        // Hitung posisi center dari kedua cell relatif terhadap container
        javafx.geometry.Point2D fromPoint = getCenterInContainer(fromCellView, container);
        javafx.geometry.Point2D toPoint = getCenterInContainer(toCellView, container);
        
        double fromX = fromPoint.getX();
        double fromY = fromPoint.getY();
        double toX = toPoint.getX();
        double toY = toPoint.getY();
        
        // Set posisi awal orb (center orb pada center cell)
        orbVisual.setLayoutX(fromX);
        orbVisual.setLayoutY(fromY);
        orbVisual.setOpacity(1.0);
        
        // Tambahkan orb ke container jika belum ada
        if (!container.getChildren().contains(orbVisual)) {
            container.getChildren().add(orbVisual);
        }
        
        // 1. TranslateTransition untuk pergerakan linear
        TranslateTransition translate = new TranslateTransition(
            Duration.millis(ORB_MOVEMENT_DURATION), orbVisual
        );
        translate.setFromX(0);
        translate.setFromY(0);
        translate.setToX(toX - fromX);
        translate.setToY(toY - fromY);
        translate.setInterpolator(Interpolator.EASE_OUT);
        
        // 2. Scale animation untuk efek "bounce" saat bergerak
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
        
        // 3. Opacity pulse untuk efek glow
        Timeline opacityTimeline = new Timeline();
        KeyValue opacityStart = new KeyValue(orbVisual.opacityProperty(), 1.0);
        KeyValue opacityMid = new KeyValue(orbVisual.opacityProperty(), 0.7);
        KeyValue opacityEnd = new KeyValue(orbVisual.opacityProperty(), 1.0);
        
        opacityTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, opacityStart),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION / 3), opacityMid),
            new KeyFrame(Duration.millis(ORB_MOVEMENT_DURATION), opacityEnd)
        );
        
        // Gabungkan semua animasi
        ParallelTransition parallel = new ParallelTransition(translate, scaleTimeline, opacityTimeline);
        
        // Cleanup dan callback
        parallel.setOnFinished(e -> {
            container.getChildren().remove(orbVisual);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return parallel;
    }
    
    /**
     * Membuat animasi spreading wave seperti gelombang air yang menyebar ke semua tetangga.
     * Semua animasi berjalan secara parallel (bersamaan) untuk efek menyebar yang natural.
     * 
     * @param centerCellView Node visual cell pusat yang meledak
     * @param neighborViews List node visual dari semua tetangga
     * @param color Warna player
     * @param container Container untuk animasi
     * @param onComplete Callback ketika semua animasi selesai
     * @return ParallelTransition yang menjalankan semua animasi secara bersamaan
     */
    public static ParallelTransition createSpreadingWaveAnimation(
            Node centerCellView, List<Node> neighborViews,
            Color color, Pane container,
            Runnable onComplete) {
        
        ParallelTransition parallel = new ParallelTransition();
        
        // 1. Wave animation di cell pusat (explosion center)
        Timeline centerWave = createWaveAnimation(centerCellView, color, container, null);
        parallel.getChildren().add(centerWave);
        
        // 2. Expanding ripple dari center ke semua tetangga secara bersamaan
        // Gunakan konversi koordinat yang benar - SELALU gunakan cache
        javafx.geometry.Point2D centerPoint = getCenterInContainer(centerCellView, container);
        double centerX = centerPoint.getX();
        double centerY = centerPoint.getY();
        
        // Buat expanding ripple untuk setiap tetangga
        for (Node neighborView : neighborViews) {
            if (neighborView == null) continue;
            
            // SELALU gunakan getCenterInContainer yang akan cek cache
            javafx.geometry.Point2D targetPoint = getCenterInContainer(neighborView, container);
            double targetX = targetPoint.getX();
            double targetY = targetPoint.getY();
            
            // Debug: Log target coordinates
            Object targetCellData = neighborView.getUserData();
            if (targetCellData instanceof com.silent.treatment.chainreaction.model.Cell) {
                com.silent.treatment.chainreaction.model.Cell targetCell = 
                    (com.silent.treatment.chainreaction.model.Cell) targetCellData;
                System.out.println(String.format(
                    "[DEBUG] Creating ripple: Center(%.2f, %.2f) -> Target Cell(%d,%d) at (%.2f, %.2f) [CACHED: %s]",
                    centerX, centerY, targetCell.getX(), targetCell.getY(), targetX, targetY,
                    coordinateCache.containsKey(targetCell) ? "YES" : "NO"
                ));
            }
            
            // Hitung jarak dan arah
            double distance = Math.sqrt(Math.pow(targetX - centerX, 2) + Math.pow(targetY - centerY, 2));
            
            // Buat expanding ripple yang bergerak dari center ke target
            Timeline expandingRipple = createExpandingRipple(
                centerX, centerY, targetX, targetY, distance, color, container
            );
            parallel.getChildren().add(expandingRipple);
            
            // Wave animation di cell tujuan (muncul saat ripple hampir sampai)
            Timeline targetWave = createWaveAnimation(neighborView, color, container, null);
            // Delay wave di target agar muncul saat ripple hampir sampai (80% dari perjalanan)
            targetWave.setDelay(Duration.millis(ORB_MOVEMENT_DURATION * 0.8));
            parallel.getChildren().add(targetWave);
        }
        
        // Callback di akhir
        parallel.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return parallel;
    }
    
    /**
     * Membuat expanding ripple effect yang bergerak dari titik asal ke titik tujuan.
     * Seperti gelombang air yang menyebar.
     */
    private static Timeline createExpandingRipple(
            double fromX, double fromY, double toX, double toY,
            double distance, Color color, Pane container) {
        
        Timeline timeline = new Timeline();
        List<Circle> ripples = new ArrayList<>();
        
        // Buat beberapa layer ripple untuk efek yang lebih kaya
        int rippleLayers = 2;
        for (int layer = 0; layer < rippleLayers; layer++) {
            Circle ripple = new Circle();
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(color);
            ripple.setStrokeWidth(2 - (layer * 0.5));
            ripple.setOpacity(0.8 - (layer * 0.2));
            ripple.setRadius(0);
            
            // Posisi awal di center (gunakan layout untuk konsistensi)
            ripple.setLayoutX(fromX);
            ripple.setLayoutY(fromY);
            ripple.setCenterX(0); // Center relatif terhadap layout position
            ripple.setCenterY(0);
            
            // Blur effect
            GaussianBlur blur = new GaussianBlur(3 + layer);
            ripple.setEffect(blur);
            
            ripples.add(ripple);
            container.getChildren().add(ripple);
            
            // Animasi: expand dari center ke target
            double delay = layer * 30; // Stagger delay untuk setiap layer
            double duration = ORB_MOVEMENT_DURATION;
            
            // Scale radius dari 0 ke distance
            KeyValue radiusStart = new KeyValue(ripple.radiusProperty(), 0);
            KeyValue radiusEnd = new KeyValue(ripple.radiusProperty(), distance);
            
            // Opacity fade
            KeyValue opacityStart = new KeyValue(ripple.opacityProperty(), 0.8 - (layer * 0.2));
            KeyValue opacityEnd = new KeyValue(ripple.opacityProperty(), 0.0);
            
            // Stroke width menipis
            KeyValue strokeStart = new KeyValue(ripple.strokeWidthProperty(), 2 - (layer * 0.5));
            KeyValue strokeEnd = new KeyValue(ripple.strokeWidthProperty(), 0.3);
            
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(delay), radiusStart, opacityStart, strokeStart),
                new KeyFrame(Duration.millis(delay + duration), radiusEnd, opacityEnd, strokeEnd)
            );
        }
        
        // Cleanup
        timeline.setOnFinished(e -> {
            for (Circle ripple : ripples) {
                container.getChildren().remove(ripple);
            }
        });
        
        return timeline;
    }
    
    /**
     * DEPRECATED: Method lama untuk animasi per-orb.
     * Digunakan createSpreadingWaveAnimation untuk efek menyebar yang lebih natural.
     */
    @Deprecated
    public static SequentialTransition createCompleteExplosionAnimation(
            Node fromCellView, Node toCellView,
            Node orbVisual, Color color,
            Pane container,
            Runnable onComplete) {
        
        SequentialTransition sequence = new SequentialTransition();
        
        // 1. Wave animation di cell asal
        Timeline waveFrom = createWaveAnimation(fromCellView, color, container, null);
        
        // 2. Orb movement animation
        ParallelTransition orbMove = createOrbMovementAnimation(
            fromCellView, toCellView, orbVisual, color, container, null
        );
        
        // 3. Wave animation di cell tujuan (setelah orb sampai)
        Timeline waveTo = createWaveAnimation(toCellView, color, container, null);
        
        // Urutkan animasi
        sequence.getChildren().addAll(waveFrom, orbMove, waveTo);
        
        // Callback di akhir
        sequence.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        return sequence;
    }
    
    /**
     * Membuat animasi shake/pulse untuk cell yang akan meledak (pre-explosion indicator).
     */
    public static Timeline createPreExplosionPulse(Node cellView) {
        Timeline timeline = new Timeline();
        
        // Scale animation: pulse effect
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
        
        timeline.setCycleCount(2); // Pulse 2 kali
        
        return timeline;
    }
}

