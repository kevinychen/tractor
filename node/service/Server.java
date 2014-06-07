import java.util.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.math.BigInteger;
import org.java_websocket.*;
import org.java_websocket.server.*;
import org.java_websocket.handshake.*;
import org.json.simple.*;
import org.http_request.*;
import model.*;

public class Server extends WebSocketServer
{
    private String viewserverAddr;
    private String myAddr;
    private String masterKey;
    private String serviceKey;

    private Map<Integer, User> socketsMap;
    private Map<String, User> usersMap;
    private Map<String, Room> roomsMap;

    public Server(InetSocketAddress addr, String viewserverAddr,
        String myAddr, String masterKey)
    {
        super(addr);

        this.viewserverAddr = viewserverAddr;
        this.myAddr = myAddr;
        this.masterKey = masterKey;
        this.serviceKey = new BigInteger(130, new SecureRandom()).toString(32);
        notifyViewserver();

        this.socketsMap = new HashMap<Integer, User>();
        this.usersMap = new HashMap<String, User>();
        this.roomsMap = new HashMap<String, Room>();
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
            if (command.equals("QUERYSERVICE"))
            {
                // request to get service info
                if (data[1] == masterKey)
                    conn.send(serviceJSON().toString());
                return;
            }
            else if (command.equals("QUERYROOM"))
            {
                // request to get room info
                String roomname = data[1];
                if (roomsMap.containsKey(roomname))
                    conn.send(roomsMap.get(roomname).statusJSON().toString());
                return;
            }
            else if (command.equals("HELLO"))
            {
                String roomname = data[1];
                String username = data[2];
                if (!encode(roomname, serviceKey, username).equals(data[3]))
                    return;  // not authorized with serviceKey
                String authorization = data[3];
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

    private void notifyViewserver()
    {
        try
        {
            String urlParameters =
                "address=" + URLEncoder.encode(myAddr, "UTF-8") +
                "&masterKey=" + URLEncoder.encode(masterKey, "UTF-8") +
                "&serviceKey=" + URLEncoder.encode(serviceKey, "UTF-8");
            HTTPRequest.executePost(
                    "http://" + viewserverAddr + "/service", urlParameters);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    private JSONObject serviceJSON()
    {
        JSONObject obj = new JSONObject();
        obj.put("key", serviceKey);
        JSONArray roomsJ = new JSONArray();
        roomsJ.addAll(roomsMap.keySet());
        obj.put("rooms", roomsJ);
        return obj;
    }

    private String encode(String ... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("md5");
            for (String s : data)
                md.update(s.getBytes());
            byte[] encoded = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded)
            {
                String s = Integer.toString(b < 0 ? b + 256 : b, 16);
                if (s.length() == 1)
                    sb.append('0');
                sb.append(s);
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("MD5 algorithm not supported.");
        }
    }

    public static void main(String ... args) throws Exception
    {
        // arguments: [viewserverAddr] [address:port] [masterKey]
        String port = args[1].split(":")[1];
        Server s = new Server(
            new InetSocketAddress(Integer.parseInt(port)),
            args[0],
            args[1],
            args[2]
            );
        s.start();
    }
}
