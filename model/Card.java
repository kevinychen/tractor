package model;

import static model.Card.SUIT.*;
import static model.Card.VALUE.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Card
{
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

    public final VALUE value;
    public final SUIT suit;

    public Card(VALUE value, SUIT suit)
    {
        this.value = value;
        this.suit = suit;
    }

    public boolean dataEquals(Card other)
    {
        return value == other.value && suit == other.suit;
    }

    @Override
    public String toString()
    {
        return "Card [value=" + value + ", suit=" + suit + "]";
    }

    /* Encode and decode methods for cards */
    public List<String> encode()
    {
        return Arrays.asList(value.toString(), suit.toString());
    }

    public static Card decode(List<String> data)
    {
        return new Card(Card.VALUE.valueOf(data.get(0)), Card.SUIT.valueOf(data
                .get(1)));
    }

    public static List<String> encodeCards(List<Card> cards)
    {
        List<String> data = new ArrayList<String>();
        data.add(Integer.toString(cards.size()));
        for (Card card : cards)
            data.addAll(card.encode());
        return data;
    }

    public static List<Card> decodeCards(List<String> data)
    {
        List<Card> cards = new ArrayList<Card>();
        for (int i = 1; i < data.size(); i += 2)
            cards.add(decode(data.subList(i, i + 2)));
        return cards;
    }
}
