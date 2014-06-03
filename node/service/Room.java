import java.util.*;
import org.json.simple.*;
import model.*;
import view.NullView;

class Room
{
    final String roomname;

    final Map<String, User> members;
    private int counter = 0;

    private final GameProperties properties;
    private boolean gameStarted;
    private String status;
    private Game game;

    /* Map from card ID to card object */
    private Map<Integer, Card> cardMap;

    /* Map from player ID to known card IDs */
    private Map<Integer, List<Card>> knownCards;

    /* Timer for drawing the initial cards */
    private Timer drawingCardsTimer;

    Room(String roomname)
    {
        this.roomname = roomname;
        this.members = new HashMap<String, User>();
        this.properties = new GameProperties();
        this.gameStarted = false;
        this.status = "awaiting players";
    }

    @Override
    public boolean equals(Object other)
    {
        return roomname.equals(((Room)other).roomname);
    }

    @Override
    public int hashCode()
    {
        return roomname.hashCode();
    }

    synchronized void addUser(User user)
    {
        if (members.containsKey(user.username))
            user.playerID = members.remove(user.username).playerID;
        members.put(user.username, user);
        sendState();
    }

    synchronized void removeUser(User user)
    {
        if (!gameStarted)
            members.remove(user.username);
        sendState();
    }

    synchronized void updateStatus(User user, String[] data)
    {
        if (gameStarted)
        {
            sendError(user, "Game already started.");
            return;
        }
        properties.numDecks = Integer.parseInt(data[3]);
        properties.find_a_friend = Boolean.parseBoolean(data[4]);
        sendState();
    }

    synchronized void beginGame(User user)
    {
        if (gameStarted)
        {
            sendError(user, "Game already started.");
            return;
        }

        String error = validateProperties();
        if (error != null)
        {
            sendError(user, error);
            return;
        }

        status = "beginning game...";
        sendState();

        gameStarted = true;
        game = new Game(properties);
        game.setView(new NullView("server"));

        int ID = 0;
        for (User u : members.values())
        {
            game.addPlayer(new Player(ID, u.username));
            u.playerID = ID++;
        }

        status = "in-game";

        new Thread()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException e) {}
                synchronized(Room.this)
                {
                    sendState();
                }
            }
        }.start();
    }

    void newRound(User user)
    {
        if (!gameStarted || game.getState() != Game.State.AWAITING_RESTART)
        {
            sendError(user, "Invalid command.");
            return;
        }

        long randomSeed = System.currentTimeMillis();
        game.startRound(randomSeed);

        cardMap = new HashMap<Integer, Card>();
        for (Card card : game.getDeck())
            cardMap.put(card.ID, card);
        knownCards = new HashMap<Integer, List<Card>>();
        for (Player player : game.getPlayers())
            knownCards.put(player.ID, new ArrayList<Card>());
        knownCards.put(-1, game.getDeck());

        drawingCardsTimer = new Timer();
        drawingCardsTimer.schedule(new TimerTask()
                {
                    int waitSteps = 0;

                    public void run()
        {
            int currentPlayerID = game.getCurrentPlayer().ID;
            if (game.started() &&
                game.canDrawFromDeck(currentPlayerID))
            {
                // send card info to the player drawing the card
                knownCards.get(currentPlayerID).add(game.getDeck().getLast());
                game.drawFromDeck(currentPlayerID);
                sendState();
            }
            else if (waitSteps++ > 20)  // wait for 8s for a show
            {
                knownCards.get(game.getMaster().ID).addAll(game.getDeck());
                game.takeKittyCards();
                drawingCardsTimer.cancel();
                sendState();
            }
        }
        }, 1000, 400);
    }

    Play parsePlay(User user, String[] data)
    {
        int numCards = Integer.parseInt(data[1]);
        if (numCards == 0)
            return null;
        List<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < numCards; i++)
            cards.add(cardMap.get(Integer.parseInt(data[2 + i])));
        return new Play(user.playerID, cards);
    }

    void showCards(User user, Play shown)
    {
        if (!gameStarted || !game.canShowCards(shown))
        {
            sendError(user, "Invalid command.");
            return;
        }
        game.showCards(shown);
        announceCards(shown.getCards());
        sendState();
    }

    void makeKitty(User user, Play kitty)
    {
        if (!gameStarted || !game.canMakeKitty(kitty))
        {
            sendError(user, "Invalid command.");
            return;
        }
        game.makeKitty(kitty);
        sendState();
    }

    void play(User user, Play play)
    {
        if (!gameStarted || !game.canPlay(play))
        {
            sendError(user, "Invalid command.");
            return;
        }
        if (game.isSpecialPlay(play))
        {
            Play filteredPlay = game.filterSpecialPlay(play);
            if (filteredPlay != play)
            {
                JSONObject obj = new JSONObject();
                obj.put("notification", "Invalid special play.");
                send(user, JSONValue.toJSONString(obj));
                play = filteredPlay;
            }
        }
        game.play(play);
        announceCards(play.getCards());
        if (game.getState() == Game.State.AWAITING_RESTART)
            announceCards(game.getKitty().getCards());
        sendState();
    }

    JSONObject statusJSON()
    {
        JSONObject obj = new JSONObject();
        obj.put("roomname", roomname);
        JSONObject propertiesJ = new JSONObject();
        propertiesJ.put("numDecks", properties.numDecks);
        propertiesJ.put("find_a_friend", properties.find_a_friend);
        obj.put("properties", propertiesJ);
        obj.put("status", status);
        JSONArray membersJ = new JSONArray();
        for (String username : members.keySet())
            membersJ.add(username);
        obj.put("members", membersJ);
        return obj;
    }

    private JSONObject gameJSON()
    {
        JSONObject obj = new JSONObject();
        if (game != null)
        {
            obj.put("state", game.getState().toString());
            JSONArray players = new JSONArray();
            for (Player player : game.getPlayers())
                players.add(player.name);
            obj.put("players", players);
            obj.put("trumpSuit", game.getTrumpSuit().toString());
            obj.put("trumpVal", game.getTrumpValue().toString());
            obj.put("master", game.getMaster().ID);
            obj.put("gameScores", mapToJSON(game.getPlayerScores()));

            if (game.getState() != Game.State.AWAITING_RESTART)
            {
                obj.put("currPlayer", game.getCurrentPlayer().ID);
                obj.put("deck", cardsToJSON(game.getDeck()));
                obj.put("currTrick", trickToJSON(game.getCurrentTrick()));
                obj.put("prevTrick", trickToJSON(game.getPreviousTrick()));
                obj.put("kitty", playToJSON(game.getKitty()));
                obj.put("shown", playToJSON(game.getShownCards()));
                obj.put("roundScores", mapToJSON(game.getTeamScores()));
                obj.put("friendCards", null);

                JSONObject handsJ = new JSONObject();
                for (Player player : game.getPlayers())
                    handsJ.put(player.ID + "",
                            cardsToJSON(game.getHand(player.ID).getCards()));
                obj.put("hands", handsJ);
            }
        }
        return obj;
    }

    private JSONObject knownCardsJSON(int playerID)
    {
        JSONObject knownCardsJ = new JSONObject();
        if (knownCards != null)
            for (Card card : knownCards.get(playerID))
            {
                JSONObject cardJ = new JSONObject();
                cardJ.put("suit", card.suit.toString());
                cardJ.put("value", card.value.toString());
                knownCardsJ.put(card.ID, cardJ);
            }
        return knownCardsJ;
    }

    private JSONObject stateJSON(int playerID)
    {
        JSONObject obj = new JSONObject();
        obj.put("gameStarted", gameStarted);
        obj.put("status", statusJSON());
        obj.put("game", gameJSON());
        obj.put("cards", knownCardsJSON(playerID));
        return obj;
    }

    private JSONArray cardsToJSON(List<Card> cards)
    {
        cards = new ArrayList<Card>(cards);
        game.sortCards(cards);
        JSONArray cardIDs = new JSONArray();
        for (Card card : cards)
            cardIDs.add(card.ID);
        return cardIDs;
    }

    private JSONObject playToJSON(Play play)
    {
        JSONObject obj = new JSONObject();
        if (play != null)
            obj.put(play.getPlayerID() + "", cardsToJSON(play.getCards()));
        return obj;
    }

    private JSONObject trickToJSON(Trick trick)
    {
        JSONObject obj = new JSONObject();
        if (trick != null)
        {
            for (Play play : trick.getPlays())
                obj.putAll(playToJSON(play));
            if (trick.getWinningPlay() != null)
                obj.put("winner", trick.getWinningPlay().getPlayerID());
        }
        return obj;
    }

    private <K, V> JSONObject mapToJSON(Map<K, V> map)
    {
        JSONObject obj = new JSONObject();
        for (Map.Entry<K, V> entry : map.entrySet())
            obj.put(entry.getKey() + "", entry.getValue() + "");
        return obj;
    }

    private void announceCards(List<Card> cards)
    {
        for (Player player : game.getPlayers())
            knownCards.get(player.ID).addAll(cards);
    }

    private String validateProperties()
    {
        if (properties.find_a_friend)
        {
            return members.size() >= 4 ? null :
                "Need at least 4 players for 'find a friend'.";
        }
        else
        {
            return members.size() % 2 == 0 ? null :
                "Need even number of players.";
        }
    }

    private void send(User user, String s)
    {
        if (user.socket.isOpen())
            user.socket.send(s);
    }

    private void sendError(User user, String s)
    {
        JSONObject obj = new JSONObject();
        obj.put("error", s);
        send(user, obj.toString());
    }

    private void sendState()
    {
        for (User user : members.values())
            send(user, stateJSON(user.playerID).toString());
    }
}
