package view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import model.Game;

public class GamePanel extends JPanel
{
    private Game game;
    
    public GamePanel()
    {
        setBackground(Color.GREEN);
    }
    
    public void setGame(Game game)
    {
        this.game = game;
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
}
