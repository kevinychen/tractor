package model;

import java.util.ArrayList;
import java.util.List;

public class Hand
{
    private List<Card> cards;
    
    public Hand()
    {
        this.cards = new ArrayList<Card>();
    }
    
    public void addCard(Card card)
    {
        cards.add(card);
    }
    
    public List<Card> getCards()
    {
        return new ArrayList<Card>(cards);
    }
    
    public void playCards(List<Card> cards)
    {
        for (Card card : cards)
            this.cards.remove(card);
    }
}
