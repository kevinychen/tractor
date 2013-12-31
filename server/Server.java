package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import model.FriendCards;
import model.Game;
import model.GameProperties;
import model.Play;
import model.Player;
import view.NullView;
import view.View;

public class Server
{
    private ServerSocket serverSocket;
    private List<Socket> sockets;

    private int currentPlayerID;
    private List<Player> players;
    private Map<Integer, ObjectOutputStream> outs;

    private View view;
    private Game game;
    private Timer drawingCardsTimer;

    public Server(View view)
    {
        currentPlayerID = 101;
        players = new ArrayList<Player>();
        outs = new HashMap<Integer, ObjectOutputStream>();
        this.view = view;
    }

    public void startServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        sockets = new ArrayList<Socket>();

        new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        final Socket incoming = serverSocket.accept();
                        sockets.add(incoming);

                        new ListenerThread(incoming).start();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    close();
                }
            }
        }.start();

        view.createRoom();
    }

    private class ListenerThread extends Thread
    {
        private Socket incoming;
        private ObjectInputStream in;
        private Player player;

        ListenerThread(Socket incoming)
        {
            this.incoming = incoming;
        }

        public void run()
        {
            try
            {
                initialize();
            }
            catch (IOException e)
            {
                System.out.println("Invalid client tried to connect.");
                return;
            }
            while (true)
            {
                try
                {
                    processMessage(player, (Object[]) in.readObject());
                }
                catch (EOFException e)
                {
                    System.out.println("Client " + player + " left.");
                    break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            synchronized (Server.this)
            {
                players.remove(player);
                outs.remove(player);
                if (game != null)
                    game.removePlayer(player);
                announce("REMOVEPLAYER", player.ID);
            }
        }

        void initialize() throws IOException
        {
            synchronized (Server.this)
            {
                ObjectOutputStream out = new ObjectOutputStream(
                        incoming.getOutputStream());
                in = new ObjectInputStream(incoming.getInputStream());

                /* HELLO [name] */
                Object[] playerInfo;
                try
                {
                    playerInfo = (Object[]) in.readObject();
                }
                catch (ClassNotFoundException e)
                {
                    // Client is using invalid message encoding.
                    throw new IOException();
                }
                if (playerInfo.length != 2 || !playerInfo[0].equals("HELLO"))
                    throw new IllegalArgumentException();
                player = new Player(currentPlayerID++, (String) playerInfo[1]);

                outs.put(player.ID, out);
                for (Player player : players)
                    message(this.player, "ADDPLAYER", player);
                players.add(player);
                if (game != null)
                    message(player, "GAMESTATE", game);
                announce("ADDPLAYER", player);
                message(player, "YOU", player.ID);
            }
        }
    }

    public void close()
    {
        try
        {
            serverSocket.close();
            for (Socket socket : sockets)
                socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        view.closeRoom();
    }

    protected synchronized void processMessage(Player player, Object... data)
    {
        String command = (String) data[0];

        if (command.equals("STARTGAME"))
        {
            /* STARTGAME [properties] */
            if (drawingCardsTimer != null)
                drawingCardsTimer.cancel();
            game = new Game((GameProperties) data[1]);
            game.setView(new NullView(view.name + " [Server]"));
            game.addPlayers(players);
            announce(data);
            // TODO ask other players to verify?
        }
        else if (command.equals("STARTROUND"))
        {
            /* STARTROUND */
            if (game.canStartNewRound())
            {
                long randomSeed = System.currentTimeMillis();
                game.startRound(randomSeed);
                announce(command, randomSeed);

                /* Start drawing */
                drawingCardsTimer = new Timer();
                drawingCardsTimer.schedule(new TimerTask()
                {
                    int waitSteps = 0;

                    public void run()
                    {
                        int currentPlayerID = game.getCurrentPlayer().ID;
                        if (game.started()
                                && game.canDrawFromDeck(currentPlayerID))
                        {
                            game.drawFromDeck(currentPlayerID);
                            announce("DRAW", currentPlayerID);
                        }
                        else if (waitSteps++ > 80) // wait for 8s for a show.
                        {
                            game.takeKittyCards();
                            announce("TAKEKITTY");
                            drawingCardsTimer.cancel();
                        }
                    }
                }, 1000, 100);
            }
            // TODO ask other players to verify?
        }
        else if (command.equals("SELECTFRIEND"))
        {
            /* SELECTFRIEND [player ID] [friend cards] */
            if (game.canSelectFriendCards((Integer) data[1],
                    (FriendCards) data[2]))
            {
                game.selectFriendCards((Integer) data[1], (FriendCards) data[2]);
                announce(data);
            }
        }
        else
        {
            Play play = (Play) data[1];
            if (command.equals("SHOW"))
            {
                /* SHOW [cards] */
                if (game.canShowCards(play))
                {
                    game.showCards(play);
                    announce(data);
                }
            }
            else if (command.equals("MAKEKITTY"))
            {
                /* MAKEKITTY [cards] */
                if (game.canMakeKitty(play))
                {
                    game.makeKitty(play);
                    announce(data);
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
                            message(player, "NOTIFICATION", "Invalid special play.");
                            play = filteredPlay;
                        }
                    }
                    game.play(play);
                    announce(command, play);
                }
            }
        }
    }

    private void message(Player player, Object... args)
    {
        try
        {
            outs.get(player.ID).writeObject(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void announce(Object... args)
    {
        System.out.println("Server announcing " + Arrays.toString(args));
        for (Player player : players)
            message(player, args);
    }
}
