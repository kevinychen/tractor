package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import model.Game;
import client.Client;

public class View
{
    private enum State
    {
        JOIN_ROOM, START_ROUND, PLAYING
    };

    private State state;

    private Client client;

    private JFrame frame;

    private JTextField notificationField;
    private JButton mainButton;
    private GamePanel gamePanel;

    public View(Client client)
    {
        this.client = client;

        frame = new JFrame("Tractor");

        notificationField = new JTextField("Welcome to Tractor");
        mainButton = new JButton();
        gamePanel = new GamePanel();
    }

    public void setup()
    {
        notificationField.setEditable(false);
        setState(State.JOIN_ROOM);

        frame.setSize(800, 600);
        frame.setResizable(false);

        GroupLayout layout = new GroupLayout(frame.getContentPane());

        layout.setHorizontalGroup(layout
                .createParallelGroup()
                .addGroup(
                        layout.createSequentialGroup()
                                .addComponent(notificationField)
                                .addComponent(mainButton))
                .addComponent(gamePanel));
        layout.setVerticalGroup(layout
                .createSequentialGroup()
                .addGroup(
                        layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(notificationField)
                                .addComponent(mainButton))
                .addComponent(gamePanel));

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

    private void removeButtonListeners()
    {
        this.state = null;
        for (ActionListener listener : mainButton.getActionListeners())
            mainButton.removeActionListener(listener);
    }

    private void setState(State state)
    {
        this.state = state;
        switch (state)
        {
            case JOIN_ROOM:
                mainButton.setText("JOIN ROOM");
                mainButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        removeButtonListeners();

                        String input = JOptionPane
                                .showInputDialog("Enter IP: e.g. 192.168.0.1");

                        try
                        {
                            /* Try to parse input IP */
                            String[] addressStr = input.split("\\.");
                            byte[] address = new byte[4];
                            for (int i = 0; i < 4; i++)
                                address[i] = (byte)Short.parseShort(addressStr[i]);

                            /* Connect to server */
                            client.connect(3003, address);
                            setState(State.START_ROUND);
                        }
                        catch (Exception e2)
                        {
                            JOptionPane.showMessageDialog(frame, e2.getMessage());
                            setState(State.JOIN_ROOM);
                        }
                    }
                });
                break;
            case START_ROUND:
                break;
            case PLAYING:
                break;
        }
    }
}
