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

public class AnimationManager {

    private ExplosionQueue explosionQueue;
    private Pane animationContainer;
    private Map<Cell, Node> cellViewMap;

    private Map<Cell, Animation> activeAnimations;
    private Map<Cell, ExplosionQueue.ExplosionTask> animatingTasks;
    private List<ExplosionQueue.ExplosionTask> pendingCompletedTasks;
    private javafx.animation.Timeline batchExecutionTimer;

    public AnimationManager(Pane animationContainer, Map<Cell, Node> cellViewMap) {
        this.explosionQueue = ExplosionQueue.getInstance();
        this.animationContainer = animationContainer;
        this.cellViewMap = cellViewMap;
        this.activeAnimations = new HashMap<>();
        this.animatingTasks = new HashMap<>();
        this.pendingCompletedTasks = new ArrayList<>();

        this.batchExecutionTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(50), e -> executeBatchCompletedTasks()));
        this.batchExecutionTimer.setCycleCount(1);

        // Listener otomatis DIHAPUS agar Turn-Based aman.
        // Kita menggunakan pemicu manual di executeBatchCompletedTasks untuk Chain Reaction.
    }

    private void processAnimationForTask(ExplosionQueue.ExplosionTask task) {
        Cell explodingCell = task.getCell();
        Node cellView = cellViewMap.get(explodingCell);

        if (cellView == null) {
            task.execute();
            return;
        }

        if (activeAnimations.containsKey(explodingCell)) {
            return;
        }

        List<Node> neighborViews = new ArrayList<>();
        for (Cell neighbor : explodingCell.getNeighbors()) {
            Node neighborView = cellViewMap.get(neighbor);
            if (neighborView != null) {
                neighborViews.add(neighborView);
            }
        }

        if (neighborViews.isEmpty()) {
            task.execute();
            return;
        }

        Color playerColor = task.getPlayer().getColor();

        List<Cell> targetCells = new ArrayList<>();
        for (Cell neighbor : explodingCell.getNeighbors()) {
            targetCells.add(neighbor);
        }
        AnimationDebugger.recordExplosion(
                explodingCell, targetCells, "spreadingWave", System.currentTimeMillis());

        ParallelTransition spreadingWave = ExplosionAnimation.createSpreadingWaveAnimation(
                cellView, neighborViews, playerColor, animationContainer, null);

        activeAnimations.put(explodingCell, spreadingWave);
        animatingTasks.put(explodingCell, task);

        spreadingWave.setOnFinished(e -> {
            activeAnimations.remove(explodingCell);
            ExplosionQueue.ExplosionTask completedTask = animatingTasks.remove(explodingCell);

            if (completedTask != null) {
                synchronized (pendingCompletedTasks) {
                    pendingCompletedTasks.add(completedTask);
                }
                // Reset timer untuk mengumpulkan task yang selesai hampir bersamaan
                batchExecutionTimer.stop();
                batchExecutionTimer.play();
            } else {
                // Fallback jika task hilang (jarang terjadi)
                checkAndNotifyCompletion();
            }
        });

        Timeline pulse = ExplosionAnimation.createPreExplosionPulse(cellView);
        pulse.setOnFinished(e -> {
            spreadingWave.play();
        });

        pulse.play();
    }

    public void processAllPendingAnimations() {
        List<ExplosionQueue.ExplosionTask> readyTasks = new ArrayList<>();

        ExplosionQueue.ExplosionTask task = explosionQueue.peekNext();
        while (task != null) {
            Cell cell = task.getCell();
            // Hanya proses jika cell tersebut tidak sedang dianimasikan
            if (!activeAnimations.containsKey(cell)) {
                readyTasks.add(task);
                explosionQueue.removeTask(task);
            }
            task = explosionQueue.peekNext();
        }

        if (readyTasks.isEmpty()) {
            checkAndNotifyCompletion();
            return;
        }

        javafx.application.Platform.runLater(() -> {
            Set<Cell> allCells = new HashSet<>();
            for (ExplosionQueue.ExplosionTask readyTask : readyTasks) {
                Cell explodingCell = readyTask.getCell();
                allCells.add(explodingCell);
                for (Cell neighbor : explodingCell.getNeighbors()) {
                    allCells.add(neighbor);
                }
            }

            for (Cell cell : allCells) {
                Node cellView = cellViewMap.get(cell);
                if (cellView != null) {
                    if (cellView.getUserData() == null) {
                        cellView.setUserData(cell);
                    }
                    ExplosionAnimation.preCalculateCoordinate(cellView, animationContainer, cell);
                }
            }

            for (ExplosionQueue.ExplosionTask readyTask : readyTasks) {
                processAnimationForTask(readyTask);
            }
            
            // Note: Kita tidak panggil checkAndNotifyCompletion di sini
            // karena animasi baru saja dimulai (activeAnimations tidak kosong)
        });
    }

    private void executeBatchCompletedTasks() {
        List<ExplosionQueue.ExplosionTask> tasksToExecute;

        synchronized (pendingCompletedTasks) {
            if (pendingCompletedTasks.isEmpty()) {
                checkAndNotifyCompletion();
                return;
            }
            tasksToExecute = new ArrayList<>(pendingCompletedTasks);
            pendingCompletedTasks.clear();
        }

        if (tasksToExecute.isEmpty()) {
            checkAndNotifyCompletion();
            return;
        }

        // 1. Kurangi Orb di cell pusat
        for (ExplosionQueue.ExplosionTask task : tasksToExecute) {
            int remainingOrbs = task.getCell().getOrbs() - task.getCell().getCriticalMass();
            task.getCell().setOrbs(remainingOrbs);

            // Jika sisa orb masih overload (kasus langka orb sangat banyak), queue lagi
            if (remainingOrbs >= task.getCell().getCriticalMass()) {
                explosionQueue.enqueueExplosion(task.getCell(), task.getBoard(), task.getPlayer());
            }
        }

        // 2. Kumpulkan distribusi orb ke tetangga
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
                playerOrbs.put(taskPlayer, playerOrbs.getOrDefault(taskPlayer, 0) + orbCount);
            }
        }

        // 3. Eksekusi penambahan orb ke tetangga
        // Disinilah "addOrb" akan dipanggil, yang mungkin memicu enqueueExplosion baru!
        for (Map.Entry<Cell, Map<Player, Integer>> entry : allDistributions.entrySet()) {
            Cell target = entry.getKey();
            Map<Player, Integer> playerOrbs = entry.getValue();

            if (playerOrbs.size() == 1) {
                Map.Entry<Player, Integer> singleEntry = playerOrbs.entrySet().iterator().next();
                target.addOrbs(singleEntry.getValue(), singleEntry.getKey(), tasksToExecute.get(0).getBoard());
            } else {
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

                if (dominantPlayer != null) {
                    target.addOrbs(totalOrbs, dominantPlayer, tasksToExecute.get(0).getBoard());
                }
            }
        }

        // [FIX UTAMA] RE-TRIGGER ANIMASI
        // Cek apakah batch barusan menyebabkan ledakan baru di Queue?
        if (explosionQueue.peekNext() != null) {
            // Jika ADA ledakan baru, langsung proses animasinya!
            // Inilah yang membuat rantai ledakan berlanjut.
            processAllPendingAnimations();
        } else {
            // Jika TIDAK ADA ledakan baru, baru kita cek apakah game boleh lanjut
            checkAndNotifyCompletion();
        }
    }

    private void checkAndNotifyCompletion() {
        // Hanya notify selesai jika:
        // 1. Tidak ada animasi visual yang sedang jalan
        // 2. Tidak ada pending task yang menunggu dieksekusi
        // 3. Tidak ada task baru di queue
        boolean isReallyEmpty = activeAnimations.isEmpty() 
                             && pendingCompletedTasks.isEmpty()
                             && explosionQueue.peekNext() == null;

        if (isReallyEmpty) {
            ExplosionAnimation.clearCoordinateCache();
            // Beritahu GameController bahwa SEMUA rangkaian ledakan sudah selesai
            explosionQueue.notifyQueueEmpty();
        }
    }

    public void startProcessing() {
        processAllPendingAnimations();
    }

    public void clear() {
        for (Animation anim : activeAnimations.values()) {
            anim.stop();
        }
        activeAnimations.clear();
        animationContainer.getChildren().clear();
    }
}