package home;

import view.HumanView;

public class Main
{
    public static void main(String... args) throws Exception
    {
        HumanView view = new HumanView("Kevin");
        view.start();
    }
}
