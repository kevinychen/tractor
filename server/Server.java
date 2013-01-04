package server;

import java.io.IOException;

import model.Game;

public interface Server
{
	/**
	 * Sets the model of this server. Must be called before startServer().
	 */
	public void setGame(Game game);

	/**
	 * Starts the server at the specified port to wait for connections from the
	 * specified number of players.
	 * 
	 * @throws IOException
	 */
	public void startServer(int port, int numPlayers) throws IOException;
}
