package home;

import client.DummyClient;
import client.HumanClient;

public class Main
{
    public static void main(String... args) throws Exception
    {
        HumanClient client = new HumanClient("Kevin");
        client.addView();

        new Thread()
        {
            public void run()
            {
                new DummyClient("dummy 1").startActions();
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                new DummyClient("dummy 2").startActions();
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                new DummyClient("dummy 3").startActions();
            }
        }.start();
    }
}
