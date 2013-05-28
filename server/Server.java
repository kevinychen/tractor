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
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer drawingCardsTimer = new Timer();

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
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Connection with "
                                            + player + " broken");
                                    e.printStackTrace();
                                }
                                finally
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
                                }
                                else
                                {
                                    return new Player(currentPlayerID++,
                                            decoded[1]);
                                }
                            }
                        }.start();
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Server error.");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void processMessage(Player player, String... data)
    {
        String command = data[0];
        List<String> params = Arrays.asList(data).subList(1, data.length);

        if (command.equals("STARTGAME"))
        {
            /* STARTGAME [properties] */
            game = new Game(GameProperties.decode(params));
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
                announce(command, Long.toString(randomSeed));
            }
            // TODO ask other players to verify?

            /* Start drawing */
            drawingCardsTimer.cancel();
            drawingCardsTimer.schedule(new TimerTask()
            {
                public void run()
                {
                    int currentPlayerID = game.getCurrentPlayer().ID;
                    if (game.canDrawFromDeck(currentPlayerID))
                    {
                        game.drawFromDeck(currentPlayerID);
                        announce("DRAW", Integer.toString(currentPlayerID));
                    }
                    else
                    {
                        drawingCardsTimer.cancel();
                    }
                }
            }, 1000, 250);
        }
        else
        {
            Play play = new Play(player.ID, Card.decodeCards(params));
            if (command.equals("SHOW"))
            {
                /* SHOW [cards] */
                if (game.canShowCards(play))
                {
                    game.showCards(play);
                    announce(buildMessage(command, player, params));
                }
            }
            else if (command.equals("MAKEKITTY"))
            {
                /* MAKEKITTY [cards] */
                game.makeKitty(play);
                announce(buildMessage(command, player, params));
            }
            else if (command.equals("PLAY"))
            {
                /* PLAY [cards] */
                if (game.isSpecialPlay(play) && !game.allowedSpecialPlay(play))
                {
                    message(player, "NOTIFICATION", "Invalid special play.");
                    Card minCard = game.minCard(play);
                    play = new Play(player.ID, Arrays.asList(minCard));
                }
                game.play(play);
                announce(buildMessage(command, player,
                        Card.encodeCards(play.getCards())));
            }
        }
    }

    protected String[] buildMessage(String command, Player player,
            List<String> data)
    {
        List<String> args = new ArrayList<String>();
        args.add(command);
        args.add(Integer.toString(player.ID));
        args.addAll(data);
        return args.toArray(new String[0]);
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
