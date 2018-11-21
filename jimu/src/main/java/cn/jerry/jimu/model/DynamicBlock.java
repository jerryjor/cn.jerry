package cn.jerry.jimu.model;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.util.NumberUtil;

import java.awt.*;

/**
 * 方块，由4*4的方块单元组成
 * 提供方法：旋转，平移，是否已到最左边，最右边，最底部
 * 
 * @author Jerry
 */
public class DynamicBlock extends Block {
	private static final long serialVersionUID = 1L;

	private DynamicBlock() {
		super();
	}

	public DynamicBlock(boolean normal) {
		super();
		setSize(4);
		if (!normal) {
			setSize(NumberUtil.getRandomSize());
		}
		generateBlock();
	}

	/**
	 * 产生新方块，并随机旋转
	 * 
	 * @return 新方块
	 */
	public void generateBlock() {
		// 初始化单元
		this.generateBlockCells();
		// 自动居中
		this.autoCenter();
		// 随机旋转
		int rotateTimes = NumberUtil.getRandomNum(4);
		for (int i = 0; i < rotateTimes; i++) {
			this.rotate(false);
		}
		// 随机设置方块颜色
		Color color = chooseColor(NumberUtil.getRandomNum(13));
		setBgColor(color);
	}

	/**
	 * 生成随机方块
	 * 如果变量 maxSize固定为4，那么只会生成正常方块
	 * 
	 * @history 生成不规则方块，from ver 2.0.0
	 */
	private void generateBlockCells() {
		// 从1,1这个单元开始
		Position baseP = new Position(1, 1);
		getCells()[0] = baseP;

		// 向外扩展单元，随机一个方向
		Position newP;
		Direction nextDir;
		int i = 1;
		while (i < getSize()) {
			newP = baseP.clone();
			nextDir = Direction.values()[NumberUtil.getRandomNum(Direction.values().length)];
			newP.add(nextDir);
			if (isPositionAvailable(newP)) {
				getCells()[i] = newP;
				baseP = getNextBaseP(i);
				i++;
			}
		}
	}

	/**
	 * 随机选取下一个单元作为基准
	 * 
	 * @param i
	 * @return
	 */
	private Position getNextBaseP(int i) {
		Position p;
		do {
			p = getCells()[NumberUtil.getRandomNum(i)];
		} while (isDeadPosition(p));
		return p;
	}

	/**
	 * 判断是否为死单元，即四周无空白的单元
	 * 
	 * @param p
	 * @return
	 */
	private boolean isDeadPosition(Position p) {
		int deadNum = 0;
		for (Direction d : Direction.values()) {
			Position p1 = p.clone();
			p1.add(d);
			if (!isPositionAvailable(p1)) {
				deadNum++;
			}
			if (deadNum > 2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断位置是否可用
	 * 
	 * @param p
	 * @return
	 */
	private boolean isPositionAvailable(Position p) {
		// 判断是否越界
		boolean outOfRange = p.getX() < 0 || p.getX() >= ViewProp.BLOCK_UNITS
		        || p.getY() < 0 || p.getY() >= ViewProp.BLOCK_UNITS;
		if (outOfRange) return false;

		// 判断是否已被占用
		for (Position cell : getCells()) {
			if (cell.equals(p)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 自动居中
	 * 
	 * @history 优化方块位置，利于旋转，from ver 2.2.1
	 */
	private void autoCenter() {
		Direction d;
		do {
			// 计算四边空白量
			int left = ViewProp.BLOCK_UNITS, right = left, up = left, down = left;
			for (Position cell : getCells()) {
				left = Math.min(left, cell.getX());
				right = Math.min(right, ViewProp.BLOCK_UNITS - 1 - cell.getX());
				down = Math.min(down, cell.getY());
				up = Math.min(up, ViewProp.BLOCK_UNITS - 1 - cell.getY());
			}
			if (left < 0) {
				d = Direction.RIGHT;
			} else if (right < 0) {
				d = Direction.LEFT;
			} else if (down < 0) {
				d = Direction.UP;
			} else if (up < 0) {
				d = Direction.DOWN;
			} else if (left - right > 1) {
				d = Direction.LEFT;
			} else if (right - left > 1) {
				d = Direction.RIGHT;
			} else if (down - up > 1) {
				d = Direction.DOWN;
			} else if (up - down > 1) {
				d = Direction.UP;
			} else {
				d = null;
			}
			if (d != null) {
				for (Position cell : getCells()) {
					cell.add(d);
				}
			}
		} while (d != null);
	}

	@Override
	protected Block createBlankInstance() {
		return new DynamicBlock();
	}
}