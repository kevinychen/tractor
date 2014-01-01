package home;

import javax.swing.JOptionPane;

import view.HumanView;

public class Main
{
    public static void main(String... args) throws Exception
    {
        String name = JOptionPane.showInputDialog("Enter your name:");
        if (name == null) // Cancel option
            System.exit(0);
        new HumanView(name, false).start();
    }
}
