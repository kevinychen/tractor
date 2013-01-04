package view;

import java.util.List;

import model.Card;
import model.Player;
import model.Scores;
import client.Client;

public interface View
{
	/**
	 * Tells the view what client methods to call.
	 */
	public void addListener(Client.Listener listener);
	
	/**
	 * Passed to the client for calling.
	 */
	public interface Listener
	{
		public void showNewGame(int numDecks, Scores playerScores);
		
		public void showNewRound(Scores playerScores, Player dealer, Card.VALUE trumpValue);
		
		public void showDraw(Card card, Player player);
		
		public void showShow(List<Card> cards, Player player, Card.SUIT trumpSuit);
		
		public void showHide(List<Card> cards);
		
		public void showPlay(List<Card> cards);
		
		public void showTrickInfo(List<Card> discards, int dPointsEarned, Player winningPlayer);
		
		public void showRoundEnd(Scores playerScores, List<Card> hiddenCards, int dPointsEarned);
	}
}
