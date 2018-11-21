package cn.jerry.jimu.model;

/**
 * 方向
 * 
 * @author Jerry
 */
public enum Direction {
	LEFT(-1, 0),
	RIGHT(1, 0),
	UP(0, 1),
	DOWN(0, -1);

	private int x;
	private int y;

	private Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/**
	 * 相反的方向
	 * 
	 * @return
	 */
	public Direction getAgainstDirection() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			return null;
		}
	}

}