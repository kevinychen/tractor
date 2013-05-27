package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import model.Card;
import model.Game;
import model.GameProperties;
import model.Play;
import model.Player;

public class Server
{
    private ServerSocket serverSocket;

    private int currentPlayerID = 101;
    private List<Player> players;
    private Map<Integer, PrintWriter> outs;

    private Game game;

    public void startServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);

        new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        final Socket incoming = serverSocket.accept();

                        final BufferedReader in = new BufferedReader(
                                new InputStreamReader(incoming.getInputStream()));

                        new Thread()
                        {
                            Player player;

                            public void run()
                            {
                                try
                                {
                                    player = makePlayer();
                                    if (player == null)
                                        return;

                                    players.add(player);
                                    outs.put(player.ID, new PrintWriter(
                                            incoming.getOutputStream()));

                                    while (true)
                                        processMessage(player,
                                                parse(in.readLine()));
                                } catch (Exception e)
                                {
                                    System.out.println("Connection with "
                                            + player + " broken");
                                    e.printStackTrace();
                                } finally
                                {
                                    if (player != null)
                                    {
                                        players.remove(player);
                                        outs.remove(player);
                                    }
                                }
                            }

                            Player makePlayer() throws IOException
                            {
                                /* HELLO [name] */
                                String[] decoded = parse(in.readLine());
                                if (decoded.length != 2
                                        || !decoded[0].equals("HELLO"))
                                {
                                    return null;
                                } else
                                {
                                    return new Player(currentPlayerID++,
                                            decoded[1]);
                                }
                            }
                        }.start();
                    }
                } catch (IOException e)
                {
                    System.out.println("Server error.");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void processMessage(Player player, String... data)
    {
        if (data[0].equals("STARTGAME"))
        {
            /* STARTGAME [properties] */
            game = new Game(GameProperties.decode(Arrays.asList(data).subList(
                    1, data.length)));
            announce(data);
            // TODO ask other players to verify?
        } else if (data[0].equals("STARTROUND"))
        {
            /* STARTROUND */
            if (game.canStartNewRound())
            {
                game.startRound();
                announce(data);
            }
        } else if (data[0].equals("SHOW"))
        {
            /* SHOW [cards] */
            Play play = new Play(player.ID, Card.decodeCards(Arrays
                    .asList(data).subList(1, data.length)));
            if (game.canShowCards(play))
            {
                game.showCards(play);
                List<String> args = new ArrayList<String>(Arrays.asList(Integer
                        .toString(player.ID)));
                args.addAll(Arrays.asList(data));
                announce(args.toArray(new String[0]));
            }
        }
    }

    protected void message(Player player, String... args)
    {
        PrintWriter out = outs.get(player.ID);
        for (String arg : args)
            out.print(arg.replace(" ", "\1"));
        out.println();
    }

    protected void announce(String... args)
    {
        for (Player player : players)
            message(player, args);
    }

    protected String[] parse(String line)
    {
        String[] data = line.split(" ");
        String[] decoded = new String[data.length];
        for (int i = 0; i < data.length; i++)
            decoded[i] = data[i].replace("\1", " ");
        return decoded;
    }

}
