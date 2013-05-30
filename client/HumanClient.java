package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Card;
import model.Game;
import model.GameProperties;
import model.Play;
import model.Player;
import view.View;

public class HumanClient extends Client
{
    private View view;

    private List<Player> players;

    public HumanClient(String name)
    {
        super(name);
        players = new ArrayList<Player>();
    }

    public void addView()
    {
        view = new View(this);
        view.show();
    }

    @Override
    public void connect(int port, byte[] address) throws IOException
    {
        super.connect(port, address);
        view.joinRoom();
    }

    @Override
    public void close()
    {
        super.close();
        view.leaveRoom();
    }

    @Override
    public void requestStartGame(GameProperties properties)
    {
        super.requestStartGame(properties);
        view.requestStartGame();
    }

    public void requestStartRound()
    {
        super.requestStartRound();
        view.requestStartRound();
    }

    @Override
    protected void processMessage(String... data)
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
            myID = Integer.parseInt(data[1]);
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
            game = new Game(GameProperties.decode(params));
            game.addPlayers(players);
            view.startGame(game);
        }
        else if (command.equals("STARTROUND"))
        {
            /* STARTROUND [random seed] */
            game.startRound(Long.parseLong(params.get(0)));
            view.startRound();
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
        view.repaint();
    }

    @Override
    protected void showNotification(String notification)
    {
        // TODO Auto-generated method stub

    }
}
