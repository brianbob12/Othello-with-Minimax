package org.cis1200.othello.Agent.Minimax;

import org.cis1200.othello.Agent.Agent;
import org.cis1200.othello.Agent.Minimax.StateEvaluator.StateEvaluator;
import org.cis1200.othello.OthelloGame;
import org.cis1200.othello.Move;
import org.cis1200.othello.Player;

import java.util.HashMap;
import java.util.function.Consumer;

public class SimpleMiniMaxAgent implements Agent {

    private final int maxDepth;

    private final StateEvaluator evaluator;

    // maps game states to [minimaxValue, depth]
    private final HashMap<String, Double[]> cachedResults = new HashMap<>();

    // the maximum depth difference for which it allowed to use a cached result from
    // a lower depth.
    private int depthTolerance = 1;

    public SimpleMiniMaxAgent(int maxDepth, StateEvaluator evaluator) {
        this.maxDepth = maxDepth;
        this.evaluator = evaluator;
    }

    public int getDepthTolerance() {
        return depthTolerance;
    }

    public void setDepthTolerance(int depthTolerance) {
        this.depthTolerance = depthTolerance;
    }

    public void getNextMove(OthelloGame othelloGame, Player me, Consumer<Move> onReady) {
        Move[] moves = othelloGame.getValidMoves(me).toArray(new Move[0]);

        if (moves.length == 0) {
            othelloGame.onGameEnd();
            return;
        }

        Move bestMove = null;

        double bestValue = me == Player.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Move move : moves) {
            OthelloGame newOthelloGame = othelloGame.copyOverGameState(() -> {
            });
            newOthelloGame.makeMove(move);
            double value = minimax(
                    newOthelloGame, me == Player.BLACK, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, maxDepth - 1
            );
            if (me == Player.WHITE && value > bestValue) {
                bestValue = value;
                bestMove = move;
            } else if (me == Player.BLACK && value < bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        onReady.accept(bestMove);
    }

    public void reset() {
        cachedResults.clear();
    }

    // white is trying to maximize scores and black is trying to minimize them
    private double minimax(
            OthelloGame othelloGame, boolean maximizing, double alpha, double beta, int depth
    ) {

        // assume the turn in game is correct
        // check if the result has been sufficiently checked already
        if (cachedResults.containsKey(othelloGame.toString())) {
            Double[] cachedResult = cachedResults.get(othelloGame.toString());
            if (cachedResult[1] >= depth - depthTolerance) {
                return cachedResult[0];
            }
        }

        Player nextToPlay = maximizing ? Player.WHITE : Player.BLACK;

        double score = 0;

        final Move[] moves = othelloGame.getValidMoves(nextToPlay).toArray(new Move[0]);

        if (moves.length == 0) {
            if (othelloGame.getWhiteCount() == othelloGame.getBlackCount()) {
                score = 0f;
            } else {
                score = ((othelloGame.getWhiteCount() > othelloGame.getBlackCount()) ? 100f
                        : -100f);
            }
        } else if (depth == 0) {
            score = evaluator.evaluate(othelloGame);
        } else {

            // recurse over children
            double bestScore = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            for (Move move : moves) {
                OthelloGame othelloGameCopy = othelloGame.copyOverGameState(() -> {
                });
                othelloGameCopy.makeMove(move);
                double moveScore = minimax(othelloGameCopy, !maximizing, alpha, beta, depth - 1);
                if (maximizing) {
                    bestScore = Math.max(bestScore, moveScore);
                    alpha = Math.max(alpha, bestScore);
                    if (bestScore >= beta) {
                        break;
                    }
                } else {
                    bestScore = Math.min(bestScore, moveScore);
                    beta = Math.min(beta, bestScore);
                    if (bestScore <= alpha) {
                        break;
                    }
                }
            }

            score = bestScore;
        }
        // cache the result
        cachedResults.put(othelloGame.toString(), new Double[] { score, (double) depth });
        return score;

    }

}
