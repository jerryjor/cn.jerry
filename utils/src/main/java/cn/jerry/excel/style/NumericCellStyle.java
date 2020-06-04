package cn.jerry.excel.style;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * 数字单元格格式定义
 */
public class NumericCellStyle extends XlsxCellStyle {
	public static final String SPLIT_COMMA = ",";
	public static final String SPLIT_DOT = ".";
	public static final String SPLIT_BLANK = " ";

	private String pattern;

	public NumericCellStyle(String kc, int dgits) {
		super(Cell.CELL_TYPE_NUMERIC);
		setAlign(CellStyle.ALIGN_RIGHT);
		setPattern(createNumberPattern(kc, dgits, null));
	}

	public NumericCellStyle() {
		this(SPLIT_COMMA, 2);
	}

	/**
	 * 拼装数字格式字符串
	 */
	protected String createNumberPattern(String kc, Integer dgits, String dot) {
		StringBuilder numPattern = new StringBuilder();
		if (kc != null && (kc.equals(SPLIT_BLANK) || kc.equals(SPLIT_COMMA) || kc.equals(SPLIT_DOT))) {
            numPattern.append("#").append(kc).append("##0");
		} else {
            numPattern.append("#0");
		}

		if (dgits != null && dgits > 0) {
			if (dot != null && (dot.equals(SPLIT_DOT) || dot.equals(SPLIT_COMMA))) {
                numPattern.append(dot);
			} else if (kc != null && kc.equals(SPLIT_DOT)) {
                numPattern.append(SPLIT_COMMA);
			} else {
                numPattern.append(SPLIT_DOT);
			}
			for (int i = 0; i < dgits; i++) {
                numPattern.append("0");
			}
		}

		return numPattern.toString();
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
