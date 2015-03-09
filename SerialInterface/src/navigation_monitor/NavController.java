package navigation_monitor;

import general_monitor.SerialMonitor;
import gnu.io.SerialPortEvent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class NavController extends SerialMonitor implements ActionListener {

	private static final int SET_RSPD_F = 1000;
	private static final int SET_RSPD_R = 2000;
	private static final int SET_LSPD_F = 3000;
	private static final int SET_LSPD_R = 4000;
	private static final int RECALIBRATE = 5000;

	private static final int INCREMENT = 15;

	// GUI components
	private JRadioButton[] sensorButtons;
	private JLabel[] sensorLabels;
	private JButton rSpdIncButton, rSpdDecButton, rSpdSetButton, lSpdIncButton, lSpdDecButton,
			lSpdSetButton, calibrateButton;
	private JTextField rSpdField, lSpdField;

	// serial communication fields
	private String nextCmd = "NOCMD";
	private int numIn = 0;

	public static void main(String[] args) {

		// set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// instantiate controller
		new NavController();
	}

	public NavController() {

		createAndShowGUI();
		initialize();

	}

	private void createAndShowGUI() {

		// make window
		JFrame frame = new JFrame("Nav Controller");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridBagLayout());

		// make sensor UI
		JPanel sensorPanel = new JPanel();
		sensorPanel.setLayout(new GridLayout(2, 6));
		((GridLayout) sensorPanel.getLayout()).setHgap(15);
		sensorButtons = new JRadioButton[6];
		for (int i = 0; i < 6; i++) {
			sensorButtons[i] = new JRadioButton();
			sensorButtons[i].setSelected(false);
			sensorButtons[i].setEnabled(false);
			sensorPanel.add(sensorButtons[i]);
		}
		sensorLabels = new JLabel[6];
		for (int i = 0; i < 6; i++) {
			sensorLabels[i] = new JLabel(Integer.toString(i) + "000");
			sensorPanel.add(sensorLabels[i]);
		}
		frame.add(sensorPanel, new GridBagConstraints(0, 1, 4, 1, 0.5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		calibrateButton = new JButton("RECALIBRATE");
		calibrateButton.addActionListener(this);
		frame.add(calibrateButton, new GridBagConstraints(0, 0, 4, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// make motor control UI
		lSpdIncButton = new JButton("+");
		lSpdIncButton.addActionListener(this);
		frame.add(lSpdIncButton, new GridBagConstraints(1, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		lSpdField = new JTextField();
		restrictFieldToNumbers(lSpdField);
		lSpdField.setText("100");
		frame.add(lSpdField, new GridBagConstraints(1, 3, 1, 1, 0, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		lSpdDecButton = new JButton("-");
		lSpdDecButton.addActionListener(this);
		frame.add(lSpdDecButton, new GridBagConstraints(1, 4, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		lSpdSetButton = new JButton("SET LSPD");
		lSpdSetButton.addActionListener(this);
		frame.add(lSpdSetButton, new GridBagConstraints(0, 3, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		rSpdIncButton = new JButton("+");
		rSpdIncButton.addActionListener(this);
		frame.add(rSpdIncButton, new GridBagConstraints(2, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		rSpdField = new JTextField();
		restrictFieldToNumbers(rSpdField);
		rSpdField.setText("100");
		frame.add(rSpdField, new GridBagConstraints(2, 3, 1, 1, 0, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		rSpdDecButton = new JButton("-");
		rSpdDecButton.addActionListener(this);
		frame.add(rSpdDecButton, new GridBagConstraints(2, 4, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		rSpdSetButton = new JButton("SET RSPD");
		rSpdSetButton.addActionListener(this);
		frame.add(rSpdSetButton, new GridBagConstraints(3, 3, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void restrictFieldToNumbers(JTextField field) {
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {

			private static final String DIGITS_ONLY = "[\\D&&[^-]]";

			@Override
			public void insertString(FilterBypass fb, int off, String str, AttributeSet attr)
					throws BadLocationException {

				fb.insertString(off, str.replaceAll(DIGITS_ONLY, ""), attr);
			}

			@Override
			public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
					throws BadLocationException {
				fb.replace(off, len, str.replaceAll(DIGITS_ONLY, ""), attr);
			}
		});
		field.setDocument(doc);

	}

	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine;
				while ((inputLine = readln()) != null) {
					// System.out.println(nextCmd + ":" + inputLine);
					switch (nextCmd) {
					case "S":
						// get sensor number
						numIn = Integer.parseInt(inputLine);
						nextCmd = "S2";
						break;
					case "S2":
						// get analog sensor input
						sensorLabels[numIn].setText(inputLine);
						nextCmd = "S3";
						break;
					case "S3":
						// get digital sensor input
						sensorButtons[numIn].setSelected(inputLine.equals("B"));
						nextCmd = "NOCMD";
						break;
					default:
						nextCmd = inputLine;
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Object src = e.getSource();

		// sensors need to be recalibrated
		if (src == calibrateButton) {
			write(RECALIBRATE);
		}

		// right motor speed needs to be set
		else if (src == rSpdIncButton || src == rSpdDecButton || src == rSpdSetButton) {
			// clean text box contents
			int rSpd = (rSpdField.getText().startsWith("-") ? -1 : 1)
					* Integer.parseInt(rSpdField.getText().replaceAll("[\\D]", ""));

			if (src == rSpdIncButton)
				rSpd += INCREMENT;
			else if (src == rSpdDecButton)
				rSpd -= INCREMENT;

			// commit changes on UI and robot
			if (rSpd > 0) {
				rSpdField.setText(Integer.toString(Math.min(rSpd, 255)));
				write(SET_RSPD_F + rSpd);
			} else {
				rSpdField.setText(Integer.toString(Math.max(rSpd, -255)));
				write(SET_RSPD_R - rSpd);
			}
		}

		// left motor speed needs to be set
		else {
			// clean text box contents
			int lSpd = (lSpdField.getText().startsWith("-") ? -1 : 1)
					* Integer.parseInt(lSpdField.getText().replaceAll("[\\D]", ""));

			if (src == lSpdIncButton)
				lSpd += INCREMENT;
			else if (src == lSpdDecButton)
				lSpd -= INCREMENT;

			// commit changes on UI and robot
			if (lSpd > 0) {
				lSpdField.setText(Integer.toString(Math.min(lSpd, 255)));
				write(SET_LSPD_F + lSpd);
			} else {
				lSpdField.setText(Integer.toString(Math.max(lSpd, -255)));
				write(SET_LSPD_R - lSpd);
			}
		}
	}
}
