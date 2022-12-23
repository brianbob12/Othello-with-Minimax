package org.cis1200.othello.Agent;

import org.cis1200.othello.OthelloGame;
import org.cis1200.othello.Move;
import org.cis1200.othello.Player;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class RandomAgent implements Agent {
    private final Random generator;

    private final int timeout;// ms

    public RandomAgent() {
        generator = new Random();
        timeout = 100;
    }

    public RandomAgent(int timeout) {
        generator = new Random();
        this.timeout = timeout;
    }

    public RandomAgent(int timeout, long seed) {
        generator = new Random(seed);
        this.timeout = timeout;
    }

    public void getNextMove(OthelloGame othelloGame, Player me, Consumer<Move> onReady) {
        ArrayList<Move> moves = othelloGame.getValidMoves(me);

        if (moves.size() == 0) {
            othelloGame.onGameEnd();
            return;
        }
        try {
            generator.nextInt(moves.size());
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal Argument Exception");
            System.out.println(moves.size());
            System.out.println(generator.nextInt(moves.size()));
            throw e;
        }
        final Move finalMove = moves.get(generator.nextInt(moves.size()));

        if (timeout == 0) {
            // don't commit the overhead of making a new thread
            onReady.accept(finalMove);
        } else {
            Thread myThread = new Thread(() -> {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onReady.accept(finalMove);
            });
            myThread.start();
        }
    }

    public void reset() {
        return;
    }
}
