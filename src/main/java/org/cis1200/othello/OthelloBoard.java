package org.cis1200.othello;

import org.cis1200.othello.Agent.*;
import org.cis1200.othello.Agent.Minimax.StateEvaluator.MCTS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.HashMap;

public class OthelloBoard extends JPanel {

    public static final int CELL_SIZE = 70;

    public static final int BOARD_X = 8;
    public static final int BOARD_Y = 8;

    public static final int SIZE_X = BOARD_X * CELL_SIZE;

    public static final int SIZE_Y = BOARD_Y * CELL_SIZE;

    public static final int LINE_WIDTH = 2;

    private static final Color BOARD_COLOR = new Color(72, 143, 31);
    private static final Color WHITE_MOVES_HIGHLIGHT = new Color(255, 0, 0, 100);
    private static final Color BLACK_MOVES_HIGHLIGHT = new Color(0, 0, 255, 100);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 100);
    private static final Color FLIP_INDICATOR_WHITE = new Color(255, 0, 0, 100);
    private static final Color FLIP_INDICATOR_BLACK = new Color(0, 0, 255, 100);

    // this is a diameter
    public static final int PIECE_SIZE = CELL_SIZE - 10;

    private static final SoundEffect soundEffect = new SoundEffect();

    private OthelloGame othelloGame;

    private HashMap<String, Move> validMoves;

    private Move hoveringOnMove = null;

    private int lastWhiteCount = 2;
    private int lastBlackCount = 2;

    private JLabel infoLabel1 = null;
    private JLabel infoLabel2 = null;
    private JLabel infoLabel3 = null;

    public OthelloBoard() {
        othelloGame = new OthelloGame(this::onUpdatedGameState);
        othelloGame.setWhiteAgent(new LocalUser(this));
        othelloGame.setBlackAgent(new RandomAgent());
        onUpdatedGameState();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!othelloGame.getInPlay()) {
                    return;
                }
                int x = e.getX() / CELL_SIZE;
                int y = e.getY() / CELL_SIZE;
                if (validMoves.containsKey(x + "," + y)) {
                    if (hoveringOnMove == validMoves.get(x + "," + y)) {
                        return;
                    }
                    hoveringOnMove = validMoves.get(x + "," + y);
                    repaint();
                } else {
                    if (hoveringOnMove == null) {
                        return;
                    }
                    hoveringOnMove = null;
                    repaint();
                }
            }
        });
    }

    public void start() {
        othelloGame.start();
        onUpdatedGameState();
    }

    public void stop() {
        othelloGame.stop();
        onUpdatedGameState();
    }

    public void reset() {
        // stop old game
        othelloGame.stop();

        // pull old agents
        Agent whiteAgent = othelloGame.getWhiteAgent();
        whiteAgent.reset();
        Agent blackAgent = othelloGame.getBlackAgent();
        blackAgent.reset();

        // setup new game
        othelloGame = new OthelloGame(this::onUpdatedGameState);
        othelloGame.setWhiteAgent(whiteAgent);
        othelloGame.setBlackAgent(blackAgent);

        lastBlackCount = 2;
        lastWhiteCount = 2;

        onUpdatedGameState();
    }

    public OthelloGame getGame() {
        return othelloGame;
    }

    public void setGame(OthelloGame othelloGame) {
        this.othelloGame = othelloGame;
        onUpdatedGameState();
    }

    public void setInfoLabel1(JLabel label) {
        this.infoLabel1 = label;
    }

    public JLabel getInfoLabel1() {
        return infoLabel1;
    }

    public void setInfoLabel2(JLabel label) {
        this.infoLabel2 = label;
    }

    public JLabel getInfoLabel2() {
        return infoLabel2;
    }

    public void setInfoLabel3(JLabel label) {
        this.infoLabel3 = label;
    }

    public JLabel getInfoLabel3() {
        return infoLabel3;
    }

    private final MCTS mcts = new MCTS(100);

    void onUpdatedGameState() {
        // change in white count => change in black count
        if (othelloGame.getInPlay() && (othelloGame.getWhiteCount() != lastWhiteCount)) {
            soundEffect.playClick();

            if (infoLabel1 != null) {
                infoLabel1.setText(
                        othelloGame.getTurn() == Player.WHITE ? "White's Turn" : "Black's Turn"
                );
            }

            if (infoLabel2 != null) {
                infoLabel2.setText(
                        "Piece count - White: " + othelloGame.getWhiteCount() + " Black: "
                                + othelloGame.getBlackCount()
                );
            }

            if (infoLabel3 != null) {
                double gameStateValue = mcts.evaluate(othelloGame);
                String message = "draw is likely";
                if (gameStateValue > 70) {
                    message = "white is very likely to win";
                } else if (gameStateValue > 50) {
                    message = "white is likely to win";
                } else if (gameStateValue > 30) {
                    message = "white seems slightly ahead";
                } else if (gameStateValue < -70) {
                    message = "black is very likely to win";
                } else if (gameStateValue < -50) {
                    message = "black is likely to win";
                } else if (gameStateValue < -30) {
                    message = "black seems slightly ahead";
                }

                infoLabel3.setText(message + " | MCTS: " + gameStateValue);
            }

        }

        // check win
        if (othelloGame.checkEnd() != -1) {
            System.out.println("Game Over");
            if (othelloGame.getWhiteCount() > othelloGame.getBlackCount()) {
                if (infoLabel1 != null) {
                    infoLabel1.setText("White Wins!");
                }
                System.out.println("White Wins");
            } else if (othelloGame.getWhiteCount() < othelloGame.getBlackCount()) {
                if (infoLabel1 != null) {
                    infoLabel1.setText("Black Wins!");
                }
                System.out.println("Black Wins");
            } else {
                if (infoLabel1 != null) {
                    infoLabel1.setText("Draw!");
                }
                System.out.println("Tie");
            }
            soundEffect.playWin();
        }

        lastWhiteCount = othelloGame.getWhiteCount();
        lastBlackCount = othelloGame.getBlackCount();

        Collection<Move> potentialMoves = othelloGame.getValidMoves(othelloGame.getTurn());
        validMoves = new HashMap<>();
        hoveringOnMove = null;
        potentialMoves.forEach(move -> validMoves.put(move.x() + "," + move.y(), move));

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background
        g.setColor(BOARD_COLOR);
        g.fillRect(0, 0, SIZE_X, SIZE_Y);

        // draw lines
        g.setColor(Color.black);

        for (int i = 0; i < BOARD_X + 1; i++) {
            g.drawLine(0, i * CELL_SIZE, SIZE_X, i * CELL_SIZE);
        }
        for (int i = 0; i < BOARD_Y + 1; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, SIZE_Y);
        }

        // draw pieces
        int pieceXBuffer = (CELL_SIZE - PIECE_SIZE) / 2;
        int pieceYBuffer = (CELL_SIZE - PIECE_SIZE) / 2;

        for (int i = 0; i < BOARD_X; i++) {
            for (int j = 0; j < BOARD_Y; j++) {
                CellState state = othelloGame.getGameState().get(j).get(i);
                switch (state) {
                    case EMPTY -> {
                        // draw hovering
                        if (!othelloGame.getInPlay() || !validMoves.containsKey(i + "," + j)) {
                            break;
                        }
                        if (hoveringOnMove != null && hoveringOnMove.x() == i
                                && hoveringOnMove.y() == j) {
                            g.setColor(HIGHLIGHT_COLOR);
                        } else {
                            g.setColor(
                                    othelloGame.getTurn() == Player.WHITE ? WHITE_MOVES_HIGHLIGHT
                                            : BLACK_MOVES_HIGHLIGHT
                            );
                        }
                        g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                    case BLACK -> {
                        g.setColor(Color.black);
                        g.fillOval(
                                i * CELL_SIZE + pieceXBuffer, j * CELL_SIZE + pieceYBuffer,
                                PIECE_SIZE, PIECE_SIZE
                        );
                        g.setColor(Color.black);
                        g.drawOval(
                                i * CELL_SIZE + pieceXBuffer, j * CELL_SIZE + pieceYBuffer,
                                PIECE_SIZE, PIECE_SIZE
                        );
                    }
                    case WHITE -> {
                        g.setColor(Color.white);
                        g.fillOval(
                                i * CELL_SIZE + pieceXBuffer, j * CELL_SIZE + pieceYBuffer,
                                PIECE_SIZE, PIECE_SIZE
                        );
                        g.setColor(Color.black);
                        g.drawOval(
                                i * CELL_SIZE + pieceXBuffer, j * CELL_SIZE + pieceYBuffer,
                                PIECE_SIZE, PIECE_SIZE
                        );
                    }
                    default -> {
                    }
                }
            }
        }

        // draw pieces that will flip
        if (hoveringOnMove != null) {
            hoveringOnMove.flips().forEach(location -> {
                g.setColor(
                        othelloGame.getTurn() == Player.WHITE ? FLIP_INDICATOR_WHITE
                                : FLIP_INDICATOR_BLACK
                );
                g.fillOval(
                        location[0] * CELL_SIZE + pieceXBuffer,
                        location[1] * CELL_SIZE + pieceYBuffer, PIECE_SIZE, PIECE_SIZE
                );
            });
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZE_X + 1, SIZE_Y + 1);
    }

}
