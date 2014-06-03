import java.util.*;
import java.net.*;
import java.io.*;
import org.java_websocket.*;
import org.java_websocket.server.*;
import org.java_websocket.handshake.*;
import model.*;

public class Server extends WebSocketServer
{
    private Map<Integer, User> socketsMap;
    private Map<String, User> usersMap;
    private Map<String, Room> roomsMap;

    public Server(InetSocketAddress addr)
    {
        super(addr);
        socketsMap = new HashMap<Integer, User>();
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
        if (socketsMap.containsKey(conn.hashCode()))
        {
            User user = socketsMap.get(conn.hashCode());
            user.room.removeUser(user);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        try {
        onMessage_(conn, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onMessage_(WebSocket conn, String message)
    {
        String[] data = message.split("__");
        System.out.println(Arrays.toString(data));
        String command = data[0];
        User user;

        synchronized (this) 
        {
            if (command.equals("QUERYROOM"))
            {
                // request to get room info
                String roomname = data[1];
                if (roomsMap.containsKey(roomname))
                    conn.send(roomsMap.get(roomname).statusJSON().toString());
                return;
            }
            else if (command.equals("HELLO"))
            {
                // authorization TODO
                String roomname = data[1];
                String username = data[2];
                user = new User(username);
                if (!roomsMap.containsKey(roomname))
                    roomsMap.put(roomname, new Room(roomname));
                user.room = roomsMap.get(roomname);
                user.socket = conn;
                user.playerID = -1;
                socketsMap.put(conn.hashCode(), user);
                user.room.addUser(user);
                return;
            }
            else
            {
                if (!socketsMap.containsKey(conn.hashCode()))
                    return;
                user = socketsMap.get(conn.hashCode());
            }
        }

        Room room = user.room;
        if (command.equals("STATUS"))
            room.updateStatus(user, data);
        else if (command.equals("BEGINGAME"))
            room.beginGame(user);
        else if (command.equals("NEWROUND"))
            room.newRound(user);
        else
        {
            // the remaining commands all involve a play,
            //   which consists of a number of card IDs.
            Play play = room.parsePlay(user, data);
            if (play == null)
                return;
            if (command.equals("SHOW"))
                room.showCards(user, play);
            else if (command.equals("MAKEKITTY"))
                room.makeKitty(user, play);
            else if (command.equals("PLAY"))
                room.play(user, play);
        }
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
}
