package cn.jerry.jimu.controller;

import cn.jerry.jimu.constant.GameOrder;
import cn.jerry.jimu.constant.SleepTime;
import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.model.Direction;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 启动线程，只允许启动一个线程
 * 根据得分提高等级
 * 根据等级提高速度
 *
 * @author Jerry
 */
public class Game extends Thread {
	private static final Game instance = new Game();

	public static void main(String[] args) {
		instance.start();
	}

	/**
	 * 私有构造
	 */
	private Game() {
		DataController.initData();
		ViewController.initView();
	}

	@Override
	public void run() {
		while (true) {
			this.executeOrder();
		}
	}

	/**
	 * 执行命令
	 *
	 * @history 更改为游戏状态为未开始时，不执行动作，from ver 2.2.3
	 * @history 更改DROP命令，从直接掉落改为加速下落，from ver 2.2.3
	 * @history 增加移动/旋转间隔时间，from ver 2.2.0
	 */
	void executeOrder() {
		if (!DataController.isStart()) {
			switch (DataController.getOrder()) {
				case START:
					// 新开游戏
					System.out.println("game start...");
					DataController.startGame();
					ViewController.refreshStartButtonText();
					ViewController.createNewBlock();
					DataController.giveOrder(GameOrder.GO_ON, false);
					break;
				default:
					System.out.println("curr order:" + DataController.getOrder());
					break;
			}
		} else {
			switch (DataController.getOrder()) {

				case OVER:
					gameOver();
					break;

				case PAUSE:
					DataController.pauseGame();
					ViewController.refreshStartButtonText();
					break;

				case GO_ON:
					DataController.startGame();
					ViewController.refreshStartButtonText();

					// 下落
					boolean done = ViewController.moveCurrBlock(Direction.DOWN);
					// 如果无法继续下落，固定当前方块
					if (!done) {
						DataController.giveOrder(GameOrder.FIX, false);
						return;
					}

					// 底部增加方块单元
					DataController.addSteps();
					if (DataController.needAddBottomRow()) {
						DataController.clearSteps();
						boolean isOverTop = ViewController.addBottomRow();
						if (isOverTop) {
							// 如果到顶了，游戏结束
							DataController.giveOrder(GameOrder.OVER, false);
							return;
						}
					}
					break;

				case ROTATE:
					done = ViewController.rotateCurrBlock();
					DataController.addLastSleepTime(ctrlGameSpeed(done ? SleepTime.MOVE_INTERVAL.getCode()
							: 0, DataController.getOrder()));

					DataController.giveOrder(GameOrder.GO_ON, false);
					break;

				case MOVE_LEFT:
				case MOVE_RIGHT:
					done = ViewController.moveCurrBlock(DataController.getOrder() == GameOrder.MOVE_LEFT ? Direction.LEFT
							: Direction.RIGHT);
					DataController.addLastSleepTime(ctrlGameSpeed(done ? SleepTime.MOVE_INTERVAL.getCode()
							: 0, DataController.getOrder()));

					DataController.giveOrder(GameOrder.GO_ON, false);
					break;

				case DROP:
					// 加速下落
					done = ViewController.moveCurrBlock(Direction.DOWN);
					if (done) {
						ctrlGameSpeed(SleepTime.MIN.getCode(), DataController.getOrder());
						DataController.giveOrder(GameOrder.GO_ON, false);
					} else {
						DataController.giveOrder(GameOrder.FIX, false);
					}
					return;

				case FIX:
					DataController.getBlocks()[0].setBgColor(ViewProp.BLOCK_ADD_COLOR);
					ViewController.refreshCurrBlock();
					Set<Integer> lines = DataController.getBlocks()[0].addToRemainder();
					clearFullRows(lines);
					if (DataController.getRemainder().isFull()) {
						DataController.giveOrder(GameOrder.OVER, false);
						return;
					}
					ViewController.createNewBlock();

					// 暂停0.25s
					ctrlGameSpeed(SleepTime.FIX_WAIT.getCode(), DataController.getOrder());
					DataController.giveOrder(GameOrder.GO_ON, false);
					break;
				default:
					break;
			}
		}

		// 游戏速度，与level相关
		long ms = getPauseTime();
		DataController.addLastSleepTime(ctrlGameSpeed(ms - DataController.getLastSleepTime(),
				DataController.getOrder()));
		if (DataController.getLastSleepTime() >= ms) {
			DataController.clearLastSleepTime();
		}
	}

	/**
	 * 控制游戏速度
	 *
	 * @param ms 最大sleep毫秒数
	 * @param curOrder 当前指令
	 * @return 返回实际睡眠时间
	 * @history 增加按键间隔控制，from ver 2.2.0
	 * @history 指令改变时，立即响应，from ver 2.2.0
	 */
	private long ctrlGameSpeed(long ms, GameOrder curOrder) {
		long start = System.nanoTime();
		long now = start;
		while (DataController.getOrder() == curOrder
				// 防止快速切换暂停/继续导致游戏加速
				|| DataController.getOrder() == GameOrder.GO_ON) {
			if (now - start >= ms)
				break;

			// 休眠最小时间
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(SleepTime.MIN.getCode(),
						TimeUnit.NANOSECONDS));
			} catch (InterruptedException e) {
			}

			now = System.nanoTime();
		}

		return now - start;
	}

	/**
	 * 清理满行，得分升级
	 *
	 * @history 增加清除动画，from ver 2.2.0
	 */
	private void clearFullRows(Set<Integer> lines) {
		// 清除满行
		List<Integer> rows = DataController.getRemainder().findFullrows(lines);
		if (rows == null || rows.isEmpty()) return;

		// 显示清除动画
		for (int i = 0; i < 2; i++) {
			for (int n = 0; n < ViewProp.MAIN_PANEL_COLS; n++) {
				ViewController.showClearing(rows, n);
				ctrlGameSpeed(SleepTime.CLEAR_INTERVAL.getCode(), GameOrder.FIX);
			}
		}
		DataController.getRemainder().removeRows(rows);

		// 得分升级
		DataController.addScore(rows.size());
		ViewController.refreshScore();
		ViewController.refreshSpeedLevel();
	}

	/**
	 * 计算当前等级需要暂停的时间
	 *
	 * @return 暂停时间（毫秒数）
	 * @history 重新设计方块下落速度，from ver 2.2.1
	 */
	private long getPauseTime() {
		long sleepMilliS = SleepTime.BASE.getCode();
		if (DataController.getSpeedLevel() > 1) {
			sleepMilliS = new Double(SleepTime.BASE.getCode()
					/ (0.85 * Math.exp(0.1644 * DataController.getSpeedLevel()))).longValue();
		}
		return sleepMilliS;
	}

	/**
	 * 游戏结束时前端展示
	 *
	 * @history 动画展示，from ver 2.2.2
	 */
	private void gameOver() {
		DataController.giveOrder(GameOrder.PAUSE, false);

		// 游戏结束动画
		for (int n = ViewProp.MAIN_PANEL_ROWS - 1; n >= 0; n--) {
			ViewController.paintMainPanelByRow(n, ViewProp.BLOCK_BACK_COLOR);
			ctrlGameSpeed(SleepTime.OVER_INTERVAL.getCode(), DataController.getOrder());
		}

		// 初始化
		DataController.initData();
		ViewController.initView();

		// 游戏初始化动画
		for (int n = 0; n < ViewProp.MAIN_PANEL_ROWS; n++) {
			ViewController.paintMainPanelByRow(n, null);
			ctrlGameSpeed(SleepTime.OVER_INTERVAL.getCode(), DataController.getOrder());
		}

	}

}