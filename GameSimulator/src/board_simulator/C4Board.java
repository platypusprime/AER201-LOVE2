package board_simulator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class C4Board {

	public static final int MAX_COLUMN = 7;
	public static final int MAX_ROW = 6;

	private static final int DROP_SCORE = 1;
	private static final int CONNECT_FOUR_BONUS = 4;
	private static final int CONNECT_FOUR_PENALTY = -2;

	private static final int CIRCLE_RADIUS = 25;
	private static final int CIRCLE_SPACING = 5;

	private BoardCell[][] board;
	private int[] scores = { 0, 0 };
	private JPanel boardUI;

	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		final C4Board board = new C4Board();
		final JPanel boardUI = board.getUI();
		frame.add(boardUI);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		frame.addMouseListener(new MouseListener() {

			BoardCell nextColor = BoardCell.WHITE;

			@Override
			public void mouseReleased(MouseEvent e) {

				Point p = e.getLocationOnScreen();
				SwingUtilities.convertPointFromScreen(p, boardUI);
				board.drop(p.x / (CIRCLE_RADIUS + CIRCLE_SPACING), nextColor);

				if (nextColor == BoardCell.BLACK)
					nextColor = BoardCell.WHITE;
				else
					nextColor = BoardCell.BLACK;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// unused
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// unused
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// unused
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// unused
			}
		});
	}

	public C4Board() {

		// initialize board
		board = new BoardCell[MAX_COLUMN][MAX_ROW];
		resetBoard();
		makeUI();
	}

	public void makeUI() {
		boardUI = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {

				g.clearRect(0, 0, getWidth(), getHeight());
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g.setColor(Color.BLACK);
				g.drawString(String.format("W: %d", scores[0]), CIRCLE_SPACING
						* 3 + CIRCLE_RADIUS * 2, CIRCLE_SPACING * (MAX_ROW + 3)
						+ CIRCLE_RADIUS * MAX_ROW);

				g.drawString(String.format("B: %d", scores[1]), CIRCLE_SPACING
						* 5 + CIRCLE_RADIUS * 4, CIRCLE_SPACING * (MAX_ROW + 3)
						+ CIRCLE_RADIUS * MAX_ROW);

				for (int column = 0; column < MAX_COLUMN; column++) {
					for (int row = 0; row < MAX_ROW; row++) {

						int x = CIRCLE_SPACING + column
								* (CIRCLE_RADIUS + CIRCLE_SPACING);
						int y = CIRCLE_SPACING + (MAX_ROW - 1 - row)
								* (CIRCLE_RADIUS + CIRCLE_SPACING);

						g.setColor(Color.BLACK);

						switch (board[column][row]) {
						case BLACK:
							g.fillOval(x, y, CIRCLE_RADIUS, CIRCLE_RADIUS);
							break;
						case WHITE:
							g.setColor(Color.GRAY.brighter());
							g.fillOval(x, y, CIRCLE_RADIUS, CIRCLE_RADIUS);
							break;
						case EMPTY:
							g.drawOval(x, y, CIRCLE_RADIUS, CIRCLE_RADIUS);
							break;
						default:
							break;
						}
					}
				}
			}

		};

		boardUI.setPreferredSize(new Dimension(CIRCLE_SPACING
				* (MAX_COLUMN + 1) + CIRCLE_RADIUS * MAX_COLUMN, CIRCLE_SPACING
				* (MAX_ROW + 1) + CIRCLE_RADIUS * MAX_ROW + 15));
	}

	private void repaintUI() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				boardUI.repaint();
			}

		});

	}

	public JPanel getUI() {
		return boardUI;
	}

	public BoardCell[][] getBoard() {
		return board;
	}

	public boolean isFull() {

		for (int i = 0; i < MAX_COLUMN; i++)
			if (board[i][MAX_ROW - 1] == BoardCell.EMPTY)
				return false;

		return true;
	}

	public boolean drop(int column, BoardCell color) {

		// input validation
		if (color == BoardCell.EMPTY || column < 0 || column >= MAX_COLUMN)
			return false;

		// find first empty cell
		for (int row = 0; row < MAX_ROW; row++) {
			if (board[column][row] == BoardCell.EMPTY) {
				board[column][row] = color;

				// TODO dropDelay();
				int connectFours = countSets(board, column, row, color)[3];
				scores[color.num()] += DROP_SCORE + connectFours
						* CONNECT_FOUR_BONUS;
				scores[color.opposite().num()] += connectFours
						* CONNECT_FOUR_PENALTY;
				repaintUI();
				return true;
			}
		}
		return false;
	}

	public static int[] countSets(BoardCell[][] board, int column, int row,
			BoardCell color) {

		int sets[] = new int[MAX_COLUMN];
		Arrays.fill(sets, 0);

		// count consecutive on horizontal
		int hCount = 0;
		for (int i = column - 1; i >= 0; i--) {
			if (board[i][row] != color)
				break;
			hCount++;
		}
		for (int i = column + 1; i < MAX_COLUMN; i++) {
			if (board[i][row] != color)
				break;
			hCount++;
		}
		sets[hCount]++;

		// count consecutive on vertical
		int vCount = 0;
		for (int j = row - 1; j >= 0; j--) {
			if (board[column][j] != color)
				break;
			vCount++;
		}
		for (int j = row + 1; j < MAX_ROW; j++) {
			if (board[column][j] != color)
				break;
			vCount++;
		}
		sets[vCount]++;

		// count on positive diagonal
		int pdCount = 0;
		for (int[] p = { column - 1, row - 1 }; p[0] >= 0 && p[1] >= 0;) {
			if (board[p[0]][p[1]] != color)
				break;
			pdCount++;
			p[0]--;
			p[1]--;
		}
		for (int[] p = { column + 1, row + 1 }; p[0] < MAX_COLUMN
				&& p[1] < MAX_ROW;) {
			if (board[p[0]][p[1]] != color)
				break;
			pdCount++;
			p[0]++;
			p[1]++;
		}
		sets[pdCount]++;

		// count on negative diagonal
		int ndCount = 0;
		for (int[] p = { column - 1, row + 1 }; p[0] >= 0 && p[1] < MAX_ROW;) {
			if (board[p[0]][p[1]] != color)
				break;
			ndCount++;
			p[0]--;
			p[1]++;
		}
		for (int[] p = { column + 1, row - 1 }; p[0] < MAX_COLUMN && p[1] >= 0;) {
			if (board[p[0]][p[1]] != color)
				break;
			ndCount++;
			p[0]++;
			p[1]--;
		}
		sets[ndCount]++;

		return sets;
	}

	public void resetBoard() {
		for (int column = 0; column < MAX_COLUMN; column++)
			for (int row = 0; row < MAX_ROW; row++)
				board[column][row] = BoardCell.EMPTY;
	}
}