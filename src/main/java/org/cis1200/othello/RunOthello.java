package org.cis1200.othello;

import org.cis1200.othello.Agent.LocalUser;
import org.cis1200.othello.Agent.Minimax.SimpleMiniMaxAgent;
import org.cis1200.othello.Agent.Minimax.StateEvaluator.MCTS;
import org.cis1200.othello.Agent.RandomAgent;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class RunOthello implements Runnable {

    final String INFO = """
            Welcome to Othello!

            This is a two player othelloGame. The goal is to have more pieces on the board than your opponent at the end of the othelloGame.

            Bellow are the rules of the othelloGame (from https://www.worldothello.org/about/about-othello/othello-rules/official-rules/english):

             1. Black always moves first.

             2. Players may not skip over their own colour disk(s) to outflank an opposing disk.

             3. Disk(s) may only be outflanked as a direct result of a move and must fall in the direct line of the disk placed down.

             4. All disks outflanked in any one move must be flipped, even if it is to the player's advantage not to flip them at all.\s

             5. Once a disk is placed on a square, it can never be moved to another square later in the othelloGame.\s

             6. When it is no longer possible for either player to move, the othelloGame is over. Disks are counted and the player with the majority of their colour showing is the winner.

             UI Controls:
                - Click on a square to place a piece there.
                - Click on the "Rest" button to start a new othelloGame.
                - Click on the "Undo" button to undo the last move.
                - Click on the "Save" button to save the current othelloGame state.
                - Click on the "Load" button to load a saved othelloGame state. If none is saved nothing will happen.
                - Click on the "Start"/"Stop" button to start/stop the othelloGame. This is useful if you want to stop the AI thinking.
                - Use the first drop down menu to select the white player.
                - Use the second drop down menu to select the black player.

            Agents to choose from:
              - Local User: A human player. It's ok to have 2 of these to play against someone else on the same computer.
              - Random Agent: A random agent that makes random moves.
              - SmartFast: A minimax agent with access to a few resources so that it plays quickly.
              - SmartSlow: A minimax agent with access to a lot of resources so that it plays wells.
             """;

    public void run() {
        final JFrame frame = new JFrame("LEVEL FRAME");

        frame.setLocation(300, 300);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel status_panel = new JPanel(new BorderLayout());

        frame.add(status_panel, BorderLayout.SOUTH);

        final JLabel status1 = new JLabel("Game Not Started");
        final JLabel status2 = new JLabel("-");
        final JLabel status3 = new JLabel("-");

        status_panel.add(status1, BorderLayout.NORTH);
        status_panel.add(status2, BorderLayout.CENTER);
        status_panel.add(status3, BorderLayout.SOUTH);

        final OthelloBoard board = new OthelloBoard();

        board.setInfoLabel1(status1);
        board.setInfoLabel2(status2);
        board.setInfoLabel3(status3);

        frame.add(board, BorderLayout.CENTER);

        final JPanel control_panel = new JPanel();
        frame.add(control_panel, BorderLayout.NORTH);

        final JButton start = new JButton("Start");

        final String[] playerOptions = { "Human", "Random", "SmartFast", "SmartSlow" };
        final JComboBox<String> player1 = new JComboBox<String>(playerOptions);
        player1.setSelectedIndex(0);
        player1.addActionListener((e) -> {
            if ("Human".equals(player1.getSelectedItem())) {
                board.stop();
                board.getGame().setWhiteAgent(new LocalUser(board));
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("Random".equals(player1.getSelectedItem())) {
                board.stop();
                board.getGame().setWhiteAgent(new RandomAgent());
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("SmartFast".equals(player1.getSelectedItem())) {
                board.stop();
                board.getGame().setWhiteAgent(new SimpleMiniMaxAgent(2, new MCTS(25)));
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("SmartSlow".equals(player1.getSelectedItem())) {
                board.stop();
                board.getGame().setWhiteAgent(new SimpleMiniMaxAgent(4, new MCTS(40)));
                status1.setText("Game Stopped");
                start.setText("Start");
            }
        });

        final JComboBox<String> player2 = new JComboBox<>(playerOptions);
        player2.setSelectedIndex(1);
        player2.addActionListener((e) -> {
            if ("Human".equals(player2.getSelectedItem())) {
                board.stop();
                board.getGame().setBlackAgent(new LocalUser(board));
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("Random".equals(player2.getSelectedItem())) {
                board.stop();
                board.getGame().setBlackAgent(new RandomAgent());
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("SmartFast".equals(player2.getSelectedItem())) {
                board.stop();
                board.getGame().setBlackAgent(new SimpleMiniMaxAgent(2, new MCTS(25)));
                status1.setText("Game Stopped");
                start.setText("Start");
            } else if ("SmartSlow".equals(player2.getSelectedItem())) {
                board.stop();
                board.getGame().setBlackAgent(new SimpleMiniMaxAgent(4, new MCTS(40)));
                status1.setText("Game Stopped");
                start.setText("Start");
            }
        });

        control_panel.add(player1);
        control_panel.add(player2);

        start.addActionListener(e -> {
            if (board.getGame().getInPlay()) {
                board.stop();
                status1.setText("Game Stopped");
                start.setText("Start");
            } else {
                board.start();
                status1.setText("White Player's Turn");
                start.setText("Stop");
                board.onUpdatedGameState();
            }
        });
        control_panel.add(start);

        final JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            board.reset();
            status1.setText("Game Not Started");
            start.setText("Start");
            board.onUpdatedGameState();
        });
        control_panel.add(reset);

        final JButton undo = new JButton("Undo");
        undo.addActionListener(e -> {
            board.setGame(board.getGame().undo());
            board.onUpdatedGameState();
            start.setText("Start");
        });
        control_panel.add(undo);

        final JButton save = new JButton("Save");
        save.addActionListener(e -> {
            new Thread(() -> {
                String toOut = board.getGame().toString();
                FileOutputStream fs = null;
                try {
                    fs = new FileOutputStream("files/savedGame.txt");
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    fs.write(toOut.getBytes());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });
        control_panel.add(save);

        final JButton load = new JButton("Load");
        load.addActionListener(e -> {
            OthelloGame oldOthelloGame = board.getGame();
            oldOthelloGame.stop();
            start.setText("Start");

            try {
                FileInputStream fs = new FileInputStream("files/savedGame.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fs));
                String stringRept = br.readLine();
                board.setGame(new OthelloGame(board::onUpdatedGameState, stringRept));
                board.getGame().setWhiteAgent(oldOthelloGame.getWhiteAgent());
                board.getGame().setBlackAgent(oldOthelloGame.getBlackAgent());
                board.onUpdatedGameState();
            } catch (FileNotFoundException ex) {
                System.out.println("File not found");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                System.out.println("File is corrupted");
            }

        });
        control_panel.add(load);

        frame.pack();

        JFrame infoFrame = new JFrame("Info");
        JTextArea myTextArea = new JTextArea(INFO);
        myTextArea.setEditable(false);
        infoFrame.add(myTextArea);
        infoFrame.pack();
        infoFrame.setVisible(true);
        infoFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

}
