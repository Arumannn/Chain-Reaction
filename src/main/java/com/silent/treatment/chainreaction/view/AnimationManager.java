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

        // [FIX] DELETED: Removed the code that overwrote the GameController's callback.
        // The GameController needs to own this callback to trigger nextTurn/elimination.
        
        explosionQueue.setOnTaskEnqueued(() -> {
            processAllPendingAnimations();
        });
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
                batchExecutionTimer.stop();
                batchExecutionTimer.play();
            }

            processAllPendingAnimations();
            checkAndNotifyCompletion();
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

            checkAndNotifyCompletion();
        });
    }

    private void executeBatchCompletedTasks() {
        List<ExplosionQueue.ExplosionTask> tasksToExecute;

        synchronized (pendingCompletedTasks) {
            if (pendingCompletedTasks.isEmpty()) {
                return;
            }
            tasksToExecute = new ArrayList<>(pendingCompletedTasks);
            pendingCompletedTasks.clear();
        }

        if (tasksToExecute.isEmpty()) {
            return;
        }

        for (ExplosionQueue.ExplosionTask task : tasksToExecute) {
            int remainingOrbs = task.getCell().getOrbs() - task.getCell().getCriticalMass();
            task.getCell().setOrbs(remainingOrbs);

            if (remainingOrbs >= task.getCell().getCriticalMass()) {
                explosionQueue.enqueueExplosion(task.getCell(), task.getBoard(), task.getPlayer());
            }
        }

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
    }

    private void checkAndNotifyCompletion() {
        if (activeAnimations.isEmpty()) {
            ExplosionAnimation.clearCoordinateCache();
            // Notifies the listener (GameController) that queue is empty
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