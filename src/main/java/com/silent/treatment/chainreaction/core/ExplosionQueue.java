package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ExplosionQueue {
    private static final Logger logger = Logger.getLogger(ExplosionQueue.class.getName());

    private Queue<ExplosionTask> taskQueue;
    private boolean isProcessing;
    private ReentrantLock lock;
    private Runnable onQueueEmpty;
    private Runnable onTaskEnqueued;

    private ExplosionQueue() {
        this.taskQueue = new ArrayDeque<>();
        this.isProcessing = false;
        this.lock = new ReentrantLock();
    }

    // Bill Pugh Singleton pattern - thread-safe lazy initialization
    private static class SingletonHolder {
        private static final ExplosionQueue INSTANCE = new ExplosionQueue();
    }

    public static ExplosionQueue getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void enqueueExplosion(Cell cell, Board board, Player player) {
        lock.lock();
        try {
            logger.info(String.format("[QUEUE] + Enqueue Explosion di (%d,%d)", cell.getX(), cell.getY()));
            ExplosionTask task = new ExplosionTask(cell, board, player);
            taskQueue.offer(task);

            // Set flag processing menjadi TRUE saat ada item masuk
            this.isProcessing = true;
            logger.info("[QUEUE] Status isProcessing SET TO TRUE (Queue tidak kosong)");

            if (onTaskEnqueued != null) {
                onTaskEnqueued.run();
            }
        } finally {
            lock.unlock();
        }
    }

    public ExplosionTask peekNext() {
        lock.lock();
        try {
            return taskQueue.peek();
        } finally {
            lock.unlock();
        }
    }

    // Ini dipanggil saat animasi selesai atau manual check
    public void notifyQueueEmpty() {
        lock.lock();
        try {
            // Hanya set false jika queue benar-benar kosong
            if (taskQueue.isEmpty()) {
                logger.info("[QUEUE] Queue Kosong & NotifyQueueEmpty dipanggil.");
                logger.info("[QUEUE] Status isProcessing SET TO FALSE (Input Dibuka Kembali)");
                isProcessing = false;

                if (onQueueEmpty != null) {
                    onQueueEmpty.run();
                }
            } else {
                logger.info(
                        () -> "[QUEUE] NotifyQueueEmpty dipanggil TAPI queue masih ada isi (" + taskQueue.size()
                                + "). isProcessing tetap TRUE.");
            }
        } finally {
            lock.unlock();
        }
    }

    // Method baru untuk memaksa status processing (digunakan oleh AnimationManager
    // jika perlu)
    public void setProcessing(boolean processing) {
        lock.lock();
        try {
            logger.info(() -> "[QUEUE] Force Set isProcessing: " + processing);
            this.isProcessing = processing;
        } finally {
            lock.unlock();
        }
    }

    public boolean isProcessing() {
        lock.lock();
        try {
            // Processing true jika flag true ATAU queue masih ada isi
            return isProcessing || !taskQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            taskQueue.clear();
            isProcessing = false;
        } finally {
            lock.unlock();
        }
    }

    public void setOnQueueEmpty(Runnable callback) {
        this.onQueueEmpty = callback;
    }

    public void setOnTaskEnqueued(Runnable callback) {
        this.onTaskEnqueued = callback;
    }

    public void removeTask(ExplosionTask task) {
        lock.lock();
        try {
            taskQueue.remove(task);
        } finally {
            lock.unlock();
        }
    }

    public static class ExplosionTask {
        private final Cell cell;
        private final Board board;
        private final Player player;

        public ExplosionTask(Cell cell, Board board, Player player) {
            this.cell = cell;
            this.board = board;
            this.player = player;
        }

        public Cell getCell() {
            return cell;
        }

        public Board getBoard() {
            return board;
        }

        public Player getPlayer() {
            return player;
        }

        public Map<Cell, Integer> collectDistributions() {
            Map<Cell, Integer> distributions = new HashMap<>();
            for (Cell neighbor : cell.getNeighbors()) {
                distributions.put(neighbor, distributions.getOrDefault(neighbor, 0) + 1);
            }
            return distributions;
        }

        public void execute() {
            // Logic manual jika tidak pakai batch (tapi kita pakai batch di
            // AnimationManager)
        }
    }
}