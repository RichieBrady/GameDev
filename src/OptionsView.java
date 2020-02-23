import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class OptionsView {
    private static String difficulty = "Easy";

    public OptionsView() {

    }

    public String getDifficulty() {
        return difficulty;
    }

    private class Buttons extends JPanel implements ActionListener {

        private final ArrayList<JRadioButton> standardButtons = new ArrayList<>();
        private final String[] difficulty = {"Easy", "Medium", "Hard"};
        private final ArrayList<String> difficultyOptions = new ArrayList<>(Arrays.asList(difficulty));
        // private final Model model;

        //JComboBox dataRateMenu = new JComboBox<String>(model.getSpecificationDataRates().get(selected));

        private Buttons() {
            super(new BorderLayout());
            //dataRateMenu.addActionListener(this);

            for (String difficulty : difficultyOptions) {
                standardButtons.add(new JRadioButton(difficulty));
            }

            JPanel difficultyRadioPanel = new JPanel(new GridLayout(3, 1));

            difficultyRadioPanel.setBackground(new Color(45, 48, 53));

            ButtonGroup difficultyGroup = new ButtonGroup();
            for (JRadioButton jRadioButton : standardButtons) {
                jRadioButton.addActionListener(this);
                difficultyGroup.add(jRadioButton);
                difficultyRadioPanel.add(jRadioButton);
            }

            difficultyGroup.setSelected(standardButtons.get(0).getModel(), true);
//
//            JPanel protocolsRadioPanel = new JPanel(new GridLayout(1, 2));
//            protocolsRadioPanel.setBackground(new Color(45,48,53));
//
//            JRadioButton tcp = new JRadioButton("TCP");
//            JRadioButton udp = new JRadioButton("UDP");
//            tcp.addActionListener(this);
//            udp.addActionListener(this);

//            ButtonGroup protocolGroup = new ButtonGroup();
//            protocolGroup.add(tcp);
//            protocolGroup.add(udp);
//            protocolGroup.setSelected(tcp.getModel(), true);
//            protocolsRadioPanel.add(tcp);
//            protocolsRadioPanel.add(udp);
//
//            clear = new JButton("Clear");
//            calculate = new JButton("Calculate");

            JPanel extraPanel = new JPanel(new BorderLayout());
//            JPanel clearPanel = new JPanel(new GridLayout(3,1));

//            clearPanel.add(dataRateMenu);
//            clearPanel.add(calculate);
//            clearPanel.add(clear);
//            extraPanel.add(protocolsRadioPanel, BorderLayout.PAGE_START);
            extraPanel.add(difficultyRadioPanel, BorderLayout.PAGE_START);
            add(extraPanel, BorderLayout.PAGE_START);
//            add(clearPanel, BorderLayout.PAGE_END);

            setBackground(new Color(45, 48, 53));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsView.difficulty = e.getActionCommand();
            //System.out.println(OptionsView.difficulty);
           // model.setSpecification(e.getActionCommand());
            //dataRateMenu.removeAllItems();
//            for (String string : model.getSpecificationDataRates().get(difficulty)) {
//                dataRateMenu.addItem(string);
//            }
        }
    }

    public void createAndShowOptions() {

        JFrame frame = new JFrame("Options");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Buttons buttons = new Buttons();
//        JPanel a = new JPanel(new GridLayout(1, 1));
        JPanel b = new JPanel(new GridLayout(1, 1));

        JButton ok = new JButton("OK");

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });

        b.add(buttons);

        frame.add(b, BorderLayout.CENTER);
        frame.add(ok, BorderLayout.PAGE_END);

        int height = 400;
        int width = 380;

        frame.setPreferredSize(new Dimension(width, height));
        frame.setResizable(false);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
