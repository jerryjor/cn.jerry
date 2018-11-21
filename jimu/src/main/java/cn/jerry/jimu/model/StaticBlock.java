package cn.jerry.jimu.model;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.util.NumberUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 方块，由4*4的方块单元组成
 * 提供方法：旋转，平移，是否已到最左边，最右边，最底部
 * 
 * @author Jerry
 */
public class StaticBlock extends Block {
	private static final long serialVersionUID = 1L;

	private static Map<Color, Integer[]> normalDefs = new HashMap<>();
	static {
		normalDefs.put(ViewProp.BLOCK_O_COLOR, new Integer[] { 1, 1, 1, 2, 2, 1, 2, 2 });
		normalDefs.put(ViewProp.BLOCK_I_COLOR, new Integer[] { 1, 0, 1, 1, 1, 2, 1, 3 });
		normalDefs.put(ViewProp.BLOCK_L_COLOR, new Integer[] { 1, 3, 1, 2, 1, 1, 2, 1 });
		normalDefs.put(ViewProp.BLOCK_Z_COLOR, new Integer[] { 1, 0, 1, 1, 2, 1, 2, 2 });
		normalDefs.put(ViewProp.BLOCK_T_COLOR, new Integer[] { 0, 2, 1, 2, 2, 2, 1, 1 });
	}

	private StaticBlock() {
		super();
	}

	public StaticBlock(boolean normal) {
		super();
		setBasePosition(new Position(0, 0));
		if (normal) {
			this.initNormalCells();
		} else {
			this.initAbnormalCells();
		}
	}

	private void initNormalCells() {
		setSize(4);
		// 随机选取一种方块
		int num = NumberUtil.getRandomNum(13);
		Color bgc = chooseColor(num);

		// 随机旋转次数
		int rotateTimes = 0;
		if (ViewProp.BLOCK_I_COLOR == bgc || ViewProp.BLOCK_Z_COLOR == bgc) {
			rotateTimes = NumberUtil.getRandomNum(2);
		} else if (ViewProp.BLOCK_L_COLOR == bgc || ViewProp.BLOCK_T_COLOR == bgc) {
			rotateTimes = NumberUtil.getRandomNum(4);
		}

		// 是否翻转
		boolean needFlip = false;
		if (ViewProp.BLOCK_L_COLOR == bgc || ViewProp.BLOCK_Z_COLOR == bgc) {
			needFlip = NumberUtil.getRandomNum(2) == 1;
		}

		// 根据定义初始化单元
		Integer[] d = normalDefs.get(bgc);
		for (int i = 0; i < getSize(); i++) {
			getCells()[i] = new Position(d[i * 2], d[i * 2 + 1]);
		}
		for (int i = 0; i < rotateTimes; i++) {
			rotate(true);
		}
		if (needFlip) flip();

		setBgColor(needFlip ? bgc.darker() : bgc.brighter());
	}

	private void initAbnormalCells() {
		// 随机选取一种方块
		int num = NumberUtil.getRandomNum(26);
		Integer[] d = chooseRandomDef(num);
		setSize(d.length / 2);

		// 根据定义初始化单元
		for (int i = 0; i < getSize(); i++) {
			getCells()[i] = new Position(d[i * 2], d[i * 2 + 1]);
		}

		// 随机旋转
		for (int i = 0, rotateTimes = NumberUtil.getRandomNum(4); i < rotateTimes; i++) {
			rotate(true);
		}

		// 随机翻转
		if (NumberUtil.getRandomNum(2) == 1) flip();

		setBgColor(chooseColor(num));
	}

	private Integer[] chooseRandomDef(int num) {
		num = num % 26;
		switch (num) {
		case 1:
			return new Integer[] { 1, 0, 1, 1, 1, 2, 2, 1, 2, 2 };
		case 2:
		case 3:
			return new Integer[] { 1, 0, 1, 1, 1, 2, 2, 2, 3, 2 };
		case 4:
		case 5:
			return new Integer[] { 0, 1, 1, 1, 2, 1, 3, 1, 3, 2 };
		case 6:
		case 7:
			return new Integer[] { 0, 1, 1, 1, 2, 1, 3, 1, 2, 2 };
		case 8:
		case 9:
			return new Integer[] { 0, 0, 1, 0, 1, 1, 1, 2, 2, 2 };
		case 10:
		case 11:
			return new Integer[] { 0, 0, 1, 0, 1, 1, 1, 2, 2, 1 };
		case 12:
		case 13:
			return new Integer[] { 0, 1, 1, 1, 1, 2, 2, 2, 3, 2 };
		case 14:
		case 15:
			return new Integer[] { 0, 0, 1, 0, 1, 1, 2, 1, 2, 2 };
		case 16:
		case 17:
			return new Integer[] { 0, 2, 1, 0, 1, 1, 1, 2, 2, 2 };
		case 18:
		case 19:
			return new Integer[] { 0, 1, 1, 0, 1, 1, 1, 2, 2, 1 };
		case 20:
		case 21:
			return new Integer[] { 0, 0, 0, 1, 1, 1, 2, 1, 2, 0 };
		case 22:
			return new Integer[] { 0, 1, 1, 1, 2, 1 };
		case 23:
			return new Integer[] { 1, 2, 1, 1, 2, 1 };
		case 24:
			return new Integer[] { 1, 1, 2, 1 };
		default:
			return new Integer[] { 1, 1 };
		}
	}

	@Override
	protected Block createBlankInstance() {
		return new StaticBlock();
	}
}