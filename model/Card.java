package model;

import static model.Card.SUIT.*;
import static model.Card.VALUE.*;

public final class Card
{
	public enum VALUE
	{
		TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE, SMALL_JOKER, BIG_JOKER
	};

	public enum SUIT
	{
		SPADE, HEART, CLUB, DIAMOND, TRUMP
	};

	public static final VALUE[] values = {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE};
    public static final SUIT[] suits = {SPADE, HEART, CLUB, DIAMOND};

    public final VALUE value;
    public final SUIT suit;

    public Card(VALUE value, SUIT suit)
    {
        this.value = value;
        this.suit = suit;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((suit == null) ? 0 : suit.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        if (suit != other.suit)
            return false;
        if (value != other.value)
            return false;
        return true;
    }
}