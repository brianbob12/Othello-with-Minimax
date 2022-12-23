package org.cis1200.othello.Agent.Minimax.StateEvaluator;

import org.cis1200.othello.OthelloGame;

public class CountPieces implements StateEvaluator {

    public double evaluate(OthelloGame othelloGame) {
        double multiplier = 100f / 64f;
        return (multiplier * (othelloGame.getWhiteCount() - othelloGame.getBlackCount()));
    }
}
