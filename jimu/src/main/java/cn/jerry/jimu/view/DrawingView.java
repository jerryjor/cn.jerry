package cn.jerry.jimu.view;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.listener.RepaintListener;
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
public class DrawingView extends BasicView {
	private Graphics2D mainGraphics;
	private int[] mainAreaSize;
	private int[] mainCellSize;
	private Graphics2D cacheGraphics;
	private int[] cacheAreaSize;
	private int[] cacheCellSize;

	/**
	 * @history 简化信息区，from ver 2.2.8
	 * @history 去除背景图，from ver 2.2.5
	 * @history 增加背景图，透明，from ver 2.2.1
	 * @history 根据方块单元大小，自动调整Frame大小，from ver 2.1.0
	 */
	public DrawingView() {
		super();
		calcGraphics();
		addWindowListener();
	}

	private void calcGraphics() {
		mainGraphics = (Graphics2D) mainPanel.getGraphics();
		mainAreaSize = new int[]{
				mainPanel.getWidth() - mainPanelBorder * 2,
				mainPanel.getHeight() - mainPanelBorder * 2
		};
		System.out.println(String.format("mainAreaSize: %dx%d, is%s same as preset: %dx%d", mainAreaSize[0], mainAreaSize[1],
				(mainAreaSize[0] == ViewProp.MAIN_PANEL_COLS * ViewProp.BLOCK_SIZE
						&& mainAreaSize[1] == ViewProp.MAIN_PANEL_ROWS * ViewProp.BLOCK_SIZE ? "" : " NOT"),
				ViewProp.MAIN_PANEL_COLS * ViewProp.BLOCK_SIZE, ViewProp.MAIN_PANEL_ROWS * ViewProp.BLOCK_SIZE));
		mainCellSize = new int[]{
				mainAreaSize[0] / ViewProp.MAIN_PANEL_COLS,
				mainAreaSize[1] / ViewProp.MAIN_PANEL_ROWS
		};
		System.out.println(String.format("mainCellSize: %dx%d, is%s same as preset: %dx%d", mainCellSize[0], mainCellSize[1],
				(mainCellSize[0] == ViewProp.BLOCK_SIZE && mainCellSize[1] == ViewProp.BLOCK_SIZE ? "" : " NOT"),
				ViewProp.BLOCK_SIZE, ViewProp.BLOCK_SIZE));

		cacheGraphics = (Graphics2D) cachePanel.getGraphics();
		cacheAreaSize = new int[]{
				cachePanel.getWidth() - cachePanelBorder[1] - cachePanelBorder[3],
				cachePanel.getHeight() - cachePanelBorder[0] - cachePanelBorder[2]
		};
		System.out.println(String.format("cacheAreaSize: %dx%d, is%s same as preset: %dx%d", cacheAreaSize[0], cacheAreaSize[1],
				(cacheAreaSize[0] == ViewProp.CACHE_PANEL_COLS * ViewProp.BLOCK_SIZE
						&& cacheAreaSize[1] == ViewProp.CACHE_PANEL_ROWS * ViewProp.BLOCK_SIZE ? "" : " NOT"),
				ViewProp.CACHE_PANEL_COLS * ViewProp.BLOCK_SIZE, ViewProp.CACHE_PANEL_ROWS * ViewProp.BLOCK_SIZE));
		cacheCellSize = new int[]{
				cacheAreaSize[0] / ViewProp.CACHE_PANEL_COLS,
				cacheAreaSize[1] / ViewProp.CACHE_PANEL_ROWS
		};
		System.out.println(String.format("cacheCellSize: %dx%d, is%s same as preset: %dx%d", cacheCellSize[0], cacheCellSize[1],
				(cacheCellSize[0] == ViewProp.BLOCK_SIZE && cacheCellSize[1] == ViewProp.BLOCK_SIZE ? "" : " NOT"),
				ViewProp.BLOCK_SIZE, ViewProp.BLOCK_SIZE));
	}

	private void addWindowListener() {
		this.getMainFrame().addWindowListener(new RepaintListener(this));
	}

	protected int cellGap() {
		return 0;
	}

	protected void repaintCachePanel(Color c) {
		cacheGraphics.setPaint(c);
		cacheGraphics.fillRect(cachePanelBorder[1], cachePanelBorder[0], cacheAreaSize[0], cacheAreaSize[1]);
	}

	protected void repaintCacheCell(LinkedHashMap<Position, Color> cells) {
		if (cells == null || cells.isEmpty()) return;
		synchronized (cacheGraphics) {
			for (Entry<Position, Color> cell : cells.entrySet()) {
				cacheGraphics.setPaint(cell.getValue());
				cacheGraphics.fillRect(cell.getKey().getX() * cacheCellSize[0] + 1 + cachePanelBorder[1],
						cacheAreaSize[1] - ((cell.getKey().getY() + 1) * cacheCellSize[1]) + 1 + cachePanelBorder[0],
						cacheCellSize[0] - 2, cacheCellSize[1] - 2);
			}
		}
	}

	protected void repaintMainPanel(Color c) {
		mainGraphics.setPaint(c);
		mainGraphics.fillRect(mainPanelBorder, mainPanelBorder, mainAreaSize[0], mainAreaSize[1]);
	}

	protected void repaintMainCell(LinkedHashMap<Position, Color> cells) {
		if (cells == null || cells.isEmpty()) return;
		synchronized (mainGraphics) {
			for (Entry<Position, Color> cell : cells.entrySet()) {
				mainGraphics.setPaint(cell.getValue());
				mainGraphics.fillRect(cell.getKey().getX() * mainCellSize[0] + 1 + mainPanelBorder,
						mainAreaSize[1] - ((cell.getKey().getY() + 1) * mainCellSize[1]) + 1 + mainPanelBorder,
						mainCellSize[0] - 2, mainCellSize[1] - 2);
			}
		}
	}

}