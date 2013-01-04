package model;

import java.util.Collections;
import java.util.List;

public interface Game
{
	/**
	 * Starts a new game (a sequence of rounds from TWO to ACE).
	 * 
	 * @returns The initial scores of TWO of all the players.
	 */
	public Scores newGame(int numDecks, List<Player> players);

	/**
	 * @return A RoundInfo consisting of current scores, the current dealer, and
	 *         the current trump VALUE. Returns null if a round is already in
	 *         progress.
	 */
	public RoundInfo getRoundBeginInfo();

	/**
	 * The specified player draws a card.
	 * 
	 * @return The Card consisting of its SUIT and VALUE.
	 */
	public Card draw(Player player) throws IllegalMoveException;

	/**
	 * Shows the specified cards.
	 * 
	 * @return The current trump SUIT.
	 */
	public Card.SUIT show(List<Card> cards) throws IllegalMoveException;

	/**
	 * Hides the specified cards.
	 */
	public void hide(List<Card> cards) throws IllegalMoveException;

	/**
	 * Plays the specified cards.
	 */
	public void play(List<Card> cards) throws IllegalMoveException;

	/**
	 * @return A TrickInfo consisting of the discard cards, the change in points
	 *         earned, and the winning player of that trick. Returns null if the
	 *         trick is not yet over.
	 */
	public TrickInfo getTrickInfo();

	/**
	 * @return A RoundEndInfo consisting of the player scores, the hidden cards,
	 *         and the change in points earned. Returns null if the round is not
	 *         yet over.
	 */
	public RoundEndInfo getRoundEndInfo();

	public final class RoundInfo
	{
		public final Scores playerScores;
		public final Player dealer;
		public final Card.VALUE trumpValue;

		public RoundInfo(Scores playerScores, Player dealer, Card.VALUE trumpValue)
		{
			this.playerScores = playerScores;
			this.dealer = dealer;
			this.trumpValue = trumpValue;
		}
	}

	public final class TrickInfo
	{
		public final List<Card> discardCards;
		public final int dPointsEarned;
		public final Player winningPlayer;

		public TrickInfo(List<Card> discardCards, int dPointsEarned, Player winningPlayer)
		{
			this.discardCards = Collections.unmodifiableList(discardCards);
			this.dPointsEarned = dPointsEarned;
			this.winningPlayer = winningPlayer;
		}
	}

	public final class RoundEndInfo
	{
		public final Scores playerScores;
		public final List<Card> hiddenCards;
		public final int dPointsEarned;

		public RoundEndInfo(Scores playerScores, List<Card> hiddenCards, int dPointsEarned)
		{
			super();
			this.playerScores = playerScores;
			this.hiddenCards = Collections.unmodifiableList(hiddenCards);
			this.dPointsEarned = dPointsEarned;
		}
	}
}
