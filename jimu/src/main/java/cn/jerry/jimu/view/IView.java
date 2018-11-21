package cn.jerry.jimu.view;

import java.awt.*;
import java.util.List;

public interface IView {
	/**
	 * 主框架
	 *
	 * @return
	 */
	Component getMainFrame();

	/**
	 * 重置按钮
	 */
	void resetButton();

	/**
	 * 刷新分数
	 */
	void refreshScore();

	/**
	 * 刷新速度等级
	 */
	void refreshSpeedLevel();

	/**
	 * 改变游戏模式
	 *
	 * @history 增加变态模式，from ver 2.0.0
	 */
	void changeBlockMode();

	/**
	 * 改变是否自增
	 */
	void changeAutoIncrease();

	/**
	 * 改变影子显示
	 *
	 * @history 添加控制是否显示方块阴影，from ver 2.2.1
	 */
	void changeShadowShowing();

	/**
	 * 更新开始按钮文本
	 */
	void changeStartButtonText(String text);

	/**
	 * 隐藏预存方块
	 */
	void hideCachedBlocks();

	/**
	 * 显示预存方块
	 */
	void showCachedBlocks();

	/**
	 * 重绘游戏区域
	 */
	void redrawMainPanel();

	/**
	 * 隐藏当前方块
	 *
	 * @param force
	 */
	void hideCurrBlock(boolean force);

	/**
	 * 显示当前方块
	 */
	void showCurrBlock();

	/**
	 * 显示清除动画（变色）
	 *
	 * @param fullRows
	 * @param n
	 * @history 动画由闪动改为变色，from ver 2.2.0
	 * @history 显示清除动画（闪动），from ver 1.3.0
	 */
	void showClearing(List<Integer> fullRows, int n);

	/**
	 * 画一行单元格
	 *
	 * @param y
	 * @param c
	 * @history 游戏结束时的动画，from ver 2.2.2
	 */
	void paintMainPanelByRow(int y, Color c);

}
