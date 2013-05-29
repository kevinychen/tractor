package home;

import client.HumanClient;

public class Main
{
	public static void main(String ... args) throws Exception
	{
		HumanClient client = new HumanClient("Kevin");
		client.addView();
	}
}
