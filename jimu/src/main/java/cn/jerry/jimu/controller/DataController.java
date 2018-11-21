package cn.jerry.jimu.controller;

import cn.jerry.jimu.constant.GameOrder;
import cn.jerry.jimu.constant.GameStatus;
import cn.jerry.jimu.constant.ViewProp;
import cn.jerry.jimu.data.GameData;
import cn.jerry.jimu.model.Block;
import cn.jerry.jimu.model.Remainder;

public class DataController {

	private static final GameData DATA = new GameData();

	/**
	 * 初始化游戏数据
	 * 
	 * @return
	 */
	public static void initData() {
		DATA.setStatus(GameStatus.STOPED);
		if (DATA.getStartSpeedLevel() == 0) {
			DATA.setStartSpeedLevel(1);
		}
		DATA.setSpeedLevel(DATA.getStartSpeedLevel());
		DATA.setScore(0);
		DATA.setNextLevelScore(calcNextLevelScore());
		DATA.setSteps(0);
	}

	/**
	 * 判断游戏是否已开始
	 * 
	 * @return 是否已开始
	 */
	public static boolean isStart() {
		return GameStatus.STOPED != DATA.getStatus();
	}

	public static void startGame() {
		DATA.setStatus(GameStatus.STARTED);
	}

	public static void pauseGame() {
		DATA.setStatus(GameStatus.PAUSING);
	}

	public static GameStatus getGameStatus() {
		return DATA.getStatus();
	}

	public static void giveOrderByStatus() {
		switch (DATA.getStatus()) {
		case STOPED:
			DataController.giveOrder(GameOrder.START, true);
			break;
		case STARTED:
			DataController.giveOrder(GameOrder.PAUSE, true);
			break;
		case PAUSING:
			DataController.giveOrder(GameOrder.GO_ON, true);
			break;
		}
	}

	/**
	 * 设置游戏指令
	 * 
	 * @param newOrder 新指令
	 * @param fromKey 是否源于按键
	 * @history 执行动画等动作时不响应按键，from ver 2.2.0
	 */
	public static void giveOrder(GameOrder newOrder, boolean fromKey) {
		switch (newOrder) {
		case START:
		case PAUSE:
		case GO_ON:
		case OVER:
			DATA.setOrder(newOrder);
			break;
		default:
			// 如果游戏未处于RUNNING指令，不执行按键动作
			if (fromKey && DATA.getOrder() != GameOrder.GO_ON)
			    return;

			DATA.setOrder(newOrder);
			break;
		}
	}

	/**
	 * 记录间隔时间
	 * 
	 * @param sleepTime
	 */
	public static void addLastSleepTime(long sleepTime) {
		DATA.setLastSleepTime(DATA.getLastSleepTime() + sleepTime);
	}

	/**
	 * 清除间隔时间
	 */
	public static void clearLastSleepTime() {
		DATA.setLastSleepTime(0l);
	}

	/**
	 * 记录按键
	 */
	public static void logKeyPressed() {
		DATA.setLastKeyPressed(System.nanoTime() / 1000);
	}

	/**
	 * 改变初始速度等级
	 * 
	 * @param raise 是否增加
	 * @return 是否成功改变
	 */
	public static void changeStartSpeedLevel(boolean raise) {
		if (raise) {
			DATA.setStartSpeedLevel(DATA.getStartSpeedLevel() % 15 + 1);
		} else {
			DATA.setStartSpeedLevel((DATA.getStartSpeedLevel() + 13) % 15 + 1);
		}
		DATA.setSpeedLevel(DATA.getStartSpeedLevel());
	}

	/**
	 * 增加步数
	 */
	public static void addSteps() {
		DATA.setSteps(DATA.getSteps() + 1);
	}

	/**
	 * 增加步数
	 */
	public static void clearSteps() {
		DATA.setSteps(0);
	}

	/**
	 * 底部是否需要增加一行
	 * 
	 * @return 是否添加
	 */
	public static boolean needAddBottomRow() {
		return DataController.isAutoIncrease()
				&& DATA.getSteps() > GameData.ADD_ROW_LOOP_TIMES;
	}

	/**
	 * 得分升级
	 */
	public static void addScore(int rows) {
		// 得分
		DATA.setScore(DATA.getScore() + (rows * (rows + 1) / 2));
		// 升级
		if (DATA.getSpeedLevel() == GameData.MAX_SPEED_LEVEL) return;
		if (DATA.getScore() >= DATA.getNextLevelScore()) {
			DATA.setSpeedLevel(DATA.getSpeedLevel() + 1);
			DATA.setNextLevelScore(calcNextLevelScore());
		}
	}

	/**
	 * 计算下次升级所需要达到的分数
	 * 
	 * @return 下次升级所需要达到的分数
	 */
	private static int calcNextLevelScore() {
		return DATA.getSpeedLevel() * (DATA.getSpeedLevel() + 1) * 10;
	}

	public static GameOrder getOrder() {
		return DATA.getOrder();
	}

	public static int getScore() {
		return DATA.getScore();
	}

	public static int getSpeedLevel() {
		return DATA.getSpeedLevel();
	}

	public static long getLastKeyPressed() {
		return DATA.getLastKeyPressed();
	}

	public static long getLastSleepTime() {
		return DATA.getLastSleepTime();
	}

	public static boolean isAutoIncrease() {
		return DATA.isAutoIncrease();
	}

	public static boolean changeAutoIncrease() {
		DATA.setAutoIncrease(!DATA.isAutoIncrease());
		return DATA.isAutoIncrease();
	}

	public static boolean isShowShadow() {
		return DATA.isShowShadow();
	}

	public static boolean changeShadowShowing() {
		DATA.setShowShadow(!DATA.isShowShadow());
		if (DATA.getBlocks()[0] != null) DATA.getBlocks()[0].calcShadowPosition();
		return DATA.isShowShadow();
	}

	public static boolean isNormalBlock() {
		return DATA.isNormalBlock();
	}

	public static boolean changeBlockMode() {
		DATA.setNormalBlock(!DATA.isNormalBlock());
		return DATA.isNormalBlock();
	}

	public static Block[] getBlocks() {
		return DATA.getBlocks();
	}

	public static void clearBlocks() {
		DATA.setBlocks(new Block[ViewProp.CACHED_BLOCKS + 1]);
	}

	public static Block getShadow() {
		return DATA.getShadow();
	}

	public static void setShadow(Block shadow) {
		DATA.setShadow(shadow);
	}

	public static Remainder getRemainder() {
		return DATA.getRemainder();
	}

	public static void clearRemainder() {
		DATA.setRemainder(new Remainder());
	}

}
