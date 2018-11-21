package cn.jerry.jimu.model;

import java.io.Serializable;

/**
 * 位置，由水平位置X和垂直位置Y组成
 * 
 * @author Jerry
 */
public class Position implements Serializable {
	private static final long serialVersionUID = 1L;

	// Horizontal
	private int x;
	// vertical
	private int y;

	public Position() {
	}

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 位置叠加
	 * 
	 * @param p
	 *            位置
	 */
	public Position add(Position p) {
		if (p == null)
			return this;

		this.x += p.getX();
		this.y += p.getY();
		return this;
	}

	/**
	 * 位置扣减
	 * 
	 * @param p
	 *            位置
	 */
	public Position minus(Position p) {
		if (p == null)
			return this;

		this.x -= p.getX();
		this.y -= p.getY();
		return this;
	}

	/**
	 * 向指定方向平移一个方块单元
	 * 
	 * @param d
	 *            方向
	 */
	public Position add(Direction d) {
		if (d == null)
			return this;

		this.x += d.getX();
		this.y += d.getY();
		return this;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public Position clone() {
		return new Position(this.x, this.y);
	}

	@Override
	public int hashCode() {
		return (this.x + "." + this.y).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Position) {
			Position p = (Position) obj;
			return this.getX() == p.getX() && this.getY() == p.getY();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "{\"x\":" + this.getX() + ",\"y\":" + this.getY() + "}";
	}
}