package view;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import model.Game;

public class View
{
    private JFrame frame;

    private JTextField notificationField;
    private JButton mainButton;
    private GamePanel gamePanel;

    public View()
    {
        frame = new JFrame("Tractor");

        notificationField = new JTextField("Welcome to Tractor");
        mainButton = new JButton("JOIN ROOM");
        gamePanel = new GamePanel();
    }

    public void setup()
    {
        notificationField.setEditable(false);
        
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

    public static void main(String ... args)
    {
        View view = new View();
        view.setup();
        view.show();
    }
}
