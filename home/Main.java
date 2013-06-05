package home;

import view.DummyView;
import view.HumanView;

public class Main
{
    public static void main(String... args) throws Exception
    {
        HumanView view = new HumanView("Kevin");
        view.start();

        new DummyView("dummy 1").start();
        new DummyView("dummy 2").start();
        new DummyView("dummy 3").start();
    }
}
