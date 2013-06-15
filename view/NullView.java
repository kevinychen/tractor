package view;

import model.Card;
import model.Game;
import model.Play;
import model.Trick;

public class NullView extends View
{
    public NullView(String name)
    {
        super(name);
    }

    @Override
    public void start()
    {
        // TODO Auto-generated method stub

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
    public void notifyCanMakeKitty(int kittySize)
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
    public void endRound()
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
