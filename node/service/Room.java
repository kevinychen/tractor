import java.util.*;
import model.*;

class Room
{
    final String roomname;
    final List<User> members;
    final GameProperties properties;
    boolean gameStarted;
    String status;
    Game game;

    Map<Integer, Card> cardMap;
    Timer drawingCardsTimer;

    Room(String roomname)
    {
        this.roomname = roomname;
        this.members = new ArrayList<User>();
        this.properties = new GameProperties();
        this.gameStarted = false;
        this.status = "awaiting players";
    }

    @Override
    public boolean equals(Object other)
    {
        return roomname.equals(((Room)other).roomname);
    }

    @Override
    public int hashCode()
    {
        return roomname.hashCode();
    }

    String statusJSON()
    {
        List<String> usernames = new ArrayList<String>();
        for (User user : members)
            usernames.add("\"" + user.username + "\"");
        return String.format("{" +
                "\"roomname\": \"%s\", " +
                "\"properties\": {" +
                "\"numDecks\": %d, " +
                "\"find_a_friend\": %b" +
                "}, " +
                "\"status\": \"%s\", " +
                "\"members\": %s " +
                "}",
                roomname,
                properties.numDecks,
                properties.find_a_friend,
                status,
                usernames
                );
    }

    String gameJSON()
    {
        if (!gameStarted)
            return "false";

        if (game.getState() == Game.State.AWAITING_RESTART)
            return String.format("{" +
                    "\"state\": \"%s\", " +
                    "\"trumpSuit\": \"%s\", " +
                    "\"trumpVal\": \"%s\", " +
                    "\"master\": %d, " +
                    "\"gameScores\": %s" +
                    "}",
                    game.getState(),
                    game.getTrumpSuit(),
                    game.getTrumpValue(),
                    game.getMaster().ID,
                    mapToJSON(game.getPlayerScores())
                    );

        return String.format("{" +
                "\"state\": \"%s\", " +
                "\"deck\": %s, " +
                "\"currTrick\": %s, " +
                "\"prevTrick\": %s, " +
                "\"hands\": %s, " +
                "\"kitty\": %s, " +
                "\"shown\": %s, " +
                "\"master\": %d, " +
                "\"trumpSuit\": \"%s\", " +
                "\"trumpVal\": \"%s\", " +
                "\"gameScores\": %s, " +
                "\"roundScores\": %s, " +
                "\"friendCards\": %s" +
                "}",
                game.getState(),
                cardsToJSON(game.getDeck()),
                trickToJSON(game.getCurrentTrick()),
                trickToJSON(game.getPreviousTrick()),
                handsToJSON(game),
                playToJSON(game.getKitty()),
                playToJSON(game.getShownCards()),
                game.getMaster().ID,
                game.getTrumpSuit(),
                game.getTrumpValue(),
                mapToJSON(game.getPlayerScores()),
                mapToJSON(game.getTeamScores()),
                friendCardsToJSON(game.getFriendCards())
                );
    }

    String stateJSON()
    {
        return String.format(
                "{\"gameStarted\": %b, \"status\": %s, \"game\": %s}",
                gameStarted, statusJSON(), gameJSON());
    }

    void parse(String[] data)
    {
        properties.numDecks = Integer.parseInt(data[3]);
        properties.find_a_friend = Boolean.parseBoolean(data[4]);
    }

    String validateProperties()
    {
        if (properties.find_a_friend)
        {
            return members.size() >= 4 ? null :
                "Need at least 4 players for 'find a friend'.";
        }
        else
        {
            return members.size() % 2 == 0 ? null :
                "Need even number of players.";
        }
    }

    String cardToJSON(Card card)
    {
        return String.format(
                "{\"id\": %d, \"suit\": \"%s\", \"value\": \"%s\"}",
                card.ID, card.suit, card.value);
    }

    String cardsToJSON(List<Card> cards)
    {
        game.sortCards(cards);
        List<Integer> ids = new ArrayList<Integer>();
        for (Card card : cards)
            ids.add(card.ID);
        return ids.toString();
    }

    String handsToJSON(Game game)
    {
        Map<String, String> json = new HashMap<String, String>();
        for (Player player : game.getPlayers())
            json.put("\"" + player.ID + "\"",
                    cardsToJSON(game.getHand(player.ID).getCards()));
        return json.toString().replace('=', ':');
    }

    String playToJSON(Play play)
    {
        if (play == null)
            return "false";

        Map<String, String> json = new HashMap<String, String>();
        json.put("\"" + play.getPlayerID() + "\"",
                cardsToJSON(play.getCards()));
        return json.toString().replace('=', ':');
    }

    String trickToJSON(Trick trick)
    {
        if (trick == null)
            return "false";

        Map<String, String> json = new HashMap<String, String>();
        for (Play play : trick.getPlays())
            json.put("\"" + play.getPlayerID() + "\"",
                    cardsToJSON(play.getCards()));
        if (trick.getWinningPlay() != null)
            json.put("\"winner\"",
                    trick.getWinningPlay().getPlayerID() + "");
        return json.toString().replace('=', ':');
    }

    String friendCardsToJSON(FriendCards friendCards)
    {
        if (friendCards == null)
            return "false";

        Map<String, Integer> json = new HashMap<String, Integer>();
        for (Map.Entry<Card, Integer> entry : friendCards.getFriendCards().entrySet())
            json.put("\"" + entry.getKey().ID + "\"", entry.getValue());
        return json.toString().replace('=', ':');
    }

    <K, V> String mapToJSON(Map<K, V> map)
    {
        Map<String, String> json = new HashMap<String, String>();
        for (Map.Entry<K, V> entry : map.entrySet())
            json.put("\"" + entry.getKey() + "\"",
                   entry.getValue().toString());
        return json.toString().replace('=', ':');
    }
}
