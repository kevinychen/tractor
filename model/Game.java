package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Card.SUIT;
import model.Card.VALUE;

public class Game
{
    private final List<Player> players;

    private int numDecks;

    /* A map from playerID to total game score */
    private final Map<Integer, Integer> playerScores;

    /* index of current master (which determines round score) */
    private int masterIndex;

    private List<Card> deck;

    /* declared cards */
    private Play shownCards;

    /* hidden cards by master */
    private Play kitty;

    /* A map from playerID to hand */
    private final Map<Integer, Hand> hands;

    /* A map from playerID to team */
    private final Map<Integer, Integer> teams;

    /* A map from playerID to current round score */
    private final Map<Integer, Integer> currentScores;

    /* A list of tricks, up to the current (possibly unfinished) */
    private final List<Trick> tricks;

    /* Last winning play */
    private Play lastWinningPlay;

    public Game(int numDecks)
    {
        this.players = new ArrayList<Player>();
        this.numDecks = numDecks;
        this.playerScores = new HashMap<Integer, Integer>();
        this.masterIndex = 0;
        this.hands = new HashMap<Integer, Hand>();
        this.teams = new HashMap<Integer, Integer>();
        this.currentScores = new HashMap<Integer, Integer>();
        this.tricks = new ArrayList<Trick>();
    }

    public List<Player> getPlayers()
    {
        return new ArrayList<Player>(players);
    }

    public void addPlayer(Player player)
    {
        players.add(player);
        playerScores.put(player.ID, 0);
    }

    public void removePlayer(Player player)
    {
        players.remove(player);
        playerScores.remove(player.ID);
        if (masterIndex > 0 && masterIndex == players.size())
            masterIndex--;
    }

    public Map<Integer, Integer> getPlayerScores()
    {
        return new HashMap<Integer, Integer>(playerScores);
    }

    public Player getMaster()
    {
        return players.get(masterIndex);
    }

    public Card.VALUE getTrumpValue()
    {
        return Card.values[playerScores.get(players.get(masterIndex).ID)];
    }

    public void startRound()
    {
        /* make deck */
        deck = new ArrayList<Card>();
        for (int deckNum = 0; deckNum < numDecks; deckNum++)
        {
            for (VALUE value : Card.values)
                for (SUIT suit : Card.suits)
                    deck.add(new Card(value, suit));
            deck.add(new Card(Card.VALUE.SMALL_JOKER, Card.SUIT.TRUMP));
            deck.add(new Card(Card.VALUE.BIG_JOKER, Card.SUIT.TRUMP));
        }
        Collections.shuffle(deck);

        /* initialize other variables */
        shownCards = null;
        kitty = null;
        hands.clear();
        teams.clear();
        currentScores.clear();
        tricks.clear();
        tricks.add(new Trick());
        lastWinningPlay = null;
    }

    public void drawFromDeck(Player player)
    {
        hands.get(player.ID).addCard(deck.remove(deck.size() - 1));
    }

    public Play getShownCards()
    {
        return shownCards;
    }

    public void showCards(Play cards)
    {
        shownCards = cards;
    }

    public Card.SUIT getTrumpSuit()
    {
        if (shownCards == null)
            return Card.SUIT.TRUMP;

        return shownCards.getCards().get(0).suit;
    }

    public Play getKitty()
    {
        return kitty;
    }

    public void makeKitty(Play cards)
    {
        kitty = cards;
        hands.get(cards.getPlayerID()).playCards(cards.getCards());
    }

    public void play(Play play)
    {
        Trick currentTrick = tricks.get(tricks.size() - 1);
        currentTrick.addPlay(play);
        hands.get(play.getPlayerID()).playCards(play.getCards());

        if (currentTrick.numPlays() == players.size())
        {
            /* Finish trick */
            lastWinningPlay = winningPlay(currentTrick);
            update(currentScores, lastWinningPlay.getPlayerID(),
                    lastWinningPlay.numPoints());
            tricks.add(new Trick());
        }
    }

    public void endRound()
    {
        /* Add points from kitty, doubled */
        if (teams.get(lastWinningPlay.getPlayerID()) != teams.get(kitty
                .getPlayerID()))
            update(currentScores, lastWinningPlay.getPlayerID(),
                    2 * kitty.numPoints());

        /* Increment scores of players on winning team */
        int totalScore = 0;
        for (Player player : players)
            if (teams.get(player.ID) == 1)
                totalScore += currentScores.get(player.ID);
        if (totalScore >= 40 * numDecks)
            incrementPlayerScores(1, 1);
        else
            incrementPlayerScores(0, 1);
    }

    private void incrementPlayerScores(int winningTeam, int dScore)
    {
        /* Move the master to the next player on the winning team */
        do
        {
            masterIndex = (masterIndex + 1) % players.size();
        } while (teams.get(players.get(masterIndex).ID) != winningTeam);

        /* Increment scores */
        for (Player player : players)
            if (teams.get(player) == winningTeam)
                update(playerScores, player.ID, 1);
    }

    private void update(Map<Integer, Integer> map, int key, int dValue)
    {
        map.put(key, map.get(key) + dValue);
    }

    private Play winningPlay(Trick trick)
    {
        Play startingPlay = trick.getPlays().get(0);

        Card.SUIT startingSuit = suit(startingPlay);
        List<int[]> profile = getProfile(startingPlay.getCards());

        /*
         * For each play, if it matches the profile, compare against initial
         * play
         */
        Play bestPlay = startingPlay;
        for (Play play : trick.getPlays())
            if (suit(play) == startingSuit || suit(play) == Card.SUIT.TRUMP)
            {
                List<List<Card>> permutations = new ArrayList<List<Card>>();
                fillPermutations(new ArrayList<Card>(play.getCards()),
                        new ArrayList<Card>(), permutations);
                for (List<Card> permutation : permutations)
                    if (matchesProfile(permutation, profile)
                            && beats(permutation, bestPlay))
                        bestPlay = play;
            }

        return bestPlay;
    }

    private Card.SUIT suit(Play play)
    {
        Card firstCard = play.getCards().get(0);
        for (Card card : play.getCards())
            if (suit(card) != suit(firstCard))
                return null;

        return suit(firstCard);
    }

    private List<int[]> getProfile(List<Card> cards)
    {
        List<int[]> profile = new ArrayList<int[]>();
        for (int i = 0; i < cards.size(); i++)
        {
            Card card = cards.get(i);
            for (int j = 0; j < i; j++)
            {
                Card otherCard = cards.get(j);
                if (card.equals(otherCard))
                    profile.add(new int[]
                    { i, j, 0 });
                else if (cardRank(card) == cardRank(otherCard) + 1)
                {
                    /* Check if there are two occurrences of card and otherCard */
                    if (cards.indexOf(card) < cards.lastIndexOf(card)
                            && cards.indexOf(otherCard) < cards
                                    .lastIndexOf(otherCard))
                    {
                        profile.add(new int[]
                        { i, j, 1 });
                    }
                }
            }
        }
        return profile;
    }

    private void fillPermutations(List<Card> cards, List<Card> current,
            List<List<Card>> permutations)
    {
        if (cards.isEmpty())
            permutations.add(new ArrayList<Card>(current));

        for (int i = 0; i < cards.size(); i++)
        {
            Card card = cards.remove(i);
            current.add(card);
            fillPermutations(cards, current, permutations);
            cards.add(i, card);
        }
    }

    private boolean matchesProfile(List<Card> cards, List<int[]> profile)
    {
        for (int[] constraint : profile)
        {
            Card card = cards.get(constraint[0]), otherCard = cards
                    .get(constraint[1]);
            if (constraint[2] == 0 && !card.equals(otherCard))
                return false;
            else if (constraint[2] == 1
                    && cardRank(card) != cardRank(otherCard) + 1)
                return false;
        }
        return true;
    }

    private boolean beats(List<Card> cards, Play bestPlay)
    {
        for (int i = 0; i < cards.size(); i++)
        {
            Card card1 = cards.get(i);
            Card card2 = bestPlay.getCards().get(i);
            int score1 = (isTrump(card1) ? 100 : 0) + cardRank(card1);
            int score2 = (isTrump(card2) ? 100 : 0) + cardRank(card2);
            if (score1 < score2)
                return false;
        }
        return true;
    }

    private int cardRank(Card card)
    {
        if (card.value == Card.VALUE.BIG_JOKER)
            return 15;
        else if (card.value == Card.VALUE.SMALL_JOKER)
            return 14;
        else if (card.value == getTrumpValue())
            return (card.suit == getTrumpSuit() ? 13 : 12);
        else if (card.value.ordinal() > getTrumpValue().ordinal())
            return card.value.ordinal() - 1;
        else
            return card.value.ordinal();
    }

    private Card.SUIT suit(Card card)
    {
        return (isTrump(card) ? Card.SUIT.TRUMP : card.suit);
    }

    private boolean isTrump(Card card)
    {
        return card.value == Card.VALUE.BIG_JOKER
                || card.value == Card.VALUE.SMALL_JOKER
                || card.value == getTrumpValue() || card.suit == getTrumpSuit();
    }
}
