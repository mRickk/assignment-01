package pcd.ass01;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class InputView implements ActionListener {
    private final JFrame frame;
    private final JButton startButton;
    private final JTextField boidsCountField;
    private final int width, height;
    private Integer result = null;

    public InputView(int width, int height) {
        this.width = width;
        this.height = height;

        frame = new JFrame("Boids");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);


        JPanel panel = new JPanel();

        panel.add(new JLabel("Number of Boids:"));
        boidsCountField = new JTextField(5);
        boidsCountField.setText("2500");
        panel.add(boidsCountField);

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        panel.add(startButton);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        try {
            result = Integer.parseInt(boidsCountField.getText());
            if (result > 0) {
                startButton.setEnabled(false);
                notifyAll();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Please enter a positive number of boids.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                result = null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Please enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            result = null;
        }
    }

    public synchronized int getBoidCount() throws InterruptedException {
        while (result == null) {
            wait();
        }
        frame.dispose();
        return result;
    }
}