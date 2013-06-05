package view;


public class DummyView extends NullView
{
    public DummyView(String name)
    {
        super(name);
    }

    @Override
    public void start()
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                    client.connect(3003, new byte[]
                    { 127, 0, 0, 1 });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
