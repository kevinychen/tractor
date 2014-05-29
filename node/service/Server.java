import java.util.*;
import java.net.*;
import java.io.*;
import org.java_websocket.*;
import org.java_websocket.server.*;
import org.java_websocket.handshake.*;
import model.*;

public class Server extends WebSocketServer
{
    private Map<Integer, String> socketsMap;
    private Map<String, User> usersMap;
    private Map<String, Room> roomsMap;

    public Server(InetSocketAddress addr)
    {
        super(addr);
        socketsMap = new HashMap<Integer, String>();
        usersMap = new HashMap<String, User>();
        roomsMap = new HashMap<String, Room>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        System.out.println("Connected: " + conn);
    }

    @Override
    public synchronized void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        System.out.println("Disconnected: " + conn);
        String username = socketsMap.get(conn.hashCode());
        User user = usersMap.get(username);
        user.room.members.remove(user);
        sendStatus(user.room);
    }

    @Override
    public synchronized void onMessage(WebSocket conn, String message)
    {
        // [COMMAND] [ROOMNAME] [USERNAME] [args]
        String[] data = message.split("__");
        String command = data[0];
        String roomname = data[1];
        String username = data[2];
        if (!roomsMap.containsKey(roomname))
            roomsMap.put(roomname, new Room(roomname));
        if (!usersMap.containsKey(username))
            usersMap.put(username, new User(username));
        Room room = roomsMap.get(roomname);
        User user = usersMap.get(username);
        if (command.equals("HELLO"))
        {
            socketsMap.put(conn.hashCode(), username);
            user.room = room;
            user.socket = conn;
            if (!room.members.contains(user))
                room.members.add(user);
        }
        if (command.equals("STATUS"))
        {
            room.parse(data);
        }

        // Send to everyone in the room
        sendStatus(room);
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.out.println("Error: " + ex);
    }

    public static void main(String ... args) throws Exception
    {
        Server s = new Server(new InetSocketAddress(2916));
        s.start();
    }

    private void sendStatus(Room room)
    {
        String staticJSON = room.staticJSON();
        for (User user : room.members)
            user.socket.send("{\"status\": " + staticJSON + "}");
    }

    class User
    {
        String username;
        Room room;
        WebSocket socket;

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

    class Room
    {
        String roomname;
        List<User> members;
        GameProperties properties;
        boolean status;
        Game game;

        Room(String roomname)
        {
            this.roomname = roomname;
            members = new ArrayList<User>();
            properties = new GameProperties();
            status = false;
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

        String staticJSON()
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
                    "\"status\": %b, " +
                    "\"members\": %s " +
                    "}",
                    roomname,
                    properties.numDecks,
                    properties.find_a_friend,
                    status,
                    usernames
                    );
        }

        void parse(String[] data)
        {
            roomname = data[1];
            properties.numDecks = Integer.parseInt(data[3]);
            properties.find_a_friend = Boolean.parseBoolean(data[4]);
            /*
            status = Boolean.parseBoolean(data[5]);
            members.clear();
            int numMembers = Integer.parseInt(data[6]);
            for (int i = 0; i < numMembers; i++)
                members.add(usersMap.get(data[7 + i]));
            */
        }
    }
}

