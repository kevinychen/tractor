import org.java_websocket.WebSocket;

class User
{
    final String username;
    Room room;
    WebSocket socket;
    int playerID;

    User(String username)
    {
        this.username = username;
    }

    @Override
    public boolean equals(Object other)
    {
        return username.equals(((User)other).username);
    }

    @Override
    public int hashCode()
    {
        return username.hashCode();
    }
}

