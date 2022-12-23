package org.cis1200.othello;

import org.cis1200.othello.Agent.Agent;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class OthelloGame {
    private final int BOARD_X = 8;
    private final int BOARD_Y = 8;
    private ArrayList<ArrayList<CellState>> gameState;

    private Player turn = Player.BLACK;

    private int whiteCount = 2;
    private int blackCount = 2;

    private Agent whiteAgent;
    private Agent blackAgent;

    private ArrayList<Move> moveHistory = new ArrayList<>();

    private boolean inPlay = false;

    private final Runnable onUpdate;

    private Runnable onEnd = null;

    public OthelloGame(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        gameState = new ArrayList<>();
        for (int i = 0; i < BOARD_Y; i++) {
            ArrayList<CellState> row = new ArrayList<CellState>();
            for (int j = 0; j < BOARD_X; j++) {
                row.add(CellState.EMPTY);
            }
            gameState.add(row);
        }
        gameState.get(3).set(3, CellState.WHITE);
        gameState.get(4).set(4, CellState.WHITE);
        gameState.get(3).set(4, CellState.BLACK);
        gameState.get(4).set(3, CellState.BLACK);
    }

    public OthelloGame(Runnable onUpdate, String stringRept) {
        this.onUpdate = onUpdate;
        this.turn = stringRept.charAt(0) == 'W' ? Player.WHITE : Player.BLACK;
        this.whiteCount = 0;
        this.blackCount = 0;
        gameState = new ArrayList<>();
        System.out.println(stringRept);
        if (stringRept.length() < BOARD_Y * BOARD_X * 2 + 1) {
            throw new IllegalArgumentException("String representation of game state is too short");
        }
        for (int i = 0; i < BOARD_Y; i++) {
            ArrayList<CellState> row = new ArrayList<>();
            for (int j = 0; j < BOARD_X; j++) {
                switch (stringRept.charAt(2 + (i * BOARD_Y + j) * 2)) {
                    case 'W' -> {
                        row.add(CellState.WHITE);
                        whiteCount++;
                    }
                    case 'B' -> {
                        row.add(CellState.BLACK);
                        blackCount++;
                    }
                    case 'E' -> row.add(CellState.EMPTY);
                    default -> throw new IllegalArgumentException("Invalid string representation");
                }
            }
            gameState.add(row);
        }
    }

    public ArrayList<ArrayList<CellState>> getGameState() {
        return gameState;
    }

    public void setGameState(ArrayList<ArrayList<CellState>> gameState) {
        this.gameState = gameState;
    }

    public void setWhiteAgent(Agent whiteAgent) {
        this.whiteAgent = whiteAgent;
    }

    public void setBlackAgent(Agent blackAgent) {
        this.blackAgent = blackAgent;
    }

    public Agent getWhiteAgent() {
        return whiteAgent;
    }

    public Agent getBlackAgent() {
        return blackAgent;
    }

    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }

    public Player getTurn() {
        return turn;
    }

    public void setTurn(Player turn) {
        this.turn = turn;
    }

    public ArrayList<Move> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(ArrayList<Move> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    public int getBlackCount() {
        return blackCount;
    }

    public boolean getInPlay() {
        return inPlay;
    }

    public void start() {
        inPlay = true;
        // create a thread to run the game
        Thread gameLoopThrad = new Thread(() -> {
            synchronized (this) {
                if (!inPlay) {
                    return;
                }
                initiateTurn();
            }
        });
        gameLoopThrad.start();
    }

    public void initiateTurn() {
        // first get control of the game
        synchronized (this) {
            while (inPlay) {
                // provision for if the movie is submitted before the loop finishes(maybe on the
                // same thread)
                Player pl = Player.WHITE;
                if (turn == Player.WHITE) {
                    whiteAgent.getNextMove(this, Player.WHITE, (move) -> {
                        synchronized (this) {
                            if (!inPlay) {
                                return;
                            }
                            makeMove(move);
                            notify();
                        }
                    });
                } else {
                    pl = Player.BLACK;
                    blackAgent.getNextMove(this, Player.BLACK, (move) -> {
                        synchronized (this) {
                            if (!inPlay) {
                                return;
                            }
                            makeMove(move);
                            notify();
                        }
                    });
                }
                // The game might have already ended and the game hasn't started waiting yet.
                if (!inPlay) {
                    return;
                }
                if (turn != pl) {
                    // the player has already submitted a move
                    continue;
                }

                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Game thread interrupted");
                    return;
                }

            }
        }
    }

    private CellState getPlayerPiece(Player player) {
        if (player == Player.WHITE) {
            return CellState.WHITE;
        } else {
            return CellState.BLACK;
        }
    }

    private CellState getOpponentPiece(Player player) {
        if (player == Player.WHITE) {
            return CellState.BLACK;
        } else {
            return CellState.WHITE;
        }
    }

    private void incrementCount(CellState piece, int count) {
        if (piece == CellState.WHITE) {
            whiteCount += count;
        } else {
            blackCount += count;
        }
    }

    // returns null if the move is invalid
    public Move getMove(int x, int y, Player player) {
        if (gameState.get(y).get(x) != CellState.EMPTY) {
            return null;
        }

        CellState moverPiece = getPlayerPiece(player);
        CellState opponentPiece = getOpponentPiece(player);

        ArrayList<int[]> flips = new ArrayList<>();

        // search all directions
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int checkX = x + i;
                int checkY = y + j;

                ArrayList<int[]> provisionalFlips = new ArrayList<>();

                while (checkX < BOARD_X - abs(i) && checkX >= abs(i)
                        && checkY < BOARD_Y - abs(j) && checkY >= abs(j)
                        && gameState.get(checkY).get(checkX) == opponentPiece) {
                    provisionalFlips.add(new int[] { checkX, checkY });
                    checkX += i;
                    checkY += j;
                    if (gameState.get(checkY).get(checkX) == moverPiece) {
                        flips.addAll(provisionalFlips);
                        break;
                    }
                }
            }
        }

        if (flips.size() == 0) {
            return null;
        }

        return new Move(x, y, player, flips);
    }

    public ArrayList<Move> getValidMoves(Player player) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < BOARD_X; i++) {
            for (int j = 0; j < BOARD_Y; j++) {
                Move move = getMove(i, j, player);
                if (move != null) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    public void makeMove(Move move) {
        if (move.player() != turn) {
            System.out.print("Player: " + turn + " tried to make a move out of turn");
            return;
        }

        moveHistory.add(move);

        // place the piece
        gameState.get(move.y()).set(move.x(), getPlayerPiece(move.player()));
        incrementCount(getPlayerPiece(move.player()), 1);

        // do the flips
        for (int[] flip : move.flips()) {
            gameState.get(flip[1]).set(flip[0], getPlayerPiece(move.player()));
            incrementCount(getOpponentPiece(move.player()), -1);
            incrementCount(getPlayerPiece(move.player()), 1);
        }

        // change turn
        if (turn == Player.WHITE) {
            turn = Player.BLACK;
        } else {
            turn = Player.WHITE;
        }
        // this is creating a new thread
        onUpdate.run();
    }

    // this is a slow function made for the UI not the AI
    // returns -1 if game is still ongoing otherwise returns the number of white
    // pieces
    public int checkEnd() {
        // check if the current player has any availible moves
        if (getValidMoves(turn).size() == 0) {
            return whiteCount;
            // DISABLED
            // check if the other player has any availible moves

            // if(getValidMoves(turn==Player.WHITE? Player.BLACK:Player.WHITE).size()==0){
            // //game is over
            // return whiteCount;
            // } else {
            // //DISABLED
            // //skip the current player's turn
            // turn = turn==Player.WHITE? Player.BLACK:Player.WHITE;
            // }
        }
        return -1;
    }

    public void stop() {
        inPlay = false;
        new Thread(() -> {
            synchronized (this) {
                notifyAll();
            }
        }).start();
    }

    public void onGameEnd() {
        synchronized (this) {
            stop();
            if (this.onEnd != null) {
                this.onEnd.run();
            }
        }
    }

    // does not copy over agents
    // otherwise deepcopy
    public OthelloGame copyOverGameState(Runnable onUpdate) {
        OthelloGame copy = new OthelloGame(onUpdate);

        copy.whiteCount = whiteCount;
        copy.blackCount = blackCount;

        switch (turn) {
            case WHITE -> copy.turn = Player.WHITE;
            case BLACK -> copy.turn = Player.BLACK;
        }

        ArrayList<ArrayList<CellState>> newGameState = new ArrayList<ArrayList<CellState>>();
        for (int i = 0; i < BOARD_Y; i++) {
            ArrayList<CellState> row = new ArrayList<CellState>();
            for (int j = 0; j < BOARD_X; j++) {
                row.add(gameState.get(i).get(j).copy());
            }
            newGameState.add(row);
        }
        copy.setGameState(newGameState);
        return copy;
    }

    public OthelloGame undo() {
        if (moveHistory.size() == 0) {
            return null;
        }
        Move move = moveHistory.get(moveHistory.size() - 1);
        stop();
        OthelloGame copy = copyOverGameState(onUpdate);
        copy.setMoveHistory(new ArrayList<>(moveHistory.subList(0, moveHistory.size() - 1)));
        copy.turn = turn == Player.WHITE ? Player.BLACK : Player.WHITE;
        copy.gameState.get(move.y()).set(move.x(), CellState.EMPTY);
        copy.incrementCount(getPlayerPiece(move.player()), -1);
        for (int[] flip : move.flips()) {
            copy.gameState.get(flip[1]).set(flip[0], getOpponentPiece(move.player()));
            copy.incrementCount(getOpponentPiece(move.player()), 1);
            copy.incrementCount(getPlayerPiece(move.player()), -1);
        }
        copy.setWhiteAgent(whiteAgent);
        copy.setBlackAgent(blackAgent);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(turn == Player.WHITE ? "W" : "B");
        output.append(";");
        for (int i = 0; i < BOARD_Y; i++) {
            for (int j = 0; j < BOARD_X; j++) {
                switch (gameState.get(i).get(j)) {
                    case WHITE -> output.append("W");
                    case BLACK -> output.append("B");
                    case EMPTY -> output.append("E");
                }
                if (j != BOARD_X - 1) {
                    output.append(",");
                }
            }
            if (i != BOARD_Y - 1) {
                output.append(";");
            }
        }
        return output.toString();
    }

}
