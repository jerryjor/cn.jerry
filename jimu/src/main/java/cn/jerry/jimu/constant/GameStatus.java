package cn.jerry.jimu.constant;

public enum GameStatus {
	STOPED(0, "未开始"),
	STARTED(1, "已开始"),
	PAUSING(2, "暂停");

	private int code;
	private String desc;

	private GameStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
