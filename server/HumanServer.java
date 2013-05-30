package server;

import java.io.IOException;

import view.View;

public class HumanServer extends Server
{
    private View view;
    
    public HumanServer(View view)
    {
        this.view = view;
    }

    @Override
    public void startServer(int port) throws IOException
    {
        super.startServer(port);
        view.createRoom();
    }

    @Override
    public void close()
    {
        super.close();
        view.closeRoom();
    }
}
