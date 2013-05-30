package client;

import java.io.IOException;

public class DummyClient extends Client
{
    public DummyClient(String name)
    {
        super(name);
    }

    public void startActions()
    {
        delay(5000);
        try
        {
            connect(3003, new byte[]
            { 127, 0, 0, 1 });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void processMessage(String... data)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void showNotification(String notification)
    {
        // TODO Auto-generated method stub

    }

    private void delay(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
