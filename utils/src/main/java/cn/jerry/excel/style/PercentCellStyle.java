package cn.jerry.excel.style;

/**
 * 百分比单元格格式定义
 */
public class PercentCellStyle extends NumericCellStyle {

	public PercentCellStyle(int dgits) {
		super(null, dgits);
		setPattern(getPattern() + "%");
	}

	public PercentCellStyle() {
		this(2);
	}

}
