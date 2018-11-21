package cn.jerry.jimu.constant;

import java.util.concurrent.TimeUnit;

public enum SleepTime {
	BASE(TimeUnit.NANOSECONDS.convert(1024, TimeUnit.MILLISECONDS), "基础时间"),
	MIN(TimeUnit.NANOSECONDS.convert(4, TimeUnit.MILLISECONDS), "最小时间"),
	FIX_WAIT(TimeUnit.NANOSECONDS.convert(256, TimeUnit.MILLISECONDS), "方块固定时间间隔"),
	MOVE_INTERVAL(TimeUnit.NANOSECONDS.convert(16, TimeUnit.MILLISECONDS), "方块移动最小时间间隔"),
	CLEAR_INTERVAL(TimeUnit.NANOSECONDS.convert(64, TimeUnit.MILLISECONDS), "清除动画闪动间隔"),
	OVER_INTERVAL(TimeUnit.NANOSECONDS.convert(128, TimeUnit.MILLISECONDS), "结束动画闪动间隔"),
	KEY_INTERVAL(TimeUnit.NANOSECONDS.convert(128, TimeUnit.MILLISECONDS), "按键最小时间间隔");

	private long code;
	private String desc;

	private SleepTime(long code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public long getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
