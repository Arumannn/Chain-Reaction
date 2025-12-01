package com.silent.treatment.chainreaction.core;

import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Queue system untuk mengelola sequential explosions dalam Chain Reaction.
 * Memastikan ledakan terjadi satu per satu dengan delay untuk animasi.
 * 
 * Thread-safe dengan ReentrantLock untuk mencegah race conditions.
 */
public class ExplosionQueue {
    private static ExplosionQueue instance;
    
    private Queue<ExplosionTask> explosionQueue;
    private boolean isProcessing;
    private ReentrantLock lock;
    
    // Callback untuk notifikasi ketika queue selesai
    private Runnable onQueueEmpty;
    
    // Callback untuk notifikasi ketika task baru di-enqueue
    // Digunakan untuk langsung trigger animasi processing
    private Runnable onTaskEnqueued;
    
    private ExplosionQueue() {
        this.explosionQueue = new ArrayDeque<>();
        this.isProcessing = false;
        this.lock = new ReentrantLock();
    }
    
    public static ExplosionQueue getInstance() {
        if (instance == null) {
            instance = new ExplosionQueue();
        }
        return instance;
    }
    
    /**
     * Menambahkan task ledakan ke queue.
     * Task akan diproses secara parallel jika multiple explosions terjadi bersamaan.
     */
    public void enqueueExplosion(Cell cell, Board board, Player player) {
        lock.lock();
        try {
            ExplosionTask task = new ExplosionTask(cell, board, player);
            explosionQueue.offer(task);
            
            // Trigger callback untuk langsung memproses animasi
            // Ini memungkinkan multiple explosions berjalan bersamaan
            if (onTaskEnqueued != null) {
                onTaskEnqueued.run();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Mengambil task berikutnya dari queue untuk dianimasikan.
     * Returns null jika queue kosong.
     */
    public ExplosionTask peekNext() {
        lock.lock();
        try {
            return explosionQueue.peek();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Memproses task berikutnya dalam queue.
     * Dipanggil setelah animasi selesai atau saat queue baru diisi.
     */
    public void processNext() {
        lock.lock();
        try {
            if (explosionQueue.isEmpty()) {
                isProcessing = false;
                
                // Notifikasi bahwa semua explosions selesai
                if (onQueueEmpty != null) {
                    onQueueEmpty.run();
                }
                return;
            }
            
            isProcessing = true;
            // Task akan diambil dan dieksekusi oleh view layer setelah animasi
            // Kita hanya set flag bahwa ada task yang perlu diproses
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Menandai bahwa animasi untuk task saat ini selesai.
     * Eksekusi task dan lanjut ke berikutnya.
     * DEPRECATED: Gunakan executeBatch() untuk batch processing.
     */
    @Deprecated
    public void onAnimationComplete() {
        lock.lock();
        try {
            ExplosionTask task = explosionQueue.poll();
            if (task != null) {
                // Execute explosion logic setelah animasi selesai
                task.execute();
            }
            
            // Proses task berikutnya jika ada
            processNext();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Mengeksekusi semua pending tasks dengan batch processing.
     * Mengumpulkan semua distribusi orb terlebih dahulu, baru execute sekaligus.
     * Ini mencegah bug ketika multiple explosions menargetkan cell yang sama.
     * 
     * @param tasks List tasks yang akan dieksekusi
     */
    public void executeBatch(List<ExplosionTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        
        // Step 1: Eksekusi semua explosions (kurangi orb di cell yang meledak)
        for (ExplosionTask task : tasks) {
            int remainingOrbs = task.getCell().getOrbs() - task.getCell().getCriticalMass();
            task.getCell().setOrbs(remainingOrbs);
        }
        
        // Step 2: Kumpulkan semua distribusi orb ke setiap target cell
        // Map: Cell target -> (Map: Player -> jumlah orb)
        Map<Cell, Map<Player, Integer>> allDistributions = new HashMap<>();
        
        for (ExplosionTask task : tasks) {
            Map<Cell, Integer> distributions = task.collectDistributions();
            
            for (Map.Entry<Cell, Integer> entry : distributions.entrySet()) {
                Cell target = entry.getKey();
                int orbCount = entry.getValue();
                
                // Kumpulkan berdasarkan player (jika cell kosong atau milik player yang sama)
                if (!allDistributions.containsKey(target)) {
                    allDistributions.put(target, new HashMap<>());
                }
                
                Map<Player, Integer> playerOrbs = allDistributions.get(target);
                Player taskPlayer = task.getPlayer();
                
                // Jika cell kosong atau milik player yang sama, tambahkan ke player tersebut
                // Jika cell milik player berbeda, tetap tambahkan (akan di-overwrite owner)
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
                target.addOrbs(singleEntry.getValue(), singleEntry.getKey(), tasks.get(0).getBoard());
            } else {
                // Jika multiple players (tidak mungkin dalam game normal, tapi handle untuk safety)
                // Gunakan player dengan orb terbanyak, atau player pertama
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
                    target.addOrbs(totalOrbs, dominantPlayer, tasks.get(0).getBoard());
                }
            }
        }
    }
    
    /**
     * Mengecek apakah queue sedang memproses explosions.
     */
    public boolean isProcessing() {
        lock.lock();
        try {
            return isProcessing || !explosionQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Membersihkan queue (untuk reset game).
     */
    public void clear() {
        lock.lock();
        try {
            explosionQueue.clear();
            isProcessing = false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Set callback ketika queue kosong (semua explosions selesai).
     */
    public void setOnQueueEmpty(Runnable callback) {
        this.onQueueEmpty = callback;
    }
    
    /**
     * Set callback ketika task baru di-enqueue.
     * Digunakan untuk langsung trigger animasi processing.
     */
    public void setOnTaskEnqueued(Runnable callback) {
        this.onTaskEnqueued = callback;
    }
    
    /**
     * Menghapus task tertentu dari queue.
     * Digunakan ketika task langsung diproses tanpa menunggu.
     */
    public void removeTask(ExplosionTask task) {
        lock.lock();
        try {
            explosionQueue.remove(task);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Notifikasi bahwa queue kosong (untuk trigger callback).
     */
    public void notifyQueueEmpty() {
        lock.lock();
        try {
            if (explosionQueue.isEmpty() && !isProcessing) {
                if (onQueueEmpty != null) {
                    onQueueEmpty.run();
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Inner class untuk menyimpan data explosion task.
     * Public untuk akses dari view layer.
     */
    public static class ExplosionTask {
        private final Cell cell;
        private final Board board;
        private final Player player;
        
        public ExplosionTask(Cell cell, Board board, Player player) {
            this.cell = cell;
            this.board = board;
            this.player = player;
        }
        
        public Cell getCell() { return cell; }
        public Board getBoard() { return board; }
        public Player getPlayer() { return player; }
        
        public void execute() {
            // Eksekusi logika explosion
            int remainingOrbs = cell.getOrbs() - cell.getCriticalMass();
            cell.setOrbs(remainingOrbs);
            
            // Distribusi ke tetangga (akan di-queue juga jika perlu)
            for (Cell neighbor : cell.getNeighbors()) {
                neighbor.addOrb(player, board);
            }
        }
        
        /**
         * Mengumpulkan semua orb yang akan didistribusikan tanpa langsung menambahkannya.
         * Digunakan untuk batch processing ketika multiple explosions terjadi.
         * 
         * @return Map yang berisi cell target dan jumlah orb yang akan ditambahkan
         */
        public Map<Cell, Integer> collectDistributions() {
            Map<Cell, Integer> distributions = new HashMap<>();
            
            // Kumpulkan distribusi ke setiap neighbor
            for (Cell neighbor : cell.getNeighbors()) {
                distributions.put(neighbor, distributions.getOrDefault(neighbor, 0) + 1);
            }
            
            return distributions;
        }
    }
}

