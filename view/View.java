package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import model.Game;
import model.GameProperties;
import server.Server;
import client.Client;

public class View
{
    private Server server;
    private Client client;

    private JFrame frame;

    private JTextField notificationField;
    private JButton[] buttons;
    private GamePanel gamePanel;

    public View(Client client)
    {
        this.server = new Server();
        this.client = client;

        frame = new JFrame("Tractor");

        notificationField = new JTextField("Welcome to Tractor");
        buttons = new JButton[4];
        for (int i = 0; i < buttons.length; i++)
            buttons[i] = new JButton();

        gamePanel = new GamePanel();
    }

    public void setup()
    {
        notificationField.setEditable(false);

        buttons[0].setText("Create Room");
        buttons[0].addActionListener(new CreateRoomActionListener());
        buttons[1].setText("Join Room");
        buttons[1].addActionListener(new JoinRoomActionListener());
        buttons[2].setText("New Game");
        buttons[2].addActionListener(new StartNewGameActionListener());
        buttons[3].setText("New Round");
        buttons[3].addActionListener(new StartNewRoundActionListener());

        frame.setSize(800, 600);
        frame.setResizable(false);

        frame.getContentPane().setLayout(
                new GroupLayout(frame.getContentPane()));
        arrangeHeadbar(buttons[0], buttons[1]);
    }

    public void arrangeHeadbar(JButton... buttons)
    {
        GroupLayout layout = (GroupLayout) frame.getContentPane().getLayout();
        Group sequentialGroup = layout.createSequentialGroup();
        Group parallelGroup = layout.createParallelGroup(Alignment.BASELINE);
        sequentialGroup.addComponent(notificationField);
        parallelGroup.addComponent(notificationField);
        for (JButton button : buttons)
        {
            sequentialGroup.addComponent(button);
            parallelGroup.addComponent(button);
        }
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(sequentialGroup).addComponent(gamePanel));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(parallelGroup).addComponent(gamePanel));
    }

    public void show()
    {
        frame.setVisible(true);
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setGame(Game game)
    {
        gamePanel.setGame(game);
    }

    public void repaint()
    {
        frame.repaint();
    }

    private void removeButtonListeners(JButton button)
    {
        for (ActionListener listener : button.getActionListeners())
            button.removeActionListener(listener);
    }

    private class CreateRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            removeButtonListeners(buttons[0]);

            try
            {
                /* Find external IP */
                String IP = new BufferedReader(new InputStreamReader(new URL(
                        "http://api.exip.org/?call=ip").openStream()))
                        .readLine();
                notificationField.setText("Setting up server...");
                server.startServer(3003);

                notificationField.setText("Your IP is " + IP + ". Players:");

                buttons[0].setText("Close Room");
                buttons[0].addActionListener(new CloseRoomActionListener());
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(frame, e2.getMessage());

                buttons[0].addActionListener(new CreateRoomActionListener());
            }
        }
    }

    private class CloseRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to close the room?") == JOptionPane.YES_OPTION)
            {
                removeButtonListeners(buttons[0]);
                server.close();
                buttons[0].setText("Create Room");
                buttons[0].addActionListener(new CreateRoomActionListener());
            }
        }
    }

    private class JoinRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            removeButtonListeners(buttons[1]);

            String input = JOptionPane
                    .showInputDialog("Enter IP: e.g. 192.168.0.1");

            try
            {
                notificationField.setText("Connecting to server...");

                /* Try to parse input IP */
                String[] addressStr = input.split("\\.");
                byte[] address = new byte[4];
                for (int i = 0; i < 4; i++)
                    address[i] = (byte) Short.parseShort(addressStr[i]);

                /* Connect to server */
                client.connect(3003, address);

                arrangeHeadbar(buttons[0], buttons[1], buttons[2], buttons[3]);
                buttons[1].setText("Start New Game");
                buttons[1].addActionListener(new LeaveRoomActionListener());
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(frame, e2.getMessage());

                buttons[1].addActionListener(new JoinRoomActionListener());
            }
        }
    }

    private class LeaveRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            removeButtonListeners(buttons[1]);

            client.close();

            arrangeHeadbar(buttons[0], buttons[1]);
            buttons[1].setText("Join Room");
            buttons[1].addActionListener(new JoinRoomActionListener());
        }
    }

    private class StartNewRoundActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            notificationField.setText("Waiting for other players...");
            client.startRound();
        }
    }

    private class StartNewGameActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            // TODO Enter actual properties
            String input = JOptionPane.showInputDialog("Enter properties:");

            notificationField.setText("Sending new game request...");
            GameProperties properties = new GameProperties();
            properties.numDecks = 2;
            properties.find_a_friend = false;
            client.startGame(properties);
        }
    }
}
