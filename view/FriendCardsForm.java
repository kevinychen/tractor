package view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.Card;
import model.FriendCards;
import client.Client;

public class FriendCardsForm extends JFrame
{
    private static final long serialVersionUID = 1L;

    private int numFriends;
    private List<JComboBox<String>> indexMenus;
    private List<JComboBox<String>> valueMenus;
    private List<JComboBox<String>> suitMenus;

    private String[] suitOptions;

    private JButton OKButton;

    public FriendCardsForm(JFrame parent, final Client client, int numDecks,
            final int numFriends)
    {
        setTitle("Select who to be your friend.");
        setResizable(false);

        this.numFriends = numFriends;
        indexMenus = new ArrayList<JComboBox<String>>();
        valueMenus = new ArrayList<JComboBox<String>>();
        suitMenus = new ArrayList<JComboBox<String>>();

        /* Set option for index of card */
        String[] indexOptions = new String[numDecks];
        if (numDecks >= 1)
            indexOptions[0] = "first";
        if (numDecks >= 2)
            indexOptions[1] = "second";
        if (numDecks >= 3)
            indexOptions[2] = "third";
        for (int i = 3; i < numDecks; i++)
            indexOptions[i] = (i + 1) + "th";

        /* Set option for value of card */
        Card.VALUE[] cardValues = Card.VALUE.values();
        String[] valueOptions = new String[cardValues.length];
        for (int i = 0; i < valueOptions.length; i++)
            valueOptions[i] = cardValues[i].toString().toLowerCase()
                    .replace("_", " ");

        /* Set option for suit of card */
        Card.SUIT[] cardSuits = Card.SUIT.values();
        suitOptions = new String[cardSuits.length - 1];
        for (int i = 0; i < suitOptions.length; i++)
            suitOptions[i] = cardSuits[i].toString().toLowerCase() + "s";

        for (int i = 0; i < numFriends; i++)
        {
            indexMenus.add(new JComboBox<String>(indexOptions));
            valueMenus.add(new JComboBox<String>(valueOptions));
            suitMenus.add(new JComboBox<String>(suitOptions));

            final int iCopy = i;
            valueMenus.get(i).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (valueMenus.get(iCopy).getSelectedItem()
                            .equals("small joker")
                            || valueMenus.get(iCopy).getSelectedItem()
                                    .equals("big joker"))
                        suitMenus.get(iCopy).setEnabled(false);
                    else
                        suitMenus.get(iCopy).setEnabled(true);
                }
            });
        }

        OKButton = new JButton("OK");
        OKButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                FriendCards friendCards = new FriendCards();
                int ID = 1;
                for (int i = 0; i < numFriends; i++)
                {
                    Card.VALUE value = Card.VALUE.values()[valueMenus.get(i)
                            .getSelectedIndex()];
                    Card.SUIT suit = (value == Card.VALUE.BIG_JOKER
                            || value == Card.VALUE.SMALL_JOKER ? Card.SUIT.TRUMP
                            : Card.SUIT.values()[suitMenus.get(i)
                                    .getSelectedIndex()]);
                    int index = indexMenus.get(i).getSelectedIndex();
                    friendCards.addFriendCard(new Card(value, suit, ID++),
                            index);
                }
                client.requestFriendCards(friendCards);
                dispose();
            }
        });
        
        /* Disallow closing */
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        arrange();

        /* Place in center of parent frame */
        Point position = parent.getLocation();
        Dimension parentSize = parent.getSize();
        Dimension size = getSize();
        setLocation(position.x + (parentSize.width - size.width) / 2,
                position.y + (parentSize.height - size.height) / 2);
    }

    private void arrange()
    {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateGaps(true);

        JLabel[] introLabels = new JLabel[numFriends];
        JLabel[] ofLabels = new JLabel[numFriends];
        for (int i = 0; i < numFriends; i++)
        {
            introLabels[i] = new JLabel("Card " + (i + 1) + ":");
            ofLabels[i] = new JLabel("of");
        }

        Group[] sequentialGroups = new Group[numFriends];
        Group[] parallelGroups = new Group[numFriends];
        for (int i = 0; i < numFriends; i++)
        {
            sequentialGroups[i] = layout.createSequentialGroup()
                    .addComponent(introLabels[i])
                    .addComponent(indexMenus.get(i))
                    .addComponent(valueMenus.get(i)).addComponent(ofLabels[i])
                    .addComponent(suitMenus.get(i));
            parallelGroups[i] = layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(introLabels[i])
                    .addComponent(indexMenus.get(i))
                    .addComponent(valueMenus.get(i)).addComponent(ofLabels[i])
                    .addComponent(suitMenus.get(i));
        }
        Group dataParallelGroup = layout.createParallelGroup();
        for (Group group : sequentialGroups)
            dataParallelGroup = dataParallelGroup.addGroup(group);
        Group dataSequentialGroup = layout.createSequentialGroup();
        for (Group group : parallelGroups)
            dataSequentialGroup = dataSequentialGroup.addGroup(group);

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
                .addGroup(dataParallelGroup).addComponent(OKButton));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(dataSequentialGroup).addComponent(OKButton));

        panel.setLayout(layout);
        add(panel);
        pack();
    }
}
