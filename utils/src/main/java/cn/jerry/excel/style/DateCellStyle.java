package cn.jerry.excel.style;


/**
 * 日期单元格格式定义
 */
public class DateCellStyle extends NumericCellStyle {
	public static String FORMAT_DATE_DEFAULT = "yyyy-mm-dd";
	public static String FORMAT_TIME_DEFAULT = "HH:mm:ss";
	public static String FORMAT_DATETIME_DEFAULT = "yyyy-mm-dd HH:mm:ss";

	public DateCellStyle(String fomat) {
		super(null, 0);
		setPattern(fomat);
	}

	public DateCellStyle() {
		this(FORMAT_DATETIME_DEFAULT);
	}

}
