package model;

import static model.Card.SUIT.*;
import static model.Card.VALUE.*;

import java.io.Serializable;
import java.util.List;

public final class Card implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum VALUE
    {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE, SMALL_JOKER, BIG_JOKER
    };

    public enum SUIT
    {
        SPADE, HEART, DIAMOND, CLUB, TRUMP
    };

	public static final VALUE[] values = {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE};
    public static final SUIT[] suits = {SPADE, HEART, CLUB, DIAMOND};

    public static final String[] valueStrs = "2,3,4,5,6,7,8,9,10,J,Q,K,A,Small Joker,Big Joker".split(",");
    public static final String[] suitStrs = {"\u2660", "\u2661", "\u2662", "\u2663", "*"};

    public final VALUE value;
    public final SUIT suit;
    public final int ID;

    public Card(VALUE value, SUIT suit, int ID)
    {
        this.value = value;
        this.suit = suit;
        this.ID = ID;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ID;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Card other = (Card) obj;
        if (ID != other.ID)
            return false;
        return true;
    }

    public boolean dataEquals(Card other)
    {
        return value == other.value && suit == other.suit;
    }

    public int frequencyIn(List<Card> cards)
    {
        int frequency = 0;
        for (Card card : cards)
            if (dataEquals(card))
                frequency++;
        return frequency;
    }

    @Override
    public String toString()
    {
        return valueStrs[value.ordinal()] + suitStrs[suit.ordinal()];
    }
}
