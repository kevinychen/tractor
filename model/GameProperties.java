package model;

import java.util.Arrays;
import java.util.List;

public class GameProperties
{
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

    public List<String> encode()
    {
        return Arrays.asList(Integer.toString(numDecks),
                Boolean.toString(find_a_friend));
    }

    public static GameProperties decode(List<String> data)
    {
        GameProperties properties = new GameProperties();
        properties.numDecks = Integer.parseInt(data.get(0));
        properties.find_a_friend = Boolean.parseBoolean(data.get(1));
        return properties;
    }
}
