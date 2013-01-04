package client;

import java.io.IOException;
import java.util.List;

import model.Card;
import view.View;

public interface Client
{
	/**
	 * Tells the client what view methods to call.
	 */
	public void addListener(View.Listener listener);
	
	/**
	 * Connects to the server at the specified port and address.
	 * @throws IOException
	 */
	public void connect(int port, String address) throws IOException;
	
	/**
	 * Passed to the view for calling.
	 */
	public interface Listener
	{
		public void requestNewGame();
		
		public void requestResign();
		
		public void requestDrawCard(Card cardID);
		
		public void requestShowCards(List<Card> cardIDs);
		
		public void requestHideCards(List<Card> cardIDs);
		
		public void requestPlayCards(List<Card> cardIDs);
		
		public void acknowledge();
	}
}
