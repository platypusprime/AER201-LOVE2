package general_monitor;

import javax.swing.JFrame;

import board_simulator.BoardCell;
import board_simulator.C4Board;

public class SerialInterface {

	private JFrame frame = new JFrame();
	private C4Board board = new C4Board();

	public SerialInterface() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(board.getUI());
		frame.setVisible(true);
		frame.pack();
	}

	protected void handleEvent(String data) {
		if (data == null)
			return;
		String[] split = data.split("-");
		switch (split[0]) {
		case "board_drop":
			board.drop(Integer.parseInt(split[1]), BoardCell.valueOf(split[2]));
			break;
		default:
			break;
		}
	}
}
