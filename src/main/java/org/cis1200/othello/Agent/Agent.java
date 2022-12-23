package org.cis1200.othello.Agent;

import org.cis1200.othello.OthelloGame;
import org.cis1200.othello.Move;
import org.cis1200.othello.Player;

import java.util.function.Consumer;

public interface Agent {
    public void getNextMove(OthelloGame othelloGame, Player me, Consumer<Move> onReady);

    public void reset();
}
