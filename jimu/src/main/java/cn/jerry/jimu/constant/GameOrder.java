package cn.jerry.jimu.constant;

public enum GameOrder {
	OVER(0, "结束"),
	START(1, "开始"),
	PAUSE(2, "暂停"),
	GO_ON(3, "继续"),
	ROTATE(4, "顺时针旋转"),
	MOVE_LEFT(6, "左移"),
	MOVE_RIGHT(7, "右移"),
	DROP(8, "加速下落"),
	FIX(9, "固定");

	private int code;
	private String desc;

	private GameOrder(int code, String desc) {
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
