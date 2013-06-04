package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Card;
import model.Game;
import model.GameProperties;
import model.Play;
import model.Player;
import view.View;

public class Client
{
    private Socket socket;
    private List<Player> players;
    private PrintWriter out;

    private View view;
    private Game game;

    public Client(View view)
    {
        this.socket = new Socket();
        this.players = new ArrayList<Player>();
        this.view = view;
    }

    /**
     * Connects to the server at the specified port and address.
     * 
     * @throws IOException
     */
    public void connect(int port, byte[] address) throws IOException
    {
        socket.connect(new InetSocketAddress(InetAddress.getByAddress(address),
                port), 30000);

        final BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        String line = in.readLine();
                        if (line == null)
                            break;
                        processMessage(parse(line));
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    System.out.println("client has closed input stream");
                    close();
                }
            }
        }.start();
        System.out.println("client has connected input stream");

        out = new PrintWriter(socket.getOutputStream(), true);
        request("HELLO", view.name);

        view.joinRoom();
    }

    public void close()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        view.leaveRoom();
    }

    /* Methods called by controller */

    public void requestStartGame(GameProperties properties)
    {
        request(makeRequest("STARTGAME", properties.encode()));
        view.requestStartGame();
    }

    public void requestStartRound()
    {
        request("STARTROUND");
        view.requestStartRound();
    }

    public void requestShowCards(List<Card> cards)
    {
        Play play = new Play(view.getPlayerID(), cards);
        if (game.canShowCards(play))
        {
            request(makeRequest("SHOW", Card.encodeCards(cards)));
        }
        else
        {
            view.notify("Invalid show.");
        }
    }

    public void requestMakeKitty(List<Card> cards)
    {
        Play play = new Play(view.getPlayerID(), cards);
        if (game.canMakeKitty(play))
        {
            List<String> args = new ArrayList<String>();
            args.add("MAKEKITTY");
            args.addAll(Card.encodeCards(cards));
            request(args.toArray(new String[0]));
        }
        else
        {
            view.notify("Incorrect number of cards.");
        }
    }

    public void requestPlayCards(List<Card> cards)
    {
        Play play = new Play(view.getPlayerID(), cards);
        if (game.canPlay(play))
        {
            List<String> args = new ArrayList<String>();
            args.add("PLAY");
            args.addAll(Card.encodeCards(cards));
            request(args.toArray(new String[0]));
        }
        else
        {
            view.notify("Invalid play.");
        }
    }

    private String[] makeRequest(String command, List<String> data)
    {
        List<String> args = new ArrayList<String>();
        args.add(command);
        args.addAll(data);
        return args.toArray(new String[0]);
    }

    private void request(String... args)
    {
        for (String arg : args)
            out.print(arg.replace(" ", "\1") + " ");
        out.println();
    }

    /* Methods called after a response from the server */

    private void processMessage(String... data)
    {
        String command = data[0];
        List<String> params = Arrays.asList(data).subList(1, data.length);

        System.out.println("Client received request: " + command + " - "
                + params);

        if (command.equals("ADDPLAYER"))
        {
            /* ADDPLAYER [playerID] [player name] */
            Player player = new Player(Integer.parseInt(data[1]), data[2]);
            players.add(player);
            if (game != null)
                game.addPlayer(player);
        }
        else if (command.equals("YOU"))
        {
            /* YOU [playerID] */
            view.setPlayerID(Integer.parseInt(data[1]));
        }
        else if (command.equals("REMOVEPLAYER"))
        {
            /* REMOVEPLAYER [playerID] */
            Player removedPlayer = null;
            for (Player player : players)
                if (players.remove(removedPlayer = player))
                    break;
            if (game != null)
                game.removePlayer(removedPlayer);
        }
        else if (command.equals("STARTGAME"))
        {
            /* STARTGAME [properties] */
            game = new Game(GameProperties.decode(params), view);
            game.addPlayers(players);
        }
        else if (command.equals("STARTROUND"))
        {
            /* STARTROUND [random seed] */
            game.startRound(Long.parseLong(params.get(0)));
        }
        else if (command.equals("NOTIFICATION"))
        {
            // TODO notify the view.
        }
        else if (command.equals("DRAW"))
        {
            /* DRAW [player ID] */
            game.drawFromDeck(Integer.parseInt(params.get(0)));
        }
        else if (command.equals("TAKEKITTY"))
        {
            /* TAKEKITTY */
            game.takeKittyCards();
        }
        else
        {
            int playerID = Integer.parseInt(data[1]);
            Play play = new Play(playerID, Card.decodeCards(Arrays.asList(data)
                    .subList(2, data.length)));
            if (command.equals("SHOW"))
            {
                /* SHOW [cards] */
                game.showCards(play);
            }
            else if (command.equals("MAKEKITTY"))
            {
                /* MAKEKITTY [cards] */
                game.makeKitty(play);
            }
            else if (command.equals("PLAY"))
            {
                /* PLAY [cards] */
                game.play(play);
            }
        }
    }

    private String[] parse(String line)
    {
        String[] data = line.split(" ");
        String[] decoded = new String[data.length];
        for (int i = 0; i < data.length; i++)
            decoded[i] = data[i].replace("\1", " ");
        return decoded;
    }

}
