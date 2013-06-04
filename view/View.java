package view;

import model.Game;
import model.Play;
import server.Server;
import client.Client;

public abstract class View
{
    public final String name;
    public final Server server;
    public final Client client;

    private int myPlayerID;

    public View(String name)
    {
        this.name = name;
        this.server = new Server(this);
        this.client = new Client(this);
    }

    public int getPlayerID()
    {
        return myPlayerID;
    }

    public void setPlayerID(int ID)
    {
        myPlayerID = ID;
    }

    public abstract void start();

    public abstract void createRoom();

    public abstract void closeRoom();

    public abstract void joinRoom();

    public abstract void leaveRoom();

    public abstract void requestStartGame();

    public abstract void startGame(Game game);

    public abstract void requestStartRound();

    public abstract void startRound();

    public abstract void notifyCanMakeKitty();

    public abstract void drawCard(int playerID);

    public abstract void showCards(Play play);

    public abstract void makeKitty(Play play);

    public abstract void playCards(Play play);

    public abstract void notify(String notification);

    public abstract void repaint();
}
