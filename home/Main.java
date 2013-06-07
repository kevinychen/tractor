package home;

import view.DummyView;
import view.HumanView;

public class Main
{
    public static void main(String... args) throws Exception
    {
        HumanView view = new HumanView("Kevin");
        view.start();

        new HumanView("dummy 1").start();
        new HumanView("dummy 2").start();
        new HumanView("dummy 3").start();
    }
}
