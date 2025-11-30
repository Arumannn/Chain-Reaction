package com.silent.treatment.chainreaction.view;

import com.silent.treatment.chainreaction.core.ExplosionQueue;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager untuk mengkoordinasikan animasi explosions dengan queue system.
 * 
 * Tanggung jawab:
 * - Monitor ExplosionQueue untuk task baru
 * - Trigger animasi untuk setiap explosion
 * - Execute task setelah animasi selesai
 * - Manage multiple simultaneous animations
 */
public class AnimationManager {
    
    private ExplosionQueue explosionQueue;
    private Pane animationContainer; // Container untuk orb visuals yang bergerak
    private Map<Cell, Node> cellViewMap; // Mapping Cell ke Node visualnya
    
    // Tracking animasi yang sedang berjalan
    private Map<Cell, Animation> activeAnimations;
    
    // Tracking tasks yang sedang dianimasikan (untuk batch processing)
    private Map<Cell, ExplosionQueue.ExplosionTask> animatingTasks;
    
    // Pending completed tasks untuk batch execution
    private List<ExplosionQueue.ExplosionTask> pendingCompletedTasks;
    
    // Timer untuk batch execution delay
    private javafx.animation.Timeline batchExecutionTimer;
    
    public AnimationManager(Pane animationContainer, Map<Cell, Node> cellViewMap) {
        this.explosionQueue = ExplosionQueue.getInstance();
        this.animationContainer = animationContainer;
        this.cellViewMap = cellViewMap;
        this.activeAnimations = new HashMap<>();
        this.animatingTasks = new HashMap<>();
        this.pendingCompletedTasks = new ArrayList<>();
        
        // Setup batch execution timer (delay 50ms untuk mengumpulkan tasks yang selesai bersamaan)
        this.batchExecutionTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(50), e -> executeBatchCompletedTasks())
        );
        this.batchExecutionTimer.setCycleCount(1);
        
        // Setup callback untuk queue empty
        explosionQueue.setOnQueueEmpty(() -> {
            // Semua explosions selesai, bisa lanjut ke turn berikutnya
            System.out.println("All explosions completed");
        });
        
        // Setup callback untuk langsung trigger animasi ketika task di-enqueue
        // Ini memungkinkan multiple explosions berjalan bersamaan
        explosionQueue.setOnTaskEnqueued(() -> {
            // Langsung proses semua pending animations secara parallel
            processAllPendingAnimations();
        });
    }
    
    /**
     * Memproses animasi untuk task tertentu.
     * Bisa dipanggil multiple times untuk different cells secara parallel.
     */
    private void processAnimationForTask(ExplosionQueue.ExplosionTask task) {
        Cell explodingCell = task.getCell();
        Node cellView = cellViewMap.get(explodingCell);
        
        if (cellView == null) {
            // Cell view tidak ditemukan, skip animasi dan execute langsung
            task.execute();
            return;
        }
        
        // Cek apakah cell ini sedang dianimasikan
        if (activeAnimations.containsKey(explodingCell)) {
            return; // Sudah sedang dianimasikan
        }
        
        // Kumpulkan semua neighbor views
        List<Node> neighborViews = new ArrayList<>();
        for (Cell neighbor : explodingCell.getNeighbors()) {
            Node neighborView = cellViewMap.get(neighbor);
            if (neighborView != null) {
                neighborViews.add(neighborView);
            }
        }
        
        // Jika tidak ada neighbor, langsung execute
        if (neighborViews.isEmpty()) {
            task.execute();
            return;
        }
        
        Color playerColor = task.getPlayer().getColor();
        
        // Debug: Record explosion
        List<Cell> targetCells = new ArrayList<>();
        for (Cell neighbor : explodingCell.getNeighbors()) {
            targetCells.add(neighbor);
        }
        AnimationDebugger.recordExplosion(
            explodingCell, targetCells, "spreadingWave", System.currentTimeMillis()
        );
        
        // Buat spreading wave animation (semua tetangga secara bersamaan)
        // Koordinat akan dihitung saat animasi dibuat, yang sudah dalam Platform.runLater
        ParallelTransition spreadingWave = ExplosionAnimation.createSpreadingWaveAnimation(
            cellView, neighborViews, playerColor, animationContainer, null
        );
        
        // Track animasi dan task
        activeAnimations.put(explodingCell, spreadingWave);
        animatingTasks.put(explodingCell, task);
        
        // Callback ketika semua animasi selesai
        spreadingWave.setOnFinished(e -> {
            // Hapus dari tracking
            activeAnimations.remove(explodingCell);
            ExplosionQueue.ExplosionTask completedTask = animatingTasks.remove(explodingCell);
            
            // Tambahkan ke pending completed tasks untuk batch execution
            if (completedTask != null) {
                synchronized (pendingCompletedTasks) {
                    pendingCompletedTasks.add(completedTask);
                }
                
                // Restart timer untuk batch execution
                // Ini memungkinkan multiple tasks yang selesai bersamaan dikumpulkan
                batchExecutionTimer.stop();
                batchExecutionTimer.play();
            }
            
            // Cek apakah masih ada pending tasks yang perlu dianimasikan
            processAllPendingAnimations();
            
            // Cek completion setelah semua pending diproses
            checkAndNotifyCompletion();
        });
        
        // Pre-explosion pulse animation
        Timeline pulse = ExplosionAnimation.createPreExplosionPulse(cellView);
        pulse.setOnFinished(e -> {
            // Setelah pulse, mulai spreading wave animation
            spreadingWave.play();
        });
        
        // Mulai dengan pulse
        pulse.play();
    }
    
    /**
     * Memproses SEMUA pending tasks secara parallel.
     * Setiap cell yang critical akan langsung dianimasikan bersamaan.
     * Menggunakan Platform.runLater untuk memastikan semua koordinat dihitung setelah layout selesai.
     */
    public void processAllPendingAnimations() {
        // Ambil semua tasks yang ready (belum dianimasikan)
        List<ExplosionQueue.ExplosionTask> readyTasks = new ArrayList<>();
        
        // Loop untuk mengambil semua tasks yang ready
        ExplosionQueue.ExplosionTask task = explosionQueue.peekNext();
        while (task != null) {
            Cell cell = task.getCell();
            // Cek apakah cell ini belum sedang dianimasikan
            if (!activeAnimations.containsKey(cell)) {
                readyTasks.add(task);
                // Remove dari queue karena akan langsung diproses
                explosionQueue.removeTask(task);
            } else {
                // Jika sudah dianimasikan, skip task ini (akan diproses nanti)
                // Tapi jangan remove dari queue, biarkan tetap di queue
            }
            // Ambil task berikutnya
            task = explosionQueue.peekNext();
        }
        
        if (readyTasks.isEmpty()) {
            checkAndNotifyCompletion();
            return;
        }
        
        // Debug: Record timing
        AnimationDebugger.recordTiming("processAllPendingAnimations", System.currentTimeMillis());
        System.out.println(String.format(
            "[DEBUG] Processing %d ready tasks for animations",
            readyTasks.size()
        ));
        
        // Proses semua ready tasks dalam satu Platform.runLater
        // Ini memastikan semua koordinat dihitung pada waktu yang sama (setelah layout selesai)
        javafx.application.Platform.runLater(() -> {
            AnimationDebugger.recordTiming("Platform.runLater-execution", System.currentTimeMillis());
            
            // Step 1: Pre-calculate semua koordinat untuk semua target cells
            // JANGAN clear cache - biarkan cache tetap ada untuk konsistensi antar batch
            // Hanya update cache untuk cells yang belum ada atau perlu di-update
            System.out.println(String.format(
                "[DEBUG] Current cache size before pre-calculation: %d",
                ExplosionAnimation.getCacheSize()
            ));
            
            // Kumpulkan semua cells yang akan terlibat (exploding + targets)
            Set<Cell> allCells = new HashSet<>();
            Set<Cell> allTargetCells = new HashSet<>();
            
            for (ExplosionQueue.ExplosionTask readyTask : readyTasks) {
                Cell explodingCell = readyTask.getCell();
                allCells.add(explodingCell);
                
                // Kumpulkan semua target cells
                for (Cell neighbor : explodingCell.getNeighbors()) {
                    allCells.add(neighbor);
                    allTargetCells.add(neighbor);
                }
            }
            
            System.out.println(String.format(
                "[DEBUG] Pre-calculating/updating coordinates for %d cells (%d exploding, %d targets)",
                allCells.size(), readyTasks.size(), allTargetCells.size()
            ));
            
            // Pre-calculate atau update koordinat untuk SEMUA cells (exploding + targets)
            // Jika cell sudah di-cache, gunakan cache yang ada untuk konsistensi
            // Jika belum, hitung dan cache
            for (Cell cell : allCells) {
                Node cellView = cellViewMap.get(cell);
                if (cellView != null) {
                    // Pastikan userData di-set
                    if (cellView.getUserData() == null) {
                        cellView.setUserData(cell);
                    }
                    
                    // Pre-calculate atau update koordinat
                    // Method ini akan menggunakan cache jika sudah ada, atau menghitung baru jika belum
                    ExplosionAnimation.preCalculateCoordinate(cellView, animationContainer, cell);
                }
            }
            
            System.out.println(String.format(
                "[DEBUG] ✅ Pre-calculation complete. Cache size: %d cells",
                ExplosionAnimation.getCacheSize()
            ));
            
            // Step 2: Process animations (akan menggunakan cached coordinates)
            for (ExplosionQueue.ExplosionTask readyTask : readyTasks) {
                System.out.println(String.format(
                    "[DEBUG] Processing animation for Cell(%d,%d)",
                    readyTask.getCell().getX(), readyTask.getCell().getY()
                ));
                processAnimationForTask(readyTask);
            }
            
            // Cek apakah semua animasi selesai dan queue kosong
            checkAndNotifyCompletion();
            
            // Print problematic cells after processing
            AnimationDebugger.printProblematicCells();
        });
    }
    
    /**
     * Mengeksekusi semua completed tasks secara batch.
     * Mengumpulkan semua distribusi orb terlebih dahulu untuk mencegah race condition.
     */
    private void executeBatchCompletedTasks() {
        List<ExplosionQueue.ExplosionTask> tasksToExecute;
        
        synchronized (pendingCompletedTasks) {
            if (pendingCompletedTasks.isEmpty()) {
                return;
            }
            
            // Copy dan clear pending tasks
            tasksToExecute = new ArrayList<>(pendingCompletedTasks);
            pendingCompletedTasks.clear();
        }
        
        if (tasksToExecute.isEmpty()) {
            return;
        }
        
        // Step 1: Eksekusi semua explosions (kurangi orb di cell yang meledak)
        for (ExplosionQueue.ExplosionTask task : tasksToExecute) {
            int remainingOrbs = task.getCell().getOrbs() - task.getCell().getCriticalMass();
            task.getCell().setOrbs(remainingOrbs);
        }
        
        // Step 2: Kumpulkan semua distribusi orb ke setiap target cell
        // Map: Cell target -> (Map: Player -> jumlah orb)
        Map<Cell, Map<Player, Integer>> allDistributions = new HashMap<>();
        
        for (ExplosionQueue.ExplosionTask task : tasksToExecute) {
            Map<Cell, Integer> distributions = task.collectDistributions();
            
            for (Map.Entry<Cell, Integer> entry : distributions.entrySet()) {
                Cell target = entry.getKey();
                int orbCount = entry.getValue();
                
                if (!allDistributions.containsKey(target)) {
                    allDistributions.put(target, new HashMap<>());
                }
                
                Map<Player, Integer> playerOrbs = allDistributions.get(target);
                Player taskPlayer = task.getPlayer();
                
                // Kumpulkan orb berdasarkan player
                playerOrbs.put(taskPlayer, playerOrbs.getOrDefault(taskPlayer, 0) + orbCount);
            }
        }
        
        // Step 3: Execute semua distribusi sekaligus
        // Untuk setiap target cell, tambahkan semua orb sekaligus
        for (Map.Entry<Cell, Map<Player, Integer>> entry : allDistributions.entrySet()) {
            Cell target = entry.getKey();
            Map<Player, Integer> playerOrbs = entry.getValue();
            
            // Jika hanya satu player, gunakan addOrbs untuk batch
            if (playerOrbs.size() == 1) {
                Map.Entry<Player, Integer> singleEntry = playerOrbs.entrySet().iterator().next();
                target.addOrbs(singleEntry.getValue(), singleEntry.getKey(), tasksToExecute.get(0).getBoard());
            } else {
                // Jika multiple players, gunakan player dengan orb terbanyak
                Player dominantPlayer = null;
                int maxOrbs = 0;
                int totalOrbs = 0;
                
                for (Map.Entry<Player, Integer> playerEntry : playerOrbs.entrySet()) {
                    totalOrbs += playerEntry.getValue();
                    if (playerEntry.getValue() > maxOrbs) {
                        maxOrbs = playerEntry.getValue();
                        dominantPlayer = playerEntry.getKey();
                    }
                }
                
                // Tambahkan semua orb dengan player dominan
                if (dominantPlayer != null) {
                    target.addOrbs(totalOrbs, dominantPlayer, tasksToExecute.get(0).getBoard());
                }
            }
        }
    }
    
    /**
     * Cek apakah semua animasi selesai dan trigger callback jika perlu.
     */
    private void checkAndNotifyCompletion() {
        // Jika tidak ada animasi yang aktif, cek queue
        if (activeAnimations.isEmpty()) {
            // Clear cache hanya saat semua animasi benar-benar selesai
            // Ini memastikan cache tetap konsisten selama chain reaction
            ExplosionAnimation.clearCoordinateCache();
            System.out.println("[DEBUG] All animations complete - cleared coordinate cache");
            explosionQueue.notifyQueueEmpty();
        }
    }
    
    /**
     * Memulai monitoring queue dan memproses animasi.
     * Dipanggil setelah user action atau explosion trigger.
     * Akan memproses SEMUA pending explosions secara parallel.
     */
    public void startProcessing() {
        // Proses semua pending animations secara parallel
        processAllPendingAnimations();
    }
    
    /**
     * Membersihkan semua animasi yang sedang berjalan.
     */
    public void clear() {
        for (Animation anim : activeAnimations.values()) {
            anim.stop();
        }
        activeAnimations.clear();
        animationContainer.getChildren().clear();
    }
}

