package board_simulator;

public enum BoardCell {
	BLACK, WHITE, EMPTY;

	public BoardCell opposite() {
		switch (this) {
		case BLACK:
			return WHITE;
		case WHITE:
			return BLACK;
		default:
			return null;
		}
	}

	public int num() {
		switch (this) {
		case WHITE:
			return 0;
		case BLACK:
			return 1;
		case EMPTY:
			return 2;
		default:
			return -1;
		}
	}
}