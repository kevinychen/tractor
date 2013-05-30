package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private GamePanel gamePanel;

    private JButton createRoomButton, closeRoomButton, joinRoomButton,
            leaveRoomButton, newGameButton, newRoundButton;
    private JButton[] buttons;

    public View(Client client)
    {
        this.server = new Server();
        this.client = client;

        frame = new JFrame("Tractor");

        notificationField = new JTextField("Welcome to Tractor");
        notificationField.setEditable(false);

        createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(new CreateRoomActionListener());
        closeRoomButton = new JButton("Close Room");
        closeRoomButton.addActionListener(new CloseRoomActionListener());
        joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(new JoinRoomActionListener());
        leaveRoomButton = new JButton("Leave Room");
        leaveRoomButton.addActionListener(new LeaveRoomActionListener());
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new NewGameActionListener());
        newRoundButton = new JButton("New Round");
        newRoundButton.addActionListener(new NewRoundActionListener());
        buttons = new JButton[]
        { createRoomButton, closeRoomButton, joinRoomButton, leaveRoomButton,
                newGameButton, newRoundButton };
        for (JButton button : buttons)
            button.setVisible(false);
        createRoomButton.setVisible(true);
        joinRoomButton.setVisible(true);

        gamePanel = new GamePanel();

        frame.setSize(800, 600);
        frame.setResizable(false);

        arrange();
    }

    public void arrange()
    {
        GroupLayout layout = new GroupLayout(frame.getContentPane());

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

        frame.getContentPane().setLayout(layout);
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

    private class CreateRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                /* Find external IP */
                String IP = new BufferedReader(new InputStreamReader(new URL(
                        "http://api.exip.org/?call=ip").openStream()))
                        .readLine();
                notificationField.setText("Setting up server...");
                server.startServer(3003);

                notificationField.setText("Your IP is " + IP + ". Players:");

                createRoomButton.setVisible(false);
                closeRoomButton.setVisible(true);
                frame.validate();
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(frame, e2.getMessage());
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
                server.close();

                closeRoomButton.setVisible(false);
                createRoomButton.setVisible(true);
                frame.validate();
            }
        }
    }

    private class JoinRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
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

                joinRoomButton.setVisible(false);
                leaveRoomButton.setVisible(true);
                newGameButton.setVisible(true);
                newRoundButton.setVisible(true);
                frame.validate();
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(frame, e2.getMessage());
            }
        }
    }

    private class LeaveRoomActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            client.close();

            leaveRoomButton.setVisible(false);
            joinRoomButton.setVisible(true);
            newGameButton.setVisible(false);
            newRoundButton.setVisible(false);
            frame.validate();
        }
    }

    private class NewRoundActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            notificationField.setText("Waiting for other players...");
            client.startRound();
        }
    }

    private class NewGameActionListener implements ActionListener
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
