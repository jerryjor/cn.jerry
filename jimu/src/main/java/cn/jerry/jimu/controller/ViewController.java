package cn.jerry.jimu.controller;

import cn.jerry.jimu.model.StaticBlock;
import cn.jerry.jimu.view.DrawingView;
import cn.jerry.jimu.view.IView;
import cn.jerry.jimu.model.Direction;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ViewController {
	private static final IView VIEW = new DrawingView();

	/**
	 * 初始化游戏面板
	 */
	public static void initView() {
		// 预生成方块
		DataController.clearBlocks();
		reCreateAllBlocks();
		// 初始化面板
		DataController.clearRemainder();

		// 初始化按钮
		VIEW.resetButton();

		refreshStartButtonText();
		refreshSpeedLevel();
		refreshScore();
	}

	/**
	 * 重新生成所有方块
	 *
	 * @history 预先显示三个方块，from ver 2.1.0
	 */
	private static void reCreateAllBlocks() {
		// 隐藏当前方块组
		VIEW.hideCachedBlocks();
		// 生成3个方块
		for (int i = 1; i < DataController.getBlocks().length; i++) {
			DataController.getBlocks()[i] = new StaticBlock(DataController.isNormalBlock());
		}
		// 显示方块组
		VIEW.showCachedBlocks();
	}

	/**
	 * 生成新的方块
	 */
	public static void createNewBlock() {
		// 隐藏当前方块组
		VIEW.hideCachedBlocks();
		// 把现有方块往前推
		for (int i = 0, size = DataController.getBlocks().length; i < size - 1; i++) {
			DataController.getBlocks()[i] = DataController.getBlocks()[i + 1];
		}
		// 生成新方块
		DataController.getBlocks()[DataController.getBlocks().length - 1] = new StaticBlock(DataController.isNormalBlock());
		// 显示方块组
		VIEW.showCachedBlocks();

		// 第一个方块准备下落
		DataController.getBlocks()[0].moveToMainPanelTop();
		// 刷新主面板
		VIEW.redrawMainPanel();
	}

	/**
	 * 向指定方向移动当前方块
	 *
	 * @param d 方向
	 * @return 是否已移动
	 */
	public static boolean moveCurrBlock(Direction d) {
		VIEW.hideCurrBlock(false);
		boolean done = DataController.getBlocks()[0].tryMove(d);
		VIEW.showCurrBlock();
		return done;
	}

	/**
	 * 旋转当前模块
	 *
	 * @return 是否成功旋转
	 * @history 增加旋转后适当平移，防止贴边时无法旋转，from ver 1.3.0
	 */
	public static boolean rotateCurrBlock() {
		VIEW.hideCurrBlock(false);
		boolean done = DataController.getBlocks()[0].tryRotate(true);
		VIEW.showCurrBlock();
		return done;
	}

	/**
	 * 显示清除动画（变色）
	 *
	 * @param fullRows
	 * @param n
	 * @history 动画由闪动改为变色，from ver 2.2.0
	 * @history 显示清除动画（闪动），from ver 1.3.0
	 */
	public static void showClearing(List<Integer> fullRows, int n) {
		VIEW.showClearing(fullRows, n);
	}

	/**
	 * 底部增加一行
	 *
	 * @return 是否到达顶部
	 * @history 增加游戏难度，from ver 1.2.0
	 */
	public static boolean addBottomRow() {
		boolean isOverTop = DataController.getRemainder().addOneRowToBottom();
		// 判断上方是否有刚落下来的方块
		if (DataController.getBlocks()[0] != null) {
			DataController.getBlocks()[0].tryMove(Direction.UP);
		}
		VIEW.redrawMainPanel();
		return isOverTop;
	}

	/**
	 * 清除最底行
	 */
	public static void removeBottomRow() {
		DataController.getRemainder().removeOneRowFromBottom();
		VIEW.redrawMainPanel();
	}

	/**
	 * 画一行单元格
	 *
	 * @param y
	 * @param c
	 * @history 游戏结束时的动画，from ver 2.2.2
	 */
	public static void paintMainPanelByRow(int y, Color c) {
		VIEW.paintMainPanelByRow(y, c);
	}

	/**
	 * 刷新分数
	 */
	public static void refreshCurrBlock() {
		VIEW.showCurrBlock();
	}

	/**
	 * 刷新分数
	 */
	public static void refreshScore() {
		VIEW.refreshScore();
	}

	/**
	 * 刷新速度等级
	 */
	public static void refreshSpeedLevel() {
		VIEW.refreshSpeedLevel();
	}

	/**
	 * 显示速度等级
	 */
	public static void changeSpeedLevel(boolean raise) {
		DataController.changeStartSpeedLevel(raise);
		refreshSpeedLevel();
	}

	/**
	 * 改变游戏模式
	 *
	 * @history 增加变态模式，from ver 2.0.0
	 */
	public static void changeBlockMode() {
		VIEW.changeBlockMode();
		// 重新生成方块
		reCreateAllBlocks();
	}

	/**
	 * 改变是否自增
	 */
	public static void changeAutoIncrease() {
		VIEW.changeAutoIncrease();
	}

	/**
	 * 改变影子显示
	 *
	 * @history 添加控制是否显示方块阴影，from ver 2.2.1
	 */
	public static void changeShadowShowing() {
		VIEW.hideCurrBlock(true);
		VIEW.changeShadowShowing();
		VIEW.showCurrBlock();
	}

	/**
	 * 更新开始按钮文本
	 */
	public static void refreshStartButtonText() {
		switch (DataController.getGameStatus()) {
			case STOPED:
				VIEW.changeStartButtonText("START");
				break;
			case STARTED:
				VIEW.changeStartButtonText("PAUSE");
				break;
			case PAUSING:
				VIEW.changeStartButtonText("GO_ON");
				break;
		}
	}

	/**
	 * 游戏结束时，弹窗
	 *
	 * @return 返回用户的选择
	 * @history 该方法不美观，不再使用，from ver 2.2.2
	 * @history 增加游戏结束时弹窗，询问是否再来，from ver 1.3.0
	 */
	public static int gameOverDialog() {
		String message = "游戏结束，本次成绩为：得分" + DataController.getScore() + "，等级"
				+ DataController.getSpeedLevel() + "。";
		String[] options = new String[]{"再来一把", "退出游戏"};
		int[] initialValue = new int[]{0, 1};
		return JOptionPane.showOptionDialog(VIEW.getMainFrame(), message, "GAME OVER",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, initialValue);
	}

	/**
	 * 按下重新开始热键时，弹窗
	 *
	 * @return 返回用户的选择
	 * @history 增加重新开始按钮，from ver 2.2.3
	 */
	public static int gameRestartDialog() {
		String message = "确定放弃当前成绩而重新开始吗？";
		String[] options = new String[]{"按错了", "确定"};
		int[] initialValue = new int[]{0, 1};
		return JOptionPane.showOptionDialog(VIEW.getMainFrame(), message, "GAME RESTART",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, initialValue);
	}

}