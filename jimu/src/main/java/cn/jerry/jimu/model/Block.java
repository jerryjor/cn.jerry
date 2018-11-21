package cn.jerry.jimu.model;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.controller.DataController;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class Block implements Serializable {
	private static final long serialVersionUID = 1L;

	private Position base = new Position(0, 0);
	private Color bgColor;
	private Position[] cells;
	private boolean inMainPanel = false;
	private Position shadow = new Position(0, 0);

	public Position getBase() {
		return this.base;
	}

	public void setBasePosition(Position position) {
		this.base = position;
	}

	public Position getShadow() {
		return this.shadow;
	}

	public int getSize() {
		return this.cells.length;
	}

	protected void setSize(int size) {
		this.cells = new Position[size];
	}

	public Color getBgColor() {
		return this.bgColor;
	}

	public void setBgColor(Color color) {
		this.bgColor = color;
	}

	public Position[] getCells() {
		return this.cells;
	}

	/**
	 * 获取单元的绝对位置（主面板中实际位置）
	 *
	 * @return
	 */
	public Position[] getCellsAbsolutePosition() {
		Position[] positions = new Position[this.getSize()];
		for (int i = 0; i < this.getSize(); i++) {
			positions[i] = this.cells[i].clone().add(this.base);
		}
		return positions;
	}

	/**
	 * 尝试平移操作
	 *
	 * @param d
	 * @return 是否平移成功
	 */
	public boolean tryMove(Direction d) {
		if (!this.inMainPanel) return false;
		this.move(d);
		if (!this.isPositionAvaliable()) {
			if (this.getSize() == 1 && Direction.DOWN == d) {
				Position p = this.getCellsAbsolutePosition()[0];
				while (p.getY() > 0) {
					p.add(Direction.DOWN);
					if (DataController.getRemainder().getColor(p) == null) {
						return true;
					}
				}
			}
			this.move(d.getAgainstDirection());
			return false;
		}
		this.calcShadowPosition();
		return true;
	}

	/**
	 * 平移操作
	 *
	 * @param d
	 */
	public void move(Direction d) {
		this.base.add(d);
	}

	/**
	 * 尝试旋转操作
	 *
	 * @param clockwise
	 * @return 是否旋转成功
	 */
	public boolean tryRotate(boolean clockwise) {
		if (!this.inMainPanel)
			return false;
		this.rotate(clockwise);
		if (!this.isPositionAvaliable()) {
			// 左移1单元
			this.move(Direction.LEFT);
			if (!this.isPositionAvaliable()) {
				// 还原位置
				this.move(Direction.RIGHT);
				// 右移1单元
				this.move(Direction.RIGHT);
				if (!this.isPositionAvaliable()) {
					// 还原位置
					this.move(Direction.LEFT);
					// 还原旋转
					this.rotate(!clockwise);
					return false;
				}
			}
		}
		this.calcShadowPosition();
		return true;
	}

	/**
	 * 旋转操作
	 *
	 * @param clockwise
	 */
	public void rotate(boolean clockwise) {
		for (Position cell : this.cells) {
			int newX, newY;
			if (clockwise) {
				newX = cell.getY();
				newY = ViewProp.BLOCK_UNITS - 1 - cell.getX();
			} else {
				newX = ViewProp.BLOCK_UNITS - 1 - cell.getY();
				newY = cell.getX();
			}
			cell.setX(newX);
			cell.setY(newY);
		}
	}

	/**
	 * 翻转操作
	 */
	public void flip() {
		for (Position cell : this.cells) {
			cell.setX(ViewProp.BLOCK_UNITS - 1 - cell.getX());
		}
	}

	/**
	 * 计算阴影位置
	 */
	public void calcShadowPosition() {
		if (!this.inMainPanel || !DataController.isShowShadow()) return;

		Position currPosition = this.base.clone();
		if (this.getSize() == 1) {
			this.base.setY(-4);
			while (!this.isPositionAvaliable()) {
				this.move(Direction.UP);
			}
		} else {
			while (this.isPositionAvaliable()) {
				this.move(Direction.DOWN);
			}
			this.move(Direction.UP);
		}
		this.shadow = this.base;
		this.base = currPosition;
	}

	/**
	 * 检查位置可用性
	 *
	 * @return
	 */
	private boolean isPositionAvaliable() {
		Position[] cps = this.getCellsAbsolutePosition();
		for (Position cp : cps) {
			// 出左、右、下边界
			if (ViewProp.isPositionOutOfMainPanel(cp.getX(), cp.getY(), false)) {
				return false;
			}
			// 与remainder重叠
			if (DataController.getRemainder().getColor(cp) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 方块移至主面板顶部
	 */
	public void moveToMainPanelTop() {
		this.base.setX((ViewProp.MAIN_PANEL_COLS - ViewProp.BLOCK_UNITS) / 2 - 1);
		this.base.setY(ViewProp.MAIN_PANEL_ROWS - ViewProp.BLOCK_UNITS / 2);
		this.inMainPanel = true;
		this.calcShadowPosition();
	}

	/**
	 * 将当前方块添加至remainder
	 */
	public Set<Integer> addToRemainder() {
		Set<Integer> lines = new HashSet<>();
		if (!this.inMainPanel) return lines;
		for (Position p : this.getCellsAbsolutePosition()) {
			DataController.getRemainder().markPosition(p, this.bgColor);
			lines.add(p.getY());
		}
		return lines;
	}

	protected Color chooseColor(int num) {
		num = num % 13;
		switch (num) {
			case 1:
			case 2:
				return ViewProp.BLOCK_O_COLOR;
			case 3:
			case 4:
				return ViewProp.BLOCK_T_COLOR;
			case 5:
			case 6:
			case 7:
			case 8:
				return ViewProp.BLOCK_L_COLOR;
			case 9:
			case 10:
			case 11:
			case 12:
				return ViewProp.BLOCK_Z_COLOR;

			default:
				return ViewProp.BLOCK_I_COLOR;
		}
	}

	@Override
	public Block clone() {
		Block newBlock = createBlankInstance();
		newBlock.base = this.base.clone();
		newBlock.bgColor = this.bgColor;
		newBlock.cells = new Position[this.getSize()];
		for (int i = 0; i < this.getSize(); i++) {
			newBlock.cells[i] = this.cells[i].clone();
		}
		return newBlock;
	}

	protected abstract Block createBlankInstance();

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("{\"base\":").append(this.base).append("}\n");
		String[][] pic = new String[4][4];
		for (Position c : cells) {
			pic[c.getY()][c.getX()] = "口";
		}
		for (String[] row : pic) {
			for (int i = 0; i < row.length; i++) {
				s.append(row[i] == null ? "　" : row[i]);
			}
			s.append("\n");
		}
		return s.toString();
	}
}
