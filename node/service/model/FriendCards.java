package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FriendCards implements Serializable
{
    private static final long serialVersionUID = 1L;

    final Map<Card, Integer> friendCards;

    public FriendCards()
    {
        friendCards = new HashMap<Card, Integer>();
    }

    public void clear()
    {
        friendCards.clear();
    }

    public void addFriendCard(Card card, int index)
    {
        if (friendCards.containsKey(card))
            throw new IllegalStateException("Error: Card already added.");

        friendCards.put(card, index);
    }

    public Map<Card, Integer> getFriendCards()
    {
        return new HashMap<Card, Integer>(friendCards);
    }

    public boolean isEmpty()
    {
        return friendCards.isEmpty();
    }

    public int size()
    {
        return friendCards.size();
    }

    /*
     * Updates the friend cards information with this newly played card. Returns
     * true if this played card causes the player to become a friend.
     */
    public boolean update(Card playedCard)
    {
        boolean updated = false;
        for (Card card : friendCards.keySet())
            if (card.dataEquals(playedCard))
            {
                friendCards.put(card, friendCards.get(card) - 1);
                if (friendCards.get(card) == 0)
                {
                    friendCards.remove(card);
                    updated = true;
                }
            }
        return updated;
    }
}
