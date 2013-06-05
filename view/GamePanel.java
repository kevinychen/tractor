package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.Card;
import model.Game;
import model.Play;
import model.Player;

public class GamePanel extends JPanel
{
    private static final long serialVersionUID = 7889326310244251698L;

    private BufferedImage[][] CARD_IMAGES;
    private BufferedImage BIG_JOKER_IMAGE, SMALL_JOKER_IMAGE;
    private BufferedImage CARD_BACK_IMAGE;

    private HumanView view;
    private Game game;

    private Map<Card, CardPosition> cardPositions;
    private List<Card> selectedCards;

    public GamePanel(HumanView view)
    {
        setBackground(Color.GREEN);
        this.view = view;
    }

    public void loadImages() throws IOException
    {
        CARD_IMAGES = new BufferedImage[13][4];
        for (int i = 0; i < CARD_IMAGES.length; i++)
            for (int j = 0; j < CARD_IMAGES[i].length; j++)
            {
                String filename = String.format("images/%c%s.gif",
                        "shdc".charAt(j),
                        "2 3 4 5 6 7 8 9 10 j q k 1".split(" ")[i]);
                CARD_IMAGES[i][j] = ImageIO.read(new File(filename));
            }
        BIG_JOKER_IMAGE = ImageIO.read(new File("images/jr.gif"));
        SMALL_JOKER_IMAGE = ImageIO.read(new File("images/jb.gif"));
        CARD_BACK_IMAGE = ImageIO.read(new File("images/b1fv.gif"));
    }

    public void setGame(Game game)
    {
        this.game = game;
        this.cardPositions = new HashMap<Card, CardPosition>();
        this.selectedCards = new ArrayList<Card>();
        for (MouseListener listener : getMouseListeners())
            removeMouseListener(listener);
        addMouseListener(new CardSelectListener());
        new Timer().schedule(new TimerTask()
        {
            public void run()
            {
                for (CardPosition position : cardPositions.values())
                    position.snap();
                repaint();
            }
        }, 50, 50);
    }

    public List<Card> resetSelected()
    {
        List<Card> selected = new ArrayList<Card>(selectedCards);
        selectedCards.clear();
        repaint();
        return selected;
    }

    public void moveCardToHand(Card card, int playerID)
    {
        moveCard(card, handLocation(playerID, card),
                playerID == view.getPlayerID(), 0.5);
    }

    public void moveCardToTable(Card card, int playerID)
    {
        moveCard(card, tableLocation(playerID, card), true, 0.3);
    }

    public void moveCardAway(Card card, int playerID)
    {
        moveCard(card, awayLocation(playerID), true, 0.2);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (game == null)
            return;

        drawGameInformation(g);
        drawGameScores(g);

        if (!game.started())
            return;

        /* Draw deck */
        if (game.deckHasCards())
            drawDeck(g);

        drawCards(g);
    }

    private void moveCard(Card card, Point point, boolean faceUp,
            double snapRatio)
    {
        if (!cardPositions.containsKey(card))
            cardPositions.put(card, new CardPosition(deckLocation(), false));
        cardPositions.get(card).setDest(point, faceUp, snapRatio);
    }

    private void drawGameInformation(Graphics g)
    {
        g.setFont(new Font("Times New Roman", 0, 14));

        /* Draw game information */
        int y = 0;
        g.drawString("Trump value: " + game.getTrumpValue(), 10, y += 18);
        g.drawString("Trump suit: "
                + (game.getTrumpSuit() == Card.SUIT.TRUMP ? '\u2668'
                        : (char) (game.getTrumpSuit().ordinal() + '\u2660')),
                10, y += 18);
        g.drawString("Starter: " + game.getMaster().name, 10, y += 18);
    }

    private void drawGameScores(Graphics g)
    {
        g.setFont(new Font("Times New Roman", 0, 14));

        int y = 0;
        g.drawString("Scores", 800, y += 18);
        Map<Integer, Integer> playerScores = game.getPlayerScores();
        for (int playerID : playerScores.keySet())
            g.drawString(findWithID(playerID).name + ": "
                    + Card.VALUE.values()[playerScores.get(playerID)], 750,
                    y += 18);
    }

    private void drawDeck(Graphics g)
    {
        g.drawImage(CARD_BACK_IMAGE, 415, 300, null);
    }

    private void drawCards(Graphics g)
    {
        Set<Card> drawnCards = new HashSet<Card>();
        for (Player player : game.getPlayers())
            for (Card card : memoizeSortedHandCards(player.ID))
            {
                moveCardToHand(card, player.ID);
                drawCard(card, g);
                drawnCards.add(card);
            }
        for (Play play : game.getCurrentTrick().getPlays())
            for (Card card : play.getCards())
            {
                moveCardToTable(card, play.getPlayerID());
                drawCard(card, g);
                drawnCards.add(card);
            }
        for (Card card : cardPositions.keySet())
            if (!drawnCards.contains(card))
                drawCard(card, g);
    }

    private Point deckLocation()
    {
        return new Point(415, 300);
    }

    private Point handLocation(int playerID, Card card)
    {
        List<Player> players = game.getPlayers();
        double angle = 2 * Math.PI / players.size() * indexWithID(playerID);
        int startX = (int) (450 * (1 + 0.7 * Math.sin(angle)));
        int startY = (int) (350 * (1 + 0.7 * Math.cos(angle)));

        List<Card> cards = memoizeSortedHandCards(playerID);
        int cardIndex = cards.indexOf(card);
        int cardDiff = playerID == view.getPlayerID() ? 14 : 9;
        return new Point((int) (startX + cardDiff * Math.cos(angle)
                * (cardIndex - cards.size() / 2.0) - 35),
                (int) (startY - cardDiff * Math.sin(angle)
                        * (cardIndex - cards.size() / 2.0) - 48));
    }

    private Point tableLocation(int playerID, Card card)
    {
        List<Player> players = game.getPlayers();
        double angle = 2 * Math.PI / players.size() * indexWithID(playerID);
        int startX = (int) (450 * (1 + 0.4 * Math.sin(angle)));
        int startY = (int) (350 * (1 + 0.4 * Math.cos(angle)));

        List<Card> cards = game.getCurrentTrick().getPlayByID(playerID)
                .getCards();
        int cardIndex = cards.indexOf(card);
        return new Point((int) (startX + 24 * Math.cos(angle)
                * (cardIndex - cards.size() / 2.0) - 35), (int) (startY - 24
                * Math.sin(angle) * (cardIndex - cards.size() / 2.0) - 48));
    }

    private Point awayLocation(int playerID)
    {
        List<Player> players = game.getPlayers();
        double angle = 2 * Math.PI / players.size() * indexWithID(playerID);
        int startX = (int) (450 * (1 + 3 * Math.sin(angle)));
        int startY = (int) (350 * (1 + 3 * Math.cos(angle)));
        return new Point(startX, startY);
    }

    private int indexWithID(int playerID)
    {
        return game.getPlayers().indexOf(findWithID(playerID));
    }

    private Player findWithID(int playerID)
    {
        for (Player player : game.getPlayers())
            if (player.ID == playerID)
                return player;

        return null;
    }

    private void drawCard(Card card, Graphics g)
    {
        CardPosition position = cardPositions.get(card);
        BufferedImage image;
        if (!position.faceUp())
            image = CARD_BACK_IMAGE;
        else if (card.value == Card.VALUE.BIG_JOKER)
            image = BIG_JOKER_IMAGE;
        else if (card.value == Card.VALUE.SMALL_JOKER)
            image = SMALL_JOKER_IMAGE;
        else
            image = CARD_IMAGES[card.value.ordinal()][card.suit.ordinal()];
        g.drawImage(image, position.currX(), position.currY(), null);
    }

    private Map<Integer, List<Card>> sortedHandCards = new HashMap<Integer, List<Card>>();
    private Map<Integer, Long> sortedHandCardTimes = new HashMap<Integer, Long>();

    private List<Card> memoizeSortedHandCards(int playerID)
    {
        long currentTime = System.currentTimeMillis();
        if (!sortedHandCardTimes.containsKey(playerID)
                || currentTime - sortedHandCardTimes.get(playerID) > 100)
        {
            sortedHandCards.put(playerID, game.getSortedHandCards(playerID));
            sortedHandCardTimes.put(playerID, currentTime);
        }
        return sortedHandCards.get(playerID);
    }

    private class CardSelectListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            List<Card> cards = memoizeSortedHandCards(view.getPlayerID());
            Collections.reverse(cards);
            for (Card card : cards)
            {
                CardPosition position = cardPositions.get(card);
                if (e.getX() >= position.currX()
                        && e.getX() < position.currX() + 71
                        && e.getY() >= position.currY()
                        && e.getY() < position.currY() + 96)
                {
                    if (selectedCards.contains(card))
                        selectedCards.remove(card);
                    else
                        selectedCards.add(card);
                }
            }
            repaint();
        }
    }
}
