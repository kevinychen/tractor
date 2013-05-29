package client;

import java.util.Arrays;
import java.util.List;

import model.Card;
import model.Game;
import model.GameProperties;
import model.Play;
import view.View;

public class HumanClient extends Client
{
    private View view;

    public HumanClient(String name)
    {
        super(name);
    }

    public void addView()
    {
        view = new View(this);
        view.setup();
        view.show();
    }

    @Override
    protected void processMessage(String... data)
    {
        String command = data[0];
        List<String> params = Arrays.asList(data).subList(1, data.length);

        if (command.equals("STARTGAME"))
        {
            /* STARTGAME [properties] */
            game = new Game(GameProperties.decode(params));
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

    @Override
    protected void showNotification(String notification)
    {
        // TODO Auto-generated method stub

    }
}
