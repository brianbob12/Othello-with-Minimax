package org.cis1200.othello.Agent.Minimax.StateEvaluator;

import org.cis1200.othello.OthelloGame;

public interface StateEvaluator {
    public double evaluate(OthelloGame othelloGame);
}
