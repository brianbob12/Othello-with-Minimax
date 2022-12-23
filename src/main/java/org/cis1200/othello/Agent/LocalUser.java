package org.cis1200.othello.Agent;

import org.cis1200.othello.*;

import java.util.function.Consumer;

import java.awt.event.*;

public class LocalUser implements Agent {
    private final OthelloBoard board;

    private boolean isTurn = false;

    private Player me = null;
    private Consumer<Move> onReady = null;

    public LocalUser(OthelloBoard board) {
        this.board = board;

        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTurn) {
                    int x = e.getX() / OthelloBoard.CELL_SIZE;
                    int y = e.getY() / OthelloBoard.CELL_SIZE;
                    Move move = board.getGame().getMove(x, y, me);
                    if (move != null) {
                        isTurn = false;
                        onReady.accept(move);
                    }
                }
            }
        });
    }

    public void getNextMove(OthelloGame othelloGame, Player me, Consumer<Move> onReady) {
        isTurn = true;
        this.onReady = onReady;
        this.me = me;
    }

    public void reset() {
        isTurn = false;
        this.onReady = null;
    }
}
