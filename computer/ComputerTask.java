package computer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import model.Card;
import model.Game;
import model.Hand;
import model.Play;

public class ComputerTask extends TimerTask
{
    private ComputerView view;
    private Game game;

    public ComputerTask(ComputerView view, Game game)
    {
        this.view = view;
        this.game = game;
    }

    @Override
    public void run()
    {
        if (game == null)
            return;

        Hand hand = game.getHand(view.getPlayerID());
        Game.State state = game.getState();
        if (state == null)
            return;

        switch (state)
        {
            case AWAITING_SHOW:
                for (Card card : hand.getCards())
                    if (card.value == game.getTrumpValue())
                    {
                        List<Card> cards = new ArrayList<Card>();
                        for (Card otherCard : hand.getCards())
                            if (card.dataEquals(otherCard))
                                cards.add(otherCard);
                        view.client.requestShowCards(cards);
                        return;
                    }
                break;
            case AWAITING_FRIEND_CARDS:
                // TODO
                break;
            case AWAITING_KITTY:
                if (game.getMaster().ID == view.getPlayerID())
                {
                    List<Card> cards = new ArrayList<Card>();
                    for (int i = 0; i < view.getKittySize(); i++)
                        cards.add(hand.getCards().get(i));
                    view.client.requestMakeKitty(cards);
                }
                break;
            case AWAITING_PLAY:
                if (game.getCurrentPlayer().ID == view.getPlayerID())
                {
                    List<Card> cards = new ArrayList<Card>();
                    if (game.getCurrentTrick().getPlays().isEmpty())
                    {
                        cards.add(hand.getCards().get(0));
                    }
                    else
                    {
                        Play play = game.getCurrentTrick().getInitialPlay();
                        Card.SUIT startingSuit = suit(play.getPrimaryCard());
                        for (Card card : hand.getCards())
                            if (suit(card) == startingSuit
                                    && cards.size() < play.numCards())
                            {
                                cards.add(card);
                                for (Card otherCard : hand.getCards())
                                    if (card.dataEquals(otherCard)
                                            && cards.size() < play.numCards()
                                            && !cards.contains(otherCard))
                                        cards.add(otherCard);
                            }
                        for (Card card : hand.getCards())
                            if (cards.size() < play.numCards()
                                    && !cards.contains(card))
                                cards.add(card);
                    }
                    view.client.requestPlayCards(cards);
                }
                break;
            case AWAITING_RESTART:
                break;
        }
    }

    private Card.SUIT suit(Card card)
    {
        return (card.suit == game.getTrumpSuit()
                || card.value == game.getTrumpValue() ? Card.SUIT.TRUMP
                : card.suit);
    }
}
