package com.silent.treatment.chainreaction.ai;

import com.silent.treatment.chainreaction.core.GameManager;
import com.silent.treatment.chainreaction.model.Board;
import com.silent.treatment.chainreaction.model.Cell;
import com.silent.treatment.chainreaction.model.Player;
import java.util.List;
import java.util.ArrayList;

import java.util.Random;

public class MinimaxEngine {

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
            return state.evaluate(aiPlayer, enemyPlayer);
        }

        Player currentPlayer = isMaximizing ? aiPlayer : enemyPlayer;
        List<AIMove> moves = generateMovesForState(state, currentPlayer);

        if (moves.isEmpty()) {
            return state.evaluate(aiPlayer, enemyPlayer);
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

    public Cell findBestMove(GameManager gm, Player aiPlayer, List<Cell> validMoves) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }

        Board board = gm.getBoard();
        List<Player> enemies = getAllEnemies(gm, aiPlayer);

        if (enemies.isEmpty()) {
            return validMoves.isEmpty() ? null : validMoves.get(0);
        }
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

        return bestCells.isEmpty() ? null : bestCells.get(random.nextInt(bestCells.size()));
    }

    private Player getFirstEnemy(GameManager gm, Player aiPlayer) {
        for (Player p : gm.getPlayers()) {
            if (!p.equals(aiPlayer) && p.isAlive()) {
                return p;
            }
        }
        return null;
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
        Cell cell = board.getCell(move.x, move.y);

        if (cell == null)
            return Double.NEGATIVE_INFINITY;

        double score = evaluator.evaluate(board, ai, enemies.get(0));

        if (cell.getOrbs() == cell.getCriticalMass() - 1) {
            score += EXPLOSION_BONUS;

            for (Cell neighbor : cell.getNeighbors()) {
                if (neighbor.getOwner() != null) {
                    for (Player enemy : enemies) {
                        if (neighbor.getOwner().equals(enemy)) {
                            score += CAPTURE_BONUS;
                            break;
                        }
                    }
                }
            }
        }

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
        score -= enemyThreats * 20.0;

        return score;
    }

    private double evaluateMovePotential(Board board, AIMove move, Player ai, Player enemy) {

        Cell cell = board.getCell(move.x, move.y);

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