package cn.jerry.jimu.constant;

import java.awt.Color;
import java.awt.Font;

public class ViewProp {
	/*
	 * View 相关设定
	 */
	// 单元的大小（像素数）,不要小于12
	public static final int BLOCK_SIZE = 24;
	// 方块（正方形）行数列数
	public static final int BLOCK_UNITS = 4;
	// 缓存的方块数量
	public static final int CACHED_BLOCKS = 3;
	// 缓存池行数
	public static final int CACHE_PANEL_ROWS = BLOCK_UNITS * CACHED_BLOCKS + CACHED_BLOCKS - 1;
	// 缓存池列数
	public static final int CACHE_PANEL_COLS = BLOCK_UNITS;
	// 主面板行数
	public static final int MAIN_PANEL_ROWS = 20;
	// 主面板列数
	public static final int MAIN_PANEL_COLS = 10;
	// 主面板网格间距
	public static final int GRID_GAP = 1;
	// 主面板边框厚度
	public static final int BORDER_THICKNESS = 2;
	// 面板默认颜色
	public static final Color PANEL_BACK_COLOR_DEFAULT = new Color(0, 0, 0);
	// 面板背景色
	public static final Color PANEL_BACK_COLOR = Color.DARK_GRAY;
	// 方块背景色
	public static final Color BLOCK_BACK_COLOR = Color.BLACK;
	// 阴影颜色
	public static final Color BLOCK_SHADOW_COLOR = Color.GRAY;
	// 7种方块颜色
	public static final Color BLOCK_O_COLOR = Color.YELLOW;
	public static final Color BLOCK_I_COLOR = Color.CYAN;
	public static final Color BLOCK_L_COLOR = Color.BLUE;
	public static final Color BLOCK_Z_COLOR = Color.GREEN;
	public static final Color BLOCK_T_COLOR = Color.RED;
	// 自动增加方块的颜色
	public static final Color BLOCK_ADD_COLOR = Color.WHITE;
	// 自动增加方块时，最多空缺个数
	public static final int ADD_ROW_MAX_BLANK_NUM = 4;
	// 字体颜色
	public static final Color FONT_COLOR = Color.GREEN;
	// 中文字体
	public static final Font CHINESE_FONT = new Font("Droid Sans Mono Bold", Font.BOLD,
	        (BLOCK_SIZE * 4 / 5 + 1));
	// 西文字体
	public static final Font WEST_FONT = new Font("Droid Sans Mono Bold", Font.BOLD,
	        (BLOCK_SIZE * 3 / 5 + 1));

	/**
	 * 检查位置是否超出主面板边界
	 * 
	 * @param x 位置x
	 * @param y 位置y
	 * @param checkTop 是否检查上边界
	 * @return 是否超边界
	 */
	public static boolean isPositionOutOfMainPanel(int x, int y, boolean checkTop) {
		if (x < 0) return true;
		if (x >= ViewProp.MAIN_PANEL_COLS) return true;
		if (y < 0) return true;
		if (checkTop && y >= ViewProp.MAIN_PANEL_ROWS) return true;
		return false;
	}
}