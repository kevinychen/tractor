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
    protected String name;

    private Socket socket;

    private PrintWriter out;

    protected Game game;
    protected Player me;

    public Client(String name)
    {
        this.name = name;
        this.socket = new Socket();
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
        request("HELLO", name);
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
    }

    /* Listed below are all client side methods */

    public void requestStartGame(GameProperties properties)
    {
        request(makeRequest("STARTGAME", properties.encode()));
    }

    public void requestStartRound()
    {
        request("STARTROUND");
    }

    public void requestShowCards(List<Card> cards)
    {
        Play play = new Play(me.ID, cards);
        if (game.canShowCards(play))
        {
            request(makeRequest("SHOW", Card.encodeCards(cards)));
        }
        else
        {
            showNotification("Invalid show.");
        }
    }

    public void requestMakeKitty(List<Card> cards)
    {
        List<String> args = new ArrayList<String>();
        args.add("MAKEKITTY");
        args.addAll(Card.encodeCards(cards));
        request(args.toArray(new String[0]));
    }

    public void requestPlayCards(List<Card> cards)
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

    protected String[] makeRequest(String command, List<String> data)
    {
        List<String> args = new ArrayList<String>();
        args.add(command);
        args.addAll(data);
        return args.toArray(new String[0]);
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
