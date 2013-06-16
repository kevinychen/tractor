package view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.GameProperties;
import client.Client;

public class GamePropertiesForm extends JFrame
{
    private static final long serialVersionUID = 1L;

    private JComboBox<Integer> numDecksMenu;
    private JCheckBox find_a_friendSelect;

    private JButton OKButton;
    private JButton cancelButton;

    public GamePropertiesForm(JFrame parent, final Client client)
    {
        setTitle("Start new game.");
        setResizable(false);

        /* Set option for number of decks */
        Integer[] numDecksOptions =
        { 1, 2, 3, 4 };
        numDecksMenu = new JComboBox<Integer>(numDecksOptions);
        numDecksMenu.setSelectedIndex(1);
        numDecksMenu.setSize(100, 20);

        /* Set option to play 'find a friend' version */
        find_a_friendSelect = new JCheckBox();

        OKButton = new JButton("OK");
        OKButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                GameProperties properties = new GameProperties();
                properties.numDecks = (Integer) numDecksMenu.getSelectedItem();
                properties.find_a_friend = find_a_friendSelect.isSelected();
                client.requestStartGame(properties);
                dispose();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

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

        JLabel numDecksCaption = new JLabel("Number of decks:");
        JLabel find_a_friendCaption = new JLabel(
                "Check to play 'find a friend' version:");
        JLabel warningCaption = new JLabel(
                "Warning: this will end the current game.");
        warningCaption.setBorder(new EmptyBorder(10, 10, 10, 10));

        Group numDecksSequentialGroup = layout.createSequentialGroup()
                .addComponent(numDecksCaption).addComponent(numDecksMenu);
        Group numDecksParallelGroup = layout
                .createParallelGroup(Alignment.BASELINE)
                .addComponent(numDecksCaption).addComponent(numDecksMenu);
        Group find_a_friendSequentialGroup = layout.createSequentialGroup()
                .addComponent(find_a_friendCaption)
                .addComponent(find_a_friendSelect);
        Group find_a_friendParallelGroup = layout
                .createParallelGroup(Alignment.BASELINE)
                .addComponent(find_a_friendCaption)
                .addComponent(find_a_friendSelect);
        Group dataParallelGroup = layout.createParallelGroup()
                .addGroup(numDecksSequentialGroup)
                .addGroup(find_a_friendSequentialGroup);
        Group dataSequentialGroup = layout.createSequentialGroup()
                .addGroup(numDecksParallelGroup)
                .addGroup(find_a_friendParallelGroup);

        Group buttonsSequentialGroup = layout.createSequentialGroup()
                .addComponent(OKButton).addComponent(cancelButton);
        Group buttonsParallelGroup = layout
                .createParallelGroup(Alignment.BASELINE).addComponent(OKButton)
                .addComponent(cancelButton);

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
                .addGroup(dataParallelGroup).addComponent(warningCaption)
                .addGroup(buttonsSequentialGroup));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(dataSequentialGroup).addComponent(warningCaption)
                .addGroup(buttonsParallelGroup));

        panel.setLayout(layout);
        add(panel);
        pack();
    }
}
