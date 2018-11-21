package cn.jerry.excel.style;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;

/**
 * 文本单元格格式定义
 */
public class TextCellStyle extends XlsxCellStyle {
	public TextCellStyle() {
		super(Cell.CELL_TYPE_STRING);
	}

	public TextCellStyle(double widthRate) {
		this();
		setTextWidthRate(widthRate);
	}

	public TextCellStyle(boolean bold) {
		this();
		setFontBold(bold ? Font.BOLDWEIGHT_BOLD : Font.BOLDWEIGHT_NORMAL);
	}
}
