package cn.jerry.excel.style;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;

/**
 * 错误单元格格式定义
 */
public class ErrorCellStyle extends TextCellStyle {
	public ErrorCellStyle() {
		super(false);
		setCellType(Cell.CELL_TYPE_ERROR);
		setFontColor(Font.COLOR_RED);
	}
}
