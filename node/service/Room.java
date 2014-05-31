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
        return "\"game info\"";
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
}
