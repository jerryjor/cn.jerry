package cn.jerry.jimu.data;

import cn.jerry.jimu.constant.GameOrder;
import cn.jerry.jimu.constant.GameStatus;
import cn.jerry.jimu.model.Block;
import cn.jerry.jimu.model.Remainder;

import java.io.Serializable;

public class GameData implements Serializable {
	private static final long serialVersionUID = 1L;

	// 版本信息
	public static final String VERSION = "2.2.8";

	// 游戏执行多少次后自动增加方块
	public static final int ADD_ROW_LOOP_TIMES = 256;
	// 已执行次数
	private int steps = 0;

	// 当前状态
	private GameStatus status = GameStatus.STOPED;
	// 当前指令
	private GameOrder order = GameOrder.PAUSE;

	// 当前得分
	private int score = 0;
	// 升级所需分数
	private int nextLevelScore = 0;
	// 最高等级
	public static final int MAX_SPEED_LEVEL = 15;
	// 当前等级
	private int speedLevel = 1;
	// 起始速度等级
	private int startSpeedLevel = 1;

	// 上次按键时间
	private long lastKeyPressed = 0l;
	// 上次睡眠时间
	private long lastSleepTime = 0l;
	// 是否开启自动增长
	private boolean autoIncrease = false;

	// 是否显示阴影
	private boolean showShadow = false;
	// 是否使用正常方块
	private boolean normalBlock = true;

	// 方块组，第0个显示在主面板，其余显示在缓存面板
	private Block[] blocks;
	// 当前方块的影子
	private Block shadow;
	// 残余方块单元
	private Remainder remainder;

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public GameOrder getOrder() {
		return order;
	}

	public void setOrder(GameOrder order) {
		this.order = order;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getNextLevelScore() {
		return nextLevelScore;
	}

	public void setNextLevelScore(int nextLevelScore) {
		this.nextLevelScore = nextLevelScore;
	}

	public int getSpeedLevel() {
		return speedLevel;
	}

	public void setSpeedLevel(int speedLevel) {
		this.speedLevel = speedLevel;
	}

	public int getStartSpeedLevel() {
		return startSpeedLevel;
	}

	public void setStartSpeedLevel(int startSpeedLevel) {
		this.startSpeedLevel = startSpeedLevel;
	}

	public long getLastKeyPressed() {
		return lastKeyPressed;
	}

	public void setLastKeyPressed(long lastKeyPressed) {
		this.lastKeyPressed = lastKeyPressed;
	}

	public long getLastSleepTime() {
		return lastSleepTime;
	}

	public void setLastSleepTime(long lastSleepTime) {
		this.lastSleepTime = lastSleepTime;
	}

	public boolean isAutoIncrease() {
		return autoIncrease;
	}

	public void setAutoIncrease(boolean autoIncrease) {
		this.autoIncrease = autoIncrease;
	}

	public boolean isShowShadow() {
		return showShadow;
	}

	public void setShowShadow(boolean showShadow) {
		this.showShadow = showShadow;
	}

	public boolean isNormalBlock() {
		return normalBlock;
	}

	public void setNormalBlock(boolean normalBlock) {
		this.normalBlock = normalBlock;
	}

	public Block[] getBlocks() {
		return blocks;
	}

	public void setBlocks(Block[] blocks) {
		this.blocks = blocks;
	}

	public Block getShadow() {
		return shadow;
	}

	public void setShadow(Block shadow) {
		this.shadow = shadow;
	}

	public Remainder getRemainder() {
		return remainder;
	}

	public void setRemainder(Remainder remainder) {
		this.remainder = remainder;
	}

}
