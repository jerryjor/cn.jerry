package cn.jerry.jimu.model;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.model.Position;
import cn.jerry.jimu.util.NumberUtil;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 未消除的方块单元的库
 *
 * @author Jerry
 */
public class Remainder implements Serializable {
	private static final long serialVersionUID = 1L;

	// 自动增加方块时，最少空白单元数量
	private static final int ADD_ROW_MIN_BLANK = 1;
	// 自动增加方块时，空白单元概率（百分比）
	private static final int ADD_ROW_BLANK_PCT = 40;

	private Color[][] panelColors = new Color[ViewProp.MAIN_PANEL_ROWS][ViewProp.MAIN_PANEL_COLS];
	private int maxY = 0;

	/**
	 * 是否满了
	 *
	 * @return
	 */
	public boolean isFull() {
		return this.maxY >= ViewProp.MAIN_PANEL_ROWS - 1;
	}

	/**
	 * 获取颜色
	 *
	 * @param x
	 * @param y
	 * @return 该位置的颜色
	 */
	public Color getColor(int x, int y) {
		return ViewProp.isPositionOutOfMainPanel(x, y, true) ? null : this.panelColors[y][x];
	}

	/**
	 * 获取颜色
	 *
	 * @param p 位置
	 * @return 该位置的颜色
	 */
	public Color getColor(Position p) {
		return p == null ? null : getColor(p.getX(), p.getY());
	}

	/**
	 * 标记一个位置
	 *
	 * @param p 位置
	 * @param c 颜色
	 */
	public void markPosition(Position p, Color c) {
		if (p == null || ViewProp.isPositionOutOfMainPanel(p.getX(), p.getY(), true)) return;
		this.panelColors[p.getY()][p.getX()] = c;
		if (p.getY() > this.maxY) this.maxY = p.getY();
	}

	/**
	 * 清除满行
	 *
	 * @return 清除的行数
	 */
	public List<Integer> findFullrows(Set<Integer> lines) {
		List<Integer> fullRows = new ArrayList<>();
		boolean allMarked;
		for (Integer y : lines) {
			allMarked = true;
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				if (this.panelColors[y][x] == null) {
					allMarked = false;
					break;
				}
			}
			if (allMarked) fullRows.add(y);
		}
		return fullRows;
	}

	/**
	 * 清除指定的几行
	 *
	 * @param rows
	 */
	public void removeRows(List<Integer> rows) {
		this.maxY = this.maxY - rows.size();
		if (rows.isEmpty()) return;

		Collections.sort(rows);
		int dy = 0;
		for (int y = 0; y < ViewProp.MAIN_PANEL_ROWS; y++) {
			for (int i : rows) {
				if (y + dy == i) dy++;
			}
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				this.panelColors[y][x] = this.getColor(x, y + dy);
			}
		}
	}

	/**
	 * 底部增加一行
	 *
	 * @return 是否到达顶部
	 */
	public boolean addOneRowToBottom() {
		if (isFull()) return true;

		this.maxY++;
		// 现有单元格上移一行
		for (int y = maxY; y > 0; y--) {
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				this.panelColors[y][x] = this.panelColors[y - 1][x];
			}
		}
		// 随机生成一行方块单元
		int added = 0;
		for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
			this.panelColors[0][x] = null;
			int flag = NumberUtil.getRandomNum(ViewProp.MAIN_PANEL_COLS);
			// 控制空格数量
			if (flag > ViewProp.MAIN_PANEL_COLS * ADD_ROW_BLANK_PCT / 100
					&& added < ViewProp.MAIN_PANEL_COLS - ADD_ROW_MIN_BLANK) {
				this.panelColors[0][x] = ViewProp.BLOCK_ADD_COLOR;
				added++;
			}
		}

		return isFull();
	}

	/**
	 * 底部删除一行
	 */
	public void removeOneRowFromBottom() {
		List<Integer> bottomRow = new ArrayList<Integer>();
		bottomRow.add(0);
		removeRows(bottomRow);
	}

}