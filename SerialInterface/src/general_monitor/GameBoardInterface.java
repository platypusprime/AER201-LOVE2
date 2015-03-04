package general_monitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameBoardInterface {

	private final JPanel panel;
	// private Dimension playerSize;
	// private Point playerPosition;
	// private int playerOrientation;

	public static final int FIELD_WIDTH = 16000;
	public static final int FIELD_LENGTH = 18000;
	public static final int CELL_LENGTH = 2000;
	public static final int BOARD_LENGTH = 3700;
	public static final int BOARD_EXTRUSION = 60;
	public static final int H_HOPPER_SEPARATION = 1225;
	public static final int V_HOPPER_SEPARATION = 2160;
	public static final int HOPPER_COLUMN_DIAM = 480;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(new GameBoardInterface().getUI());
		frame.setVisible(true);
		frame.pack();
	}

	public GameBoardInterface() {

		panel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g.clearRect(0, 0, panel.getWidth(), panel.getHeight());
				paintGrid(g);
				paintObjects(g);
				paintPlayer(g);
				// TODO
			}
		};
		panel.setPreferredSize(new Dimension(800, 800 / 8 * 9));
		// playerSize = new Dimension(400, 400);
		// playerPosition = new Point(0, FIELD_WIDTH);
	}

	/**
	 * Paints the walls and underlying grid of the board.
	 * 
	 * @param g
	 *            Graphics object to paint
	 */
	private void paintGrid(Graphics g) {

		int displayWidth = panel.getWidth();
		int displayLength = panel.getHeight();

		g.setColor(Color.BLACK);

		// draw gridLines
		for (int i = CELL_LENGTH; i < FIELD_WIDTH; i += CELL_LENGTH) {
			int location = i * displayWidth / FIELD_WIDTH;

			// center line
			if (i == CELL_LENGTH * 4)
				g.setColor(Color.RED);

			// draw vertical line
			g.drawLine(location, 0, location, displayLength);
			// return color
			if (i == CELL_LENGTH * 4)
				g.setColor(Color.BLACK);
		}

		// draw gridLines
		for (int i = CELL_LENGTH; i < FIELD_LENGTH; i += CELL_LENGTH) {
			int location = i * displayLength / FIELD_LENGTH;

			// draw horizontal line
			g.drawLine(0, location, displayWidth, location);

		}

	}

	/**
	 * Paints the location of the hoppers and board
	 * 
	 * @param g
	 *            Graphics object to paint
	 */
	private void paintObjects(Graphics g) {

		// paint board
		g.setColor(Color.RED);
		g.fillRect(scaleToWindow((FIELD_WIDTH - BOARD_LENGTH) / 2), 0,
				scaleToWindow(BOARD_LENGTH), scaleToWindow(BOARD_EXTRUSION));

		// paint hopper locations
		g.setColor(Color.BLUE);
		int startX = (FIELD_WIDTH - H_HOPPER_SEPARATION * 6) / 2;
		int startY = (FIELD_LENGTH - V_HOPPER_SEPARATION * 3) / 2;

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 4; j++) {
				g.fillOval(scaleToWindow(startX + i * H_HOPPER_SEPARATION) - 3,
						scaleToWindow(startY + j * V_HOPPER_SEPARATION) - 3, 6,
						6);
			}
		}

		// TODO paint actual hoppers
	}

	/**
	 * Paints the robot position/orientation
	 * 
	 * @param g
	 *            Graphics object to paint
	 */
	private void paintPlayer(Graphics g) {
		// TODO
	}

	private int scaleToWindow(int i) {
		return i * panel.getWidth() / FIELD_WIDTH;
	}

	public JPanel getUI() {
		return panel;
	}
}
