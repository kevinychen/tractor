package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.Card;
import model.Game;
import model.Player;

public class GamePanel extends JPanel
{
    private static final long serialVersionUID = 7889326310244251698L;

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

        if (game == null)
            return;

        int y;

        g.setFont(new Font("Times New Roman", 0, 24));

        y = 0;
        g.drawString("Trump value: " + game.getTrumpValue(), 10, y += 40);
        g.drawString("Trump suit: "
                + (game.getTrumpSuit().ordinal() + '\u2660'), 10, y += 40);
        g.drawString("Starter: " + game.getMaster().name, 10, y += 40);

        y = 0;
        g.drawString("Scores", 600, y += 40);
        Map<Integer, Integer> playerScores = game.getPlayerScores();
        for (int playerID : playerScores.keySet())
            g.drawString(findWithID(playerID, game.getPlayers()).name + ": "
                    + Card.VALUE.values()[playerScores.get(playerID)], 600,
                    y += 40);
    }

    private Player findWithID(int playerID, List<Player> players)
    {
        for (Player player : players)
            if (player.ID == playerID)
                return player;

        return null;
    }
}
