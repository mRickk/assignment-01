package pcd.ass01;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView implements ChangeListener, ActionListener {
	private final JFrame frame;
	private final BoidsPanel boidsPanel;
	private final JSlider cohesionSlider, separationSlider, alignmentSlider;
	private final JButton startButton, stopButton;
	private final BoidsModel model;
	private final int width, height;
	private final BoidsSimulator simulator;

	public BoidsView(BoidsModel model, BoidsSimulator simulator, int width, int height) {
		this.model = model;
		this.width = width;
		this.height = height;
		this.simulator = simulator;

		frame = new JFrame("Boids Simulation");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);
		boidsPanel = new BoidsPanel(this, model);
		cp.add(BorderLayout.CENTER, boidsPanel);
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());

		JPanel buttonsPanel = new JPanel();
		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		buttonsPanel.add(startButton);
		buttonsPanel.add(stopButton);

		JPanel slidersPanel = new JPanel();
		slidersPanel.setLayout(new GridLayout(3, 2));

		cohesionSlider = makeSlider();
		separationSlider = makeSlider();
		alignmentSlider = makeSlider();

		slidersPanel.add(new JLabel("Separation"));
		slidersPanel.add(separationSlider);
		slidersPanel.add(new JLabel("Alignment"));
		slidersPanel.add(alignmentSlider);
		slidersPanel.add(new JLabel("Cohesion"));
		slidersPanel.add(cohesionSlider);

		controlPanel.add(BorderLayout.CENTER, slidersPanel);
		controlPanel.add(BorderLayout.SOUTH, buttonsPanel);

		cp.add(BorderLayout.SOUTH, controlPanel);
		frame.setContentPane(cp);

		frame.setVisible(true);
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(10, new JLabel("1"));
		labelTable.put(20, new JLabel("2"));
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		slider.addChangeListener(this);
		return slider;
	}

	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			model.setCohesionWeight(0.1*val);
		} else {
			var val = alignmentSlider.getValue();
			model.setAlignmentWeight(0.1*val);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startButton) {
			simulator.startSimulator();
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		}
		if (e.getSource() == stopButton) {
			simulator.stopSimulator();
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}