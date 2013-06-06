package model;

import java.io.Serializable;

public class GameProperties implements Serializable
{
    private static final long serialVersionUID = 1L;

    /* Number of decks used; 54 cards each */
    public int numDecks = 2;

    /*
     * Whether playing "find-a-friend" version, where a card to determine a
     * friend is called
     */
    public boolean find_a_friend = false;

    @Override
    public String toString()
    {
        return "GameProperties [numDecks=" + numDecks + ", find_a_friend="
                + find_a_friend + "]";
    }
}
