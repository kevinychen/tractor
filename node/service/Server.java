import java.util.*;
import java.net.*;
import java.io.*;
import org.java_websocket.*;
import org.java_websocket.server.*;
import org.java_websocket.handshake.*;
import model.*;
import view.NullView;

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
        try
        {
            onMessageHelper(conn, message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onMessageHelper(WebSocket conn, String message)
    {
        // [QUERYROOM] [roomname]
        // [command] [roomname] [username] [args]
        String[] data = message.split("__");
        String command = data[0];

        String roomname = data[1];
        if (!roomsMap.containsKey(roomname))
            roomsMap.put(roomname, new Room(roomname));
        final Room room = roomsMap.get(roomname);
        final Game game = room.game;
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
            room.parse(data);
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
            room.game = new Game(room.properties);
            room.game.setView(new NullView("server"));

            // Add player list
            List<Player> players = new ArrayList<Player>();
            for (int i = 0; i < room.members.size(); i++)
                players.add(new Player(i, room.members.get(i).username));
            room.game.addPlayers(players);

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
                        sendAll(room, room.stateJSON());
                    }
                }
            }.start();
        }
        else if (command.equals("NEWROUND"))
        {
            if (!room.gameStarted ||
                    game.getState() != Game.State.AWAITING_RESTART)
            {
                return;
            }

            long randomSeed = System.currentTimeMillis();
            game.startRound(randomSeed);

            // Set card mapping
            room.cardMap = new HashMap<Integer, Card>();
            for (Card card : game.getDeck())
                room.cardMap.put(card.ID, card);

            /* Start drawing */
            room.drawingCardsTimer = new Timer();
            room.drawingCardsTimer.schedule(new TimerTask()
            {
                int waitSteps = 0;

                public void run()
                {
                    int currentPlayerID = game.getCurrentPlayer().ID;
                    if (game.started()
                        && game.canDrawFromDeck(currentPlayerID))
                    {
                        // send card info to the player drawing the card
                        send(room.members.get(currentPlayerID).socket,
                            "{\"card\": " +
                            room.cardToJSON(game.getDeck().getLast()) + "}");
                        game.drawFromDeck(currentPlayerID);
                        sendAll(room, room.stateJSON());
                    }
                    else if (waitSteps++ > 80) // wait for 8s for a show
                    {
                        game.takeKittyCards();
                        // send kitty cards to the master
                        Play kitty = game.getKitty();
                        if (kitty != null)
                        {
                            User master = room.members.get(kitty.getPlayerID());
                            for (Card card : kitty.getCards())
                                send(master.socket,
                                        "{\"card\": " + room.cardToJSON(card));
                        }
                        sendAll(room, room.stateJSON());
                        room.drawingCardsTimer.cancel();
                    }
                }
            }, 1000, 100);
        }
        else
        {
            if (room.gameStarted)
                return;

            // the remaining commands all involve a play,
            //   which consists of a number of card IDs.
            int numCards = Integer.parseInt(data[3]);
            List<Card> cards = new ArrayList<Card>();
            for (int i = 0; i < numCards; i++)
                cards.add(room.cardMap.get(Integer.parseInt(data[4 + i])));
            Play play = new Play(room.members.indexOf(user), cards);

            if (command.equals("SHOW"))
            {
                /* SHOW [cards] */
                if (game.canShowCards(play))
                {
                    game.showCards(play);
                    for (Card card : cards)
                        sendAll(room, "{\"card\": " + room.cardToJSON(card));
                    sendAll(room, room.stateJSON());
                }
            }
            else if (command.equals("MAKEKITTY"))
            {
                /* MAKEKITTY [cards] */
                if (game.canMakeKitty(play))
                {
                    game.makeKitty(play);
                    sendAll(room, room.stateJSON());
                }
            }
            else if (command.equals("PLAY"))
            {
                /* PLAY [cards] */
                if (game.canPlay(play))
                {
                    if (game.isSpecialPlay(play))
                    {
                        Play filteredPlay = game.filterSpecialPlay(play);
                        if (filteredPlay != play)
                        {
                            send(conn, "{\"notification\": " +
                                    "\"Invalid special play.\"}");
                            play = filteredPlay;
                        }
                    }
                    game.play(play);
                    for (Card card : play.getCards())
                        sendAll(room, "{\"card\": " + room.cardToJSON(card));
                    if (game.getState() == Game.State.AWAITING_RESTART)
                        for (Card card : game.getKitty().getCards())
                            sendAll(room, "{\"card\": " + room.cardToJSON(card));
                    sendAll(room, room.stateJSON());
                }
            }
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
