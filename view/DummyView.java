package view;

import model.Card;
import model.Game;
import model.Play;
import model.Trick;

public class DummyView extends View
{
    public DummyView(String name)
    {
        super(name);
    }

    @Override
    public void start()
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                    client.connect(3003, new byte[]
                    { 127, 0, 0, 1 });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void createRoom()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeRoom()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void joinRoom()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void leaveRoom()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestStartGame()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void startGame(Game game)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestStartRound()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void startRound()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyCanMakeKitty()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawCard(Card card, int playerID)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void showCards(Play play)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void makeKitty(Play play)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void playCards(Play play)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void finishTrick(Trick trick, int winnerID)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void notify(String notification)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void repaint()
    {
        // TODO Auto-generated method stub

    }

}
