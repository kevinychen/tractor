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
        sendAll(user.room, user.room.stateJSON());
    }

    @Override
    public synchronized void onMessage(WebSocket conn, String message)
    {
        // [QUERYROOM] [roomname]
        // [command] [roomname] [username] [args]
        String[] data = message.split("__");
        String command = data[0];

        String roomname = data[1];
        if (!roomsMap.containsKey(roomname))
            roomsMap.put(roomname, new Room(roomname));
        final Room room = roomsMap.get(roomname);
        if (command.equals("QUERYROOM"))
        {
            send(conn, room.statusJSON());
            return;
        }

        String username = data[2];
        if (!usersMap.containsKey(username))
            usersMap.put(username, new User(username));
        final User user = usersMap.get(username);
        if (command.equals("HELLO"))
        {
            socketsMap.put(conn.hashCode(), username);
            user.room = room;
            user.socket = conn;
            if (!room.members.contains(user))
                room.members.add(user);
            sendAll(room, room.stateJSON());
        }
        else if (command.equals("STATUS"))
        {
            if (room.gameStarted)
                return;
            try
            {
                room.parse(data);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            sendAll(room, room.stateJSON());
        }
        else if (command.equals("BEGINGAME"))
        {
            if (room.gameStarted)
                return;
            String error = room.validateProperties();
            if (error != null)
            {
                send(conn, "{\"error\": \"" + error + "\"}");
                return;
            }
            room.status = "beginning game...";
            sendAll(room, room.stateJSON());
            room.gameStarted = true;
            new Thread()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(3000);
                    }
                    catch (InterruptedException e) {}
                    synchronized(Server.this)
                    {
                        room.status = "in-game";
                        sendAll(room, "{\"begin\": \"game\"}");
                    }
                }
            }.start();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.out.println("Error: " + ex);
    }

    private void send(WebSocket conn, String s)
    {
        if (conn.isOpen())
            conn.send(s);
    }

    private void sendAll(Room room, String s)
    {
        for (User user : room.members)
            send(user.socket, s);
    }

    public static void main(String ... args) throws Exception
    {
        Server s = new Server(new InetSocketAddress(2916));
        s.start();
    }
}
