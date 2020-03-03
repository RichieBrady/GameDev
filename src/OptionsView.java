import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/* Richard Brady
 * 16726839
 * */

// options view class. Shows a jframe window with radio buttons to select difficulty
public class OptionsView {
    private static String difficulty = "Easy"; // default choice

    public OptionsView() {

    }

    public String getDifficulty() {
        return difficulty;
    }

    private class Buttons extends JPanel implements ActionListener {

        private final ArrayList<JRadioButton> difficultyButtons = new ArrayList<>();
        private final String[] difficulty = {"Easy", "Medium", "Hard"};
        private final ArrayList<String> difficultyOptions = new ArrayList<>(Arrays.asList(difficulty));

        private Buttons() {
            super(new BorderLayout());

            for (String difficulty : difficultyOptions) {
                difficultyButtons.add(new JRadioButton(difficulty));
            }

            JPanel difficultyRadioPanel = new JPanel(new GridLayout(3, 1));

            difficultyRadioPanel.setBackground(new Color(45, 48, 53));

            // button group ensures only one radio button can be selected
            ButtonGroup difficultyGroup = new ButtonGroup();

            for (JRadioButton jRadioButton : difficultyButtons) {
                jRadioButton.addActionListener(this);
                difficultyGroup.add(jRadioButton);
                difficultyRadioPanel.add(jRadioButton);
            }

            difficultyGroup.setSelected(difficultyButtons.get(0).getModel(), true);

            JPanel extraPanel = new JPanel(new BorderLayout());

            extraPanel.add(difficultyRadioPanel, BorderLayout.PAGE_START);
            add(extraPanel, BorderLayout.PAGE_START);

            setBackground(new Color(45, 48, 53));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsView.difficulty = e.getActionCommand();
        }
    }

    public void createAndShowOptions() {

        JFrame frame = new JFrame("Options");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Buttons buttons = new Buttons();
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
