package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MinimaxEngine {

    private final BoardEvaluator evaluator;
    private final int maxDepth;
    private final Random random;

    // Constants untuk magic numbers
    private static final double SCORE_THRESHOLD = 0.1; // Threshold untuk tie detection
    private static final double RANDOM_NOISE = 0.05; // Random noise untuk variasi
    private static final double EXPLOSION_BONUS = 50.0;
    private static final double CAPTURE_BONUS = 30.0;

    public MinimaxEngine(BoardEvaluator evaluator, int depth) {
        this.evaluator = evaluator;
        this.maxDepth = depth;
        this.random = new Random();
    }

    public MinimaxEngine(int depth) {
        this.evaluator = new StandardEvaluator();
        this.maxDepth = depth;
        this.random = new Random();
    }

    public AIMove findBestMove(Board board, Player aiPlayer, Player enemyPlayer) {
        List<AIMove> moves = MoveGenerator.getLegalMoves(board, aiPlayer);

        if (moves.isEmpty())
            return null;

        // Collect all best moves (untuk handle tie)
        List<AIMove> bestMoves = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (AIMove move : moves) {
            // Evaluasi move dengan look-ahead based on depth
            double score = evaluateMoveWithDepth(board, move, aiPlayer, enemyPlayer, maxDepth, true);

            // Tambah random noise kecil untuk break deterministic pattern
            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                // Score hampir sama, collect sebagai kandidat
                bestMoves.add(move);
            }
        }

        // Pilih random dari best moves
        return bestMoves.isEmpty() ? null : bestMoves.get(random.nextInt(bestMoves.size()));
    }

    /**
     * Evaluasi move dengan recursive depth-limited search.
     * Simplified minimax tanpa actual simulation (untuk performance).
     */
    private double evaluateMoveWithDepth(Board board, AIMove move, Player ai, Player enemy,
            int depth, boolean isMaximizing) {
        // Base case: depth 0 atau invalid
        if (depth == 0) {
            return evaluateMovePotential(board, move, ai, enemy);
        }

        // Untuk depth > 0, evaluasi lebih dalam dengan bonus/penalty
        double baseScore = evaluateMovePotential(board, move, ai, enemy);

        // Apply depth multiplier (deeper moves are less certain)
        double depthFactor = 1.0 / (1.0 + depth * 0.2);

        return baseScore * depthFactor;
    }

    /**
     * Overload untuk GameManager dan List<Cell> validMoves.
     * Digunakan oleh AIMedium dan AIHard.
     * Support multi-player dengan evaluasi semua musuh.
     */
    public Cell findBestMove(GameManager gm, Player aiPlayer, List<Cell> validMoves) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }

        Board board = gm.getBoard();
        List<Player> enemies = getAllEnemies(gm, aiPlayer);

        // Safety check: harus ada musuh
        if (enemies.isEmpty()) {
            return validMoves.isEmpty() ? null : validMoves.get(0);
        }

        Player primaryEnemy = enemies.get(0); // Primary enemy untuk evaluasi

        // Collect all best cells (untuk handle tie)
        List<Cell> bestCells = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            // Convert Cell to AIMove
            AIMove move = new AIMove(cell.getX(), cell.getY());

            // Evaluate potential dengan multi-enemy consideration
            double score = evaluateMoveWithMultipleEnemies(board, move, aiPlayer, enemies);

            // Tambah random noise kecil untuk break deterministic pattern
            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestCells.clear();
                bestCells.add(cell);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                // Score hampir sama, collect sebagai kandidat
                bestCells.add(cell);
            }
        }

        // Pilih random dari best cells
        return bestCells.isEmpty() ? null : bestCells.get(random.nextInt(bestCells.size()));
    }

    /**
     * Get first alive enemy (untuk backward compatibility).
     */
    private Player getFirstEnemy(GameManager gm, Player aiPlayer) {
        for (Player p : gm.getPlayers()) {
            if (!p.equals(aiPlayer) && p.isAlive()) {
                return p;
            }
        }
        return null;
    }

    /**
     * Get all alive enemies (untuk multi-player support).
     */
    private List<Player> getAllEnemies(GameManager gm, Player aiPlayer) {
        List<Player> enemies = new ArrayList<>();
        for (Player p : gm.getPlayers()) {
            if (!p.equals(aiPlayer) && p.isAlive()) {
                enemies.add(p);
            }
        }
        return enemies;
    }

    /**
     * Evaluasi move dengan mempertimbangkan multiple enemies.
     */
    private double evaluateMoveWithMultipleEnemies(Board board, AIMove move, Player ai, List<Player> enemies) {
        Cell cell = board.getCell(move.x, move.y);

        if (cell == null)
            return Double.NEGATIVE_INFINITY;

        // Base score dari evaluator (dengan primary enemy)
        double score = evaluator.evaluate(board, ai, enemies.get(0));

        // Bonus jika cell hampir critical
        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += EXPLOSION_BONUS;

            // Check capture bonus untuk SEMUA musuh
            for (Cell neighbor : cell.getNeighbors()) {
                if (neighbor.getOwner() != null) {
                    for (Player enemy : enemies) {
                        if (neighbor.getOwner().equals(enemy)) {
                            score += CAPTURE_BONUS;
                            break; // Hanya hitung sekali per neighbor
                        }
                    }
                }
            }
        }

        // Penalty jika ada multiple enemies yang kuat di sekitar
        int enemyThreats = 0;
        for (Cell neighbor : cell.getNeighbors()) {
            if (neighbor.getOwner() != null) {
                for (Player enemy : enemies) {
                    if (neighbor.getOwner().equals(enemy) &&
                            neighbor.getOrbs() >= neighbor.getCriticalMass() - 1) {
                        enemyThreats++;
                    }
                }
            }
        }
        score -= enemyThreats * 20.0; // Penalty untuk ancaman

        return score;
    }

    private double evaluateMovePotential(Board board, AIMove move, Player ai, Player enemy) {
        // 1. Dapatkan Cell target
        Cell cell = board.getCell(move.x, move.y);

        // Safety check: Skip null cells
        if (cell == null)
            return Double.NEGATIVE_INFINITY;

        // 2. Nilai posisi papan saat ini (Dasar)
        double score = evaluator.evaluate(board, ai, enemy);

        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += EXPLOSION_BONUS; // Bonus Ledakan

            // Cek apakah ledakan ini berpotensi mengenai musuh (Capture bonus)
            for (Cell n : cell.getNeighbors()) {
                if (n.getOwner() != null && n.getOwner().equals(enemy)) {
                    score += CAPTURE_BONUS; // Capture bonus
                }
            }
        }

        return score;
    }
}