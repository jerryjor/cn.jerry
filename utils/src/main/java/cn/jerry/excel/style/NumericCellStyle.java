package cn.jerry.excel.style;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

/**
 * 数字单元格格式定义
 */
public class NumericCellStyle extends XlsxCellStyle {
	public static String SPLIT_COMMA = ",";
	public static String SPLIT_DOT = ".";
	public static String SPLIT_BLANK = " ";

	private String pattern;

	public NumericCellStyle(String kc, int dgits) {
		super(Cell.CELL_TYPE_NUMERIC);
		setAlign(XSSFCellStyle.ALIGN_RIGHT);
		setPattern(createNumberPattern(kc, dgits, null));
	}

	public NumericCellStyle() {
		this(SPLIT_COMMA, 2);
	}

	/**
	 * 拼装数字格式字符串
	 * 
	 * @param kc
	 * @param dgits
	 * @param dot
	 * @return
	 */
	protected String createNumberPattern(String kc, Integer dgits, String dot) {
		String pattern;
		if (kc != null
				&& (kc.equals(SPLIT_BLANK) || kc.equals(SPLIT_COMMA) || kc.equals(SPLIT_DOT))) {
			pattern = "#" + kc + "##0";
		} else {
			pattern = "#0";
		}

		if (dgits != null && dgits > 0) {
			if (dot != null
					&& (dot.equals(SPLIT_DOT) || dot.equals(SPLIT_COMMA))) {
				pattern += dot;
			} else if (kc != null && kc.equals(SPLIT_DOT)) {
				pattern += SPLIT_COMMA;
			} else {
				pattern += SPLIT_DOT;
			}
			for (int i = 0; i < dgits; i++) {
				pattern += "0";
			}
		}

		return pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
