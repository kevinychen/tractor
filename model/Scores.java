package model;

import java.util.List;

public interface Scores
{
	/**
	 * @return An unmodifiable list of the players.
	 */
	public List<Player> getPlayers();
	
	/**
	 * @return The current score of the given player.
	 */
	public int getScore(Player player);
}
