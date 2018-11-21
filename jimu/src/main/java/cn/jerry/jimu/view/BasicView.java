package cn.jerry.jimu.view;

import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.controller.DataController;
import cn.jerry.jimu.listener.*;
import cn.jerry.jimu.model.Block;
import cn.jerry.jimu.model.Position;
import cn.jerry.jimu.util.NumberUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 主面板，含中心面板，下一个方块面板，信息区三部分
 * 提供创建/显示功能
 *
 * @author Jerry
 */
public abstract class BasicView implements IView {
	private MyFrame mainFrame;
	protected MyPanel mainPanel;
	protected int mainPanelBorder;
	protected MyPanel cachePanel;
	protected int[] cachePanelBorder;
	private MyPanel buttonPanel;
	private MyLabel startButton;
	private MyLabel modeButton;
	private MyLabel increaseButton;
	private MyLabel shadowButton;
	private MyLabel scoreValueLabel;
	private MyLabel speedLevelLabel;

	/**
	 * @history 简化信息区，from ver 2.2.8
	 * @history 去除背景图，from ver 2.2.5
	 * @history 增加背景图，透明，from ver 2.2.1
	 * @history 根据方块单元大小，自动调整Frame大小，from ver 2.1.0
	 */
	public BasicView() {
		// 创建面板
		createMainFrame();
		// 自动调整窗口大小
		autoResetFrameSize();
		// 注册监听器
		addListeners();
	}

	private void createMainFrame() {
		mainFrame = new MyFrame("Classic Blocks");
		mainFrame.setLayout(new BorderLayout());

		JPanel panel = new MyPanel();
		panel.setOpaque(true);
		panel.setBackground(ViewProp.PANEL_BACK_COLOR);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout());
		panel.add(createCenterBorder(), BorderLayout.CENTER);
		panel.add(createEastBorder(), BorderLayout.EAST);
		panel.add(createNorthBorder(), BorderLayout.NORTH);
		mainFrame.add(panel);
	}

	private MyPanel createCenterBorder() {
		mainPanel = new MyPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanelBorder = ViewProp.BORDER_THICKNESS;
		mainPanel.setBorder(new LineBorder(ViewProp.FONT_COLOR, mainPanelBorder));
		mainPanel.setBackground(ViewProp.BLOCK_BACK_COLOR);
		return mainPanel;
	}

	private MyPanel createEastBorder() {
		MyPanel panel = new MyPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(0, 20, 0, 0));
		panel.add(createCachePanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private MyPanel createCachePanel() {
		cachePanel = new MyPanel();
		return cachePanel;
	}

	private MyPanel createButtonPanel() {
		buttonPanel = new MyPanel();
		GridLayout buttonLayout = new GridLayout(2, 1);
		buttonLayout.setHgap(0);
		buttonLayout.setVgap(ViewProp.BLOCK_SIZE);
		buttonPanel.setLayout(buttonLayout);

		startButton = new MyLabel("START", ViewProp.CHINESE_FONT);
		buttonPanel.add(startButton);

		MyPanel panel = new MyPanel();
		GridLayout confLayout = new GridLayout(1, 3);
		confLayout.setHgap(ViewProp.BLOCK_SIZE);
		confLayout.setVgap(0);
		panel.setLayout(confLayout);
		buttonPanel.add(panel);

		modeButton = new MyLabel("M", ViewProp.CHINESE_FONT);
		panel.add(modeButton);
		increaseButton = new MyLabel("I", ViewProp.CHINESE_FONT);
		panel.add(increaseButton);
		shadowButton = new MyLabel("S", ViewProp.CHINESE_FONT);
		panel.add(shadowButton);

		return buttonPanel;
	}

	private MyPanel createNorthBorder() {
		MyPanel panel = new MyPanel();
		panel.setBorder(new EmptyBorder(0, 0, 10, 10));

		GridLayout labelLayout = new GridLayout(1, 4);
		panel.setLayout(labelLayout);

		MyLabel scoreTextLabel = new MyLabel("SCORE：", ViewProp.WEST_FONT);
		scoreTextLabel.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(scoreTextLabel);
		scoreValueLabel = new MyLabel("0", ViewProp.WEST_FONT);
		scoreValueLabel.setHorizontalAlignment(JLabel.LEFT);
		panel.add(scoreValueLabel);

		MyLabel speedTextLabel = new MyLabel("SPEED：", ViewProp.WEST_FONT);
		speedTextLabel.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(speedTextLabel);
		speedLevelLabel = new MyLabel(" 1", ViewProp.WEST_FONT);
		speedLevelLabel.setHorizontalAlignment(JLabel.LEFT);
		panel.add(speedLevelLabel);

		return panel;
	}

	/**
	 * @history 根据方块单元大小，自动调整Frame大小，from ver 2.1.0
	 */
	public void autoResetFrameSize() {
		// 计算mainPanel应当具有的大小
		int mainPanelWidthCalc = mainPanelBorder * 2 // mainPanel边框高度
				+ ViewProp.BLOCK_SIZE * ViewProp.MAIN_PANEL_COLS // 单元占用高度
				+ cellGap() * (ViewProp.MAIN_PANEL_COLS - 1); // 网格占用高度
		int mainPanelHeightCalc = mainPanelBorder * 2 // mainPanel边框宽度
				+ ViewProp.BLOCK_SIZE * ViewProp.MAIN_PANEL_ROWS // 单元占用宽度
				+ cellGap() * (ViewProp.MAIN_PANEL_ROWS - 1); // 网格占用高度

		cachePanelBorder = new int[]{0, 0, 0, 0};
		int[] buttonPanelBorder = new int[]{0, 0, 0, 0};
		// 计算blockPanel应当具有的大小
		int cachePanelWidthCalc = ViewProp.BLOCK_SIZE * ViewProp.CACHE_PANEL_COLS
				+ cellGap() * (ViewProp.CACHE_PANEL_COLS - 1);
		int cachePanelHeightCalc = ViewProp.BLOCK_SIZE * ViewProp.CACHE_PANEL_ROWS
				+ cellGap() * (ViewProp.CACHE_PANEL_ROWS - 1);

		mainFrame.pack();
		// 调整cache面板大小
		// 不知道为什么根据计算生成的cachePanel高度总是少2像素，所以这里多减2
		int dltV = mainPanelHeightCalc - cachePanelHeightCalc - buttonPanel.getHeight() - 2;
		if (dltV >= 0) {
			buttonPanelBorder[0] += dltV;
		} else {
			// 增加center行数吧
		}
		int addWidth = 0; // frame需要拉宽
		int dltH = cachePanelWidthCalc - cachePanel.getWidth();
		if (dltH >= 0) {
			addWidth += dltH;
			buttonPanelBorder[1] += dltH / 2 + dltH % 2;
			buttonPanelBorder[3] += dltH / 2;
		} else {
			dltH = -dltH;
			cachePanelBorder[1] += dltH / 2 + dltH % 2;
			cachePanelBorder[3] += dltH / 2;
		}
		EmptyBorder blockBorder = new EmptyBorder(cachePanelBorder[0], cachePanelBorder[1], cachePanelBorder[2],
				cachePanelBorder[3]);
		cachePanel.setBorder(blockBorder);
		cachePanelWidthCalc += cachePanelBorder[1] + cachePanelBorder[3];
		cachePanelHeightCalc += cachePanelBorder[0] + cachePanelBorder[2];
		cachePanel.setSize(cachePanelWidthCalc, cachePanelHeightCalc);
		EmptyBorder buttonBorder = new EmptyBorder(buttonPanelBorder[0], buttonPanelBorder[1], buttonPanelBorder[2],
				buttonPanelBorder[3]);
		buttonPanel.setBorder(buttonBorder);
		// 调整整个窗口大小，高度多增加2个像素，感觉美观
		dltV = mainPanelHeightCalc - mainPanel.getHeight() + 2;
		dltH = mainPanelWidthCalc - mainPanel.getWidth();
		mainFrame.setSize(mainFrame.getWidth() + dltH + addWidth, mainFrame.getHeight() + dltV);

		mainFrame.setLocationRelativeTo(null);// 参数为null可使窗口居中
		mainFrame.setVisible(true);
	}

	/**
	 * 注册按键监听器
	 */
	private void addListeners() {
		this.mainFrame.addKeyListener(new KeyPressedListener());
		this.startButton.addMouseListener(new GameStatusListener());
		this.modeButton.addMouseListener(new BlockModeListener());
		this.increaseButton.addMouseListener(new AutoIncreaseListener());
		this.shadowButton.addMouseListener(new ShadowShowingListener());
		this.speedLevelLabel.addMouseListener(new SpeedLevelListener());
	}

	public MyFrame getMainFrame() {
		return mainFrame;
	}

	public void resetButton() {
		this.modeButton.setForeground(
				DataController.isNormalBlock() ? Color.RED : Color.GREEN);
		this.increaseButton.setForeground(
				DataController.isAutoIncrease() ? Color.GREEN : Color.RED);
		this.shadowButton.setForeground(
				DataController.isShowShadow() ? Color.GREEN : Color.RED);
	}

	public void refreshScore() {
		this.scoreValueLabel.setText(DataController.getScore() + "");
	}

	public void refreshSpeedLevel() {
		this.speedLevelLabel.setText(
				NumberUtil.formatInt(DataController.getSpeedLevel(), " 0"));
	}

	public void changeBlockMode() {
		this.modeButton.setForeground(
				DataController.isNormalBlock() ? Color.RED : Color.GREEN);
	}

	public void changeAutoIncrease() {
		this.increaseButton.setForeground(
				DataController.isAutoIncrease() ? Color.GREEN : Color.RED);
	}

	public void changeShadowShowing() {
		this.shadowButton.setForeground(
				DataController.isShowShadow() ? Color.GREEN : Color.RED);
	}

	public void changeStartButtonText(String text) {
		this.startButton.setText(text);
	}

	/**
	 * 重绘缓存区域
	 */
	public void redrawCachePanel() {
		showCachedBlocks();
	}

	/**
	 * 隐藏预存方块
	 */
	public void hideCachedBlocks() {
		repaintCachePanel(ViewProp.PANEL_BACK_COLOR);
	}

	/**
	 * 显示预存方块
	 */
	public void showCachedBlocks() {
		for (int i = 1; i < DataController.getBlocks().length; i++) {
			if (DataController.getBlocks()[i] == null)
				return;
			int startLine = ViewProp.CACHE_PANEL_ROWS - (ViewProp.BLOCK_UNITS * i + (i - 1));

			Position panelP = null;
			LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
			for (Position cell : DataController.getBlocks()[i].getCells()) {
				panelP = new Position(0, startLine).add(cell);
				cells.put(panelP, DataController.getBlocks()[i].getBgColor());
			}
			repaintCacheCell(cells);
		}
	}

	/**
	 * 重绘游戏区域
	 */
	public void redrawMainPanel() {
		// 显示未消除的单元
		showRemainder();
		// 显示当前方块
		showCurrBlock();
	}

	/**
	 * 显示未消除的单元
	 */
	private void showRemainder() {
		if (DataController.getRemainder() == null) return;
		Color c;
		LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
		for (int y = 0; y < ViewProp.MAIN_PANEL_ROWS; y++) {
			for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
				c = DataController.getRemainder().getColor(x, y);
				cells.put(new Position(x, y), c != null ? c : ViewProp.PANEL_BACK_COLOR);
			}
		}
		repaintMainCell(cells);
	}

	public void hideCurrBlock(boolean force) {
		if (DataController.getBlocks()[0] == null) return;
		redrawBlock(DataController.getBlocks()[0], null, force);
		// 针对穿透型单元
		if (DataController.getBlocks()[0].getSize() == 1) {
			Position p = DataController.getBlocks()[0].getCellsAbsolutePosition()[0];
			Color c = DataController.getRemainder().getColor(p);
			if (c != null) {
				LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
				cells.put(p, c);
				repaintMainCell(cells);
			}
		}
	}

	public void showCurrBlock() {
		if (DataController.getBlocks()[0] == null) return;
		redrawBlock(DataController.getBlocks()[0],
				DataController.getBlocks()[0].getBgColor(), false);
	}

	/**
	 * 显示/隐藏主面板方块
	 *
	 * @param block
	 * @param color
	 * @param forceClearShadow
	 */
	private void redrawBlock(Block block, Color color, boolean forceClearShadow) {
		if (block == null)
			return;

		Position[] cps = block.getCellsAbsolutePosition();
		// 先画阴影cell
		int y;
		LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
		for (Position cp : cps) {
			if (DataController.isShowShadow() || forceClearShadow) {
				y = cp.getY();
				y += (block.getShadow().getY() - block.getBase().getY());
				if (y < ViewProp.MAIN_PANEL_ROWS) {
					cells.put(new Position(cp.getX(), y), color != null
							? ViewProp.BLOCK_SHADOW_COLOR : ViewProp.PANEL_BACK_COLOR);
				}
			}
		}
		repaintMainCell(cells);
		// 再画方块cell
		cells = new LinkedHashMap<Position, Color>();
		for (Position cp : cps) {
			if (cp.getY() < ViewProp.MAIN_PANEL_ROWS) {
				cells.put(cp, color != null ? color : ViewProp.PANEL_BACK_COLOR);
			}
		}
		repaintMainCell(cells);
	}

	public void showClearing(List<Integer> fullRows, int n) {
		if (n < 0) return;
		if (n >= ViewProp.MAIN_PANEL_COLS) {
			n = ViewProp.MAIN_PANEL_COLS - 1;
		}

		LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
		for (Integer y : fullRows) {
			for (int x = 0; x < n; x++) {
				double dlt = n == 0 ? 0 : 150.00 / n;
				int r = 255;
				int g = 105 + new Double((n - x) * dlt).intValue();
				int b = 0;
				Color bg = new Color(r, g, b);
				cells.put(new Position(x, y), bg);
				cells.put(new Position(ViewProp.MAIN_PANEL_COLS - 1 - x, y), bg);
			}
		}
		repaintMainCell(cells);
	}

	public void paintMainPanelByRow(int y, Color c) {
		if (y < 0 || y >= ViewProp.MAIN_PANEL_ROWS) return;

		Color cc;
		LinkedHashMap<Position, Color> cells = new LinkedHashMap<Position, Color>();
		for (int x = 0; x < ViewProp.MAIN_PANEL_COLS; x++) {
			cc = DataController.getRemainder().getColor(x, y);
			cells.put(new Position(x, y), c != null ? c
					: cc != null ? cc : ViewProp.PANEL_BACK_COLOR);
		}
		repaintMainCell(cells);
	}

	/**
	 * 单元格网格间距
	 *
	 * @return
	 */
	protected abstract int cellGap();

	/**
	 * 清空缓存面板
	 *
	 * @param c
	 */
	protected abstract void repaintCachePanel(Color c);

	/**
	 * 重绘缓存面板的单元
	 *
	 * @param cells
	 */
	protected abstract void repaintCacheCell(LinkedHashMap<Position, Color> cells);

	/**
	 * 清空主面板
	 *
	 * @param c
	 */
	protected abstract void repaintMainPanel(Color c);

	/**
	 * 重绘主面板的单元
	 *
	 * @param cells
	 */
	protected abstract void repaintMainCell(LinkedHashMap<Position, Color> cells);

}