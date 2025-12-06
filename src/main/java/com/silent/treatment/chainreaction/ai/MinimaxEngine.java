package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class MinimaxEngine {

    private static final Logger logger = Logger.getLogger(MinimaxEngine.class.getName());

    private final BoardEvaluator evaluator;
    private final int maxDepth;
    private final Random random;

    private static final double SCORE_THRESHOLD = 0.1;
    private static final double RANDOM_NOISE = 0.05;
    private static final double EXPLOSION_BONUS = 50.0;
    private static final double CAPTURE_BONUS = 30.0;

    private static final double ALPHA_INITIAL = Double.NEGATIVE_INFINITY;
    private static final double BETA_INITIAL = Double.POSITIVE_INFINITY;

    private final boolean useSimulation;

    public MinimaxEngine(BoardEvaluator evaluator, int depth) {
        this.evaluator = evaluator;
        this.maxDepth = depth;
        this.random = new Random();

        this.useSimulation = (depth >= 2);
    }

    public MinimaxEngine(int depth) {
        this.evaluator = new StandardEvaluator();
        this.maxDepth = depth;
        this.random = new Random();
        this.useSimulation = (depth >= 2);
    }

    public AIMove findBestMove(Board board, Player aiPlayer, Player enemyPlayer) {
        List<AIMove> moves = MoveGenerator.getLegalMoves(board, aiPlayer);

        if (moves.isEmpty())
            return null;

        if (useSimulation && maxDepth >= 2) {
            return findBestMoveWithMinimax(board, aiPlayer, enemyPlayer, moves);
        }

        return findBestMoveGreedy(board, aiPlayer, enemyPlayer, moves);
    }

    private AIMove findBestMoveWithMinimax(Board board, Player aiPlayer, Player enemyPlayer, List<AIMove> moves) {
        BoardSimulator.BoardState initialState = BoardSimulator.cloneBoard(board);

        List<AIMove> bestMoves = new ArrayList<>();
        double bestScore = ALPHA_INITIAL;

        for (AIMove move : moves) {

            BoardSimulator.BoardState newState = BoardSimulator.simulateMove(initialState, move, aiPlayer);

            double score = minimax(newState, maxDepth - 1, ALPHA_INITIAL, BETA_INITIAL, false, aiPlayer, enemyPlayer);

            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                bestMoves.add(move);
            }
        }

        return bestMoves.isEmpty() ? null : bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double minimax(BoardSimulator.BoardState state, int depth, double alpha, double beta,
            boolean isMaximizing, Player aiPlayer, Player enemyPlayer) {

        if (depth == 0) {
            return state.evaluate(aiPlayer);
        }

        Player currentPlayer = isMaximizing ? aiPlayer : enemyPlayer;
        List<AIMove> moves = generateMovesForState(state, currentPlayer);

        if (moves.isEmpty()) {
            return state.evaluate(aiPlayer);
        }

        if (isMaximizing) {

            double maxEval = ALPHA_INITIAL;

            for (AIMove move : moves) {
                BoardSimulator.BoardState newState = BoardSimulator.simulateMove(state, move, currentPlayer);
                double eval = minimax(newState, depth - 1, alpha, beta, false, aiPlayer, enemyPlayer);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;

        } else {

            double minEval = BETA_INITIAL;

            for (AIMove move : moves) {
                BoardSimulator.BoardState newState = BoardSimulator.simulateMove(state, move, currentPlayer);
                double eval = minimax(newState, depth - 1, alpha, beta, true, aiPlayer, enemyPlayer);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private List<AIMove> generateMovesForState(BoardSimulator.BoardState state, Player player) {
        List<AIMove> moves = new ArrayList<>();

        for (int x = 0; x < state.getWidth(); x++) {
            for (int y = 0; y < state.getHeight(); y++) {
                BoardSimulator.CellState cell = state.getCell(x, y);
                if (cell != null && (cell.owner == null || cell.owner.equals(player))) {
                    moves.add(new AIMove(x, y));
                }
            }
        }

        return moves;
    }

    private AIMove findBestMoveGreedy(Board board, Player aiPlayer, Player enemyPlayer, List<AIMove> moves) {
        List<AIMove> bestMoves = new ArrayList<>();
        double bestScore = ALPHA_INITIAL;

        for (AIMove move : moves) {
            double score = evaluateMovePotential(board, move, aiPlayer, enemyPlayer);
            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                bestMoves.add(move);
            }
        }

        return bestMoves.isEmpty() ? null : bestMoves.get(random.nextInt(bestMoves.size()));
    }

    /**
     * findBestMove untuk MULTIPLAYER (2-8 player) dengan DEPTH SEARCH.
     * 
     * Perbedaan berdasarkan maxDepth:
     * - Depth 1 (Easy): Greedy, hanya evaluasi immediate move
     * - Depth 2 (Medium): Simulasi 2 langkah, evaluasi counter-move musuh
     * - Depth 3+ (Hard): Simulasi 3+ langkah, strategic planning
     */
    public Cell findBestMove(GameManager gm, Player aiPlayer, List<Cell> validMoves) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }

        Board board = gm.getBoard();
        List<Player> enemies = getAllEnemies(gm, aiPlayer);

        if (enemies.isEmpty()) {
            return validMoves.isEmpty() ? null : validMoves.get(0);
        }

        // Jika depth 1, gunakan greedy (lebih cepat)
        if (maxDepth == 1) {
            return findBestMoveGreedyMultiplayer(board, aiPlayer, validMoves, enemies);
        }

        // Jika depth >= 2, gunakan minimax simulation
        return findBestMoveWithSimulation(board, aiPlayer, validMoves, enemies);
    }

    /**
     * GREEDY approach untuk depth 1 (Easy AI).
     * Evaluasi setiap move tanpa simulasi lanjutan.
     */
    private Cell findBestMoveGreedyMultiplayer(Board board, Player aiPlayer, List<Cell> validMoves,
            List<Player> enemies) {
        List<Cell> bestCells = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            AIMove move = new AIMove(cell.getX(), cell.getY());
            double score = evaluateMoveWithMultipleEnemies(board, move, aiPlayer, enemies);
            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestCells.clear();
                bestCells.add(cell);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                bestCells.add(cell);
            }
        }

        if (!bestCells.isEmpty()) {
            Cell chosen = bestCells.get(random.nextInt(bestCells.size()));
            logger.info(String.format("  ✓ Best move: (%d, %d) Score: %.1f",
                    chosen.getX(), chosen.getY(), bestScore));
            return chosen;
        }
        return null;
    }

    /**
     * SIMULATION approach untuk depth >= 2 (Medium/Hard AI).
     * Gunakan BoardSimulator untuk simulate moves dan evaluate hasil.
     */
    private Cell findBestMoveWithSimulation(Board board, Player aiPlayer, List<Cell> validMoves,
            List<Player> enemies) {
        BoardSimulator.BoardState initialState = BoardSimulator.cloneBoard(board);

        List<Cell> bestCells = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Cell cell : validMoves) {
            if (cell == null)
                continue;

            AIMove move = new AIMove(cell.getX(), cell.getY());

            // Simulate move
            BoardSimulator.BoardState newState = BoardSimulator.simulateMove(initialState, move, aiPlayer);

            // Evaluate dengan minimax (consider enemy responses)
            double score;
            if (maxDepth >= 2 && !enemies.isEmpty()) {
                // Simulate enemy response (worst case for us)
                score = evaluateStateWithEnemyResponse(newState, aiPlayer, enemies, maxDepth - 1);
            } else {
                // Fallback ke evaluasi langsung
                score = newState.evaluate(aiPlayer);
            }

            score += random.nextDouble() * RANDOM_NOISE;

            if (score > bestScore) {
                bestScore = score;
                bestCells.clear();
                bestCells.add(cell);
            } else if (Math.abs(score - bestScore) < SCORE_THRESHOLD) {
                bestCells.add(cell);
            }
        }

        if (!bestCells.isEmpty()) {
            Cell chosen = bestCells.get(random.nextInt(bestCells.size()));
            logger.info(String.format("  ✓ Best move after simulation: (%d, %d) Score: %.1f",
                    chosen.getX(), chosen.getY(), bestScore));
            return chosen;
        }
        return null;
    }

    /**
     * Evaluate state dengan mempertimbangkan response dari musuh.
     * Assume worst case: musuh akan pilih move terbaik mereka.
     */
    private double evaluateStateWithEnemyResponse(BoardSimulator.BoardState state, Player aiPlayer,
            List<Player> enemies, int depth) {
        if (depth <= 0 || enemies.isEmpty()) {
            return state.evaluate(aiPlayer);
        }

        // Untuk simplicity, simulate response dari musuh pertama (strongest)
        Player strongestEnemy = enemies.get(0);
        List<AIMove> enemyMoves = generateMovesForState(state, strongestEnemy);

        if (enemyMoves.isEmpty()) {
            return state.evaluate(aiPlayer);
        }

        // Assume enemy pilih move terbaik (worst for us = minimum score)
        double worstScore = Double.POSITIVE_INFINITY;

        for (AIMove enemyMove : enemyMoves) {
            BoardSimulator.BoardState afterEnemyMove = BoardSimulator.simulateMove(state, enemyMove, strongestEnemy);
            double score = afterEnemyMove.evaluate(aiPlayer);
            worstScore = Math.min(worstScore, score);

            // Alpha-beta style pruning untuk efisiensi
            if (worstScore < ALPHA_INITIAL + 100) {
                break;
            }
        }

        return worstScore;
    }

    private List<Player> getAllEnemies(GameManager gm, Player aiPlayer) {
        List<Player> enemies = new ArrayList<>();
        for (Player p : gm.getPlayers()) {
            if (!p.equals(aiPlayer) && p.isAlive()) {
                enemies.add(p);
            }
        }
        return enemies;
    }

    private double evaluateMoveWithMultipleEnemies(Board board, AIMove move, Player ai, List<Player> enemies) {
        Cell cell = board.getCell(move.getX(), move.getY());

        if (cell == null)
            return Double.NEGATIVE_INFINITY;

        double score = evaluator.evaluate(board, ai, enemies.get(0));

        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += EXPLOSION_BONUS;
            score += calculateCaptureBonus(cell, enemies);
        }

        int enemyThreats = countEnemyThreats(cell, enemies);
        score -= enemyThreats * 20.0;

        return score;
    }

    private double calculateCaptureBonus(Cell cell, List<Player> enemies) {
        double bonus = 0.0;
        for (Cell neighbor : cell.getNeighbors()) {
            if (isOwnedByEnemy(neighbor, enemies)) {
                bonus += CAPTURE_BONUS;
            }
        }
        return bonus;
    }

    private boolean isOwnedByEnemy(Cell cell, List<Player> enemies) {
        if (cell.getOwner() == null) {
            return false;
        }
        for (Player enemy : enemies) {
            if (cell.getOwner().equals(enemy)) {
                return true;
            }
        }
        return false;
    }

    private int countEnemyThreats(Cell cell, List<Player> enemies) {
        int threats = 0;
        for (Cell neighbor : cell.getNeighbors()) {
            if (isEnemyThreat(neighbor, enemies)) {
                threats++;
            }
        }
        return threats;
    }

    private boolean isEnemyThreat(Cell cell, List<Player> enemies) {
        if (cell.getOwner() == null) {
            return false;
        }
        if (cell.getOrbs() < cell.getCriticalMass() - 1) {
            return false;
        }
        return isOwnedByEnemy(cell, enemies);
    }

    private double evaluateMovePotential(Board board, AIMove move, Player ai, Player enemy) {

        Cell cell = board.getCell(move.getX(), move.getY());

        if (cell == null)
            return Double.NEGATIVE_INFINITY;

        double score = evaluator.evaluate(board, ai, enemy);

        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += EXPLOSION_BONUS;

            for (Cell n : cell.getNeighbors()) {
                if (n.getOwner() != null && n.getOwner().equals(enemy)) {
                    score += CAPTURE_BONUS;
                }
            }
        }

        return score;
    }
}