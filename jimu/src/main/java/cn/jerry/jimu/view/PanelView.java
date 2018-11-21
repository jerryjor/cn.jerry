package cn.jerry.jimu.view;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.model.Position;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * 主面板，含中心面板，下一个方块面板，信息区三部分
 * 提供创建/显示功能
 * 
 * @author Jerry
 */
public class PanelView extends BasicView {
	private MyPanel[][] mainPanels;
	private MyPanel[][] cachePanels;

	public PanelView() {
		super();
		createCellPanels();
	}

	private void createCellPanels() {
		GridLayout centerLayout = new GridLayout(ViewProp.MAIN_PANEL_ROWS, ViewProp.MAIN_PANEL_COLS);
		centerLayout.setHgap(cellGap());
		centerLayout.setVgap(cellGap());
		this.mainPanel.setLayout(centerLayout);
		this.mainPanels = new MyPanel[ViewProp.MAIN_PANEL_ROWS][ViewProp.MAIN_PANEL_COLS];
		for (int y = ViewProp.MAIN_PANEL_ROWS - 1; y >= 0; y--) {
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				this.mainPanels[y][x] = new MyPanel();
				this.mainPanel.add(this.mainPanels[y][x]);
				// mainPanels[y][x].setOpaque(true);
				this.mainPanels[y][x].setBackground(ViewProp.BLOCK_BACK_COLOR);
			}
		}

		GridLayout cacheLayout = new GridLayout(ViewProp.CACHE_PANEL_ROWS,
		        ViewProp.CACHE_PANEL_COLS);
		cacheLayout.setHgap(cellGap());
		cacheLayout.setVgap(cellGap());
		this.cachePanel.setLayout(cacheLayout);
		this.cachePanels = new MyPanel[ViewProp.CACHE_PANEL_ROWS][ViewProp.CACHE_PANEL_COLS];
		for (int y = ViewProp.CACHE_PANEL_ROWS - 1; y >= 0; y--) {
			for (int x = 0; x < ViewProp.CACHE_PANEL_COLS; x++) {
				this.cachePanels[y][x] = new MyPanel();
				this.cachePanel.add(this.cachePanels[y][x]);
			}
		}
	}

	protected int cellGap() {
		return ViewProp.GRID_GAP;
	}

	protected void repaintCachePanel(Color c) {
		for (MyPanel[] row : this.cachePanels) {
			for (MyPanel cell : row) {
				// 去色
				cell.resetPanel2Default();
			}
		}
	}

	protected void repaintCacheCell(LinkedHashMap<Position, Color> cells) {
		if (cells == null || cells.isEmpty()) return;
		synchronized (this.cachePanels) {
			for (Entry<Position, Color> cell : cells.entrySet()) {
				this.cachePanels[cell.getKey().getY()][cell.getKey().getX()]
				        .setPanelBg(cell.getValue());
			}
		}
	}

	protected void repaintMainPanel(Color c) {
		for (int y = 0; y < ViewProp.MAIN_PANEL_ROWS; y++) {
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				this.mainPanels[y][x].resetPanel2Default();
			}
		}
	}

	protected void repaintMainCell(LinkedHashMap<Position, Color> cells) {
		if (cells == null || cells.isEmpty()) return;
		synchronized (this.mainPanels) {
			for (Entry<Position, Color> cell : cells.entrySet()) {
				this.mainPanels[cell.getKey().getY()][cell.getKey().getX()]
				        .setPanelBg(cell.getValue());
			}
		}
	}

}