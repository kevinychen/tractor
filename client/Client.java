package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import model.Card;
import model.Game;
import model.GameProperties;
import model.Play;
import model.Player;

public abstract class Client
{
    private String name;

    private PrintWriter out;

    private Game game;
    private Player me;

    public Client(String name)
    {
        this.name = name;
    }

    /**
     * Connects to the server at the specified port and address.
     * 
     * @throws IOException
     */
    public void connect(int port, byte[] address) throws IOException
    {
        Socket socket = new Socket();
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
                        processMessage(parse(in.readLine()));
                }
                catch (IOException e)
                {
                    System.out.println("Socket connection closed.");
                    e.printStackTrace();
                }
            }
        }.start();

        out = new PrintWriter(socket.getOutputStream(), true);
        request("HELLO", name);
    }

    /* Listed below are all client side methods */

    public void startGame(GameProperties properties)
    {
        game = new Game(properties);
        request("STARTGAME");
    }

    public void startRound()
    {
        request("STARTROUND");
    }

    public void showCards(List<Card> cards)
    {
        Play play = new Play(me.ID, cards);
        if (game.canShowCards(play))
        {
            List<String> args = new ArrayList<String>();
            args.add("SHOW");
            args.addAll(Card.encodeCards(cards));
            request(args.toArray(new String[0]));
        }
        else
        {
            showNotification("Invalid show.");
        }
    }

    public void makeKitty(List<Card> cards)
    {
        List<String> args = new ArrayList<String>();
        args.add("MAKEKITTY");
        args.addAll(Card.encodeCards(cards));
        request(args.toArray(new String[0]));
    }

    public void playCards(List<Card> cards)
    {
        Play play = new Play(me.ID, cards);
        if (game.canPlay(play))
        {
            List<String> args = new ArrayList<String>();
            args.add("PLAY");
            args.addAll(Card.encodeCards(cards));
            request(args.toArray(new String[0]));
        }
        else
        {
            showNotification("Invalid play.");
        }
    }

    /* Listed below are accessor methods to root class */

    protected abstract void processMessage(String... data);

    protected abstract void showNotification(String notification);

    protected String getName()
    {
        return name;
    }

    protected void request(String... args)
    {
        for (String arg : args)
            out.print(arg.replace(" ", "\1") + " ");
        out.println();
    }

    protected String[] parse(String line)
    {
        String[] data = line.split(" ");
        String[] decoded = new String[data.length];
        for (int i = 0; i < data.length; i++)
            decoded[i] = data[i].replace("\1", " ");
        return decoded;
    }

    /**
     * Passed to the view for calling.
     */
    public interface Listener
    {
        public void requestNewGame();

        public void requestResign();

        public void requestDrawCard(Card cardID);

        public void requestShowCards(List<Card> cardIDs);

        public void requestHideCards(List<Card> cardIDs);

        public void requestPlayCards(List<Card> cardIDs);

        public void acknowledge();
    }
}
