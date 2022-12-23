package org.cis1200.othello.Agent.Minimax.StateEvaluator;

import org.cis1200.othello.Agent.RandomAgent;
import org.cis1200.othello.OthelloGame;

// Monte Carlo Tree Search
public class MCTS implements StateEvaluator {

    public final int simulationCount;

    private double total;
    private int threadsFinished;

    private final RandomAgent randomAgent = new RandomAgent(0);

    public MCTS(int simulationCount) {
        this.simulationCount = simulationCount;
    }

    // every game where white wins gets 100 points
    // every game where there is a draw gets 0 points
    // every game where black wins gets -100 points
    public double evaluate(OthelloGame othelloGame) {

        total = 0;
        threadsFinished = 0;

        for (int i = 0; i < simulationCount; i++) {

            OthelloGame newOthelloGame = othelloGame.copyOverGameState(() -> {
            });
            newOthelloGame.setWhiteAgent(randomAgent);
            newOthelloGame.setBlackAgent(randomAgent);

            newOthelloGame.setOnEnd(() -> {
                double toadd = 0;
                if (newOthelloGame.getWhiteCount() != newOthelloGame.getBlackCount()) {
                    toadd = ((newOthelloGame.getWhiteCount() > newOthelloGame.getBlackCount()) ? 100
                            : -100);
                }
                synchronized (this) {
                    total += toadd;
                    threadsFinished++;
                    notify();
                }
            });

            newOthelloGame.start();
        }
        // wait for all threads to finish
        synchronized (this) {
            while (threadsFinished < simulationCount) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return total / (double) simulationCount;
        }
    }
}
