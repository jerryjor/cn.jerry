package cn.jerry.excel.style;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

/**
 * 空单元格格式定义
 */
public abstract class XlsxCellStyle {
	// 换行阈值
	public static final int TEXT_WRAP_LENGTH = 20;

	// 单元格类型
	private int cellType;
	// 单元格格式
	private CellStyle cellStyle;

	// 居中
	private Short align = CellStyle.ALIGN_CENTER;
	// 颜色
	private Short fontColor = Font.COLOR_NORMAL;
	// 加粗
	private Short fontBold = Font.BOLDWEIGHT_NORMAL;
	// 列宽比例
	private double textWidthRate = 1.0;
	// 是否为公式
	private boolean formula = false;

	public XlsxCellStyle(int cellType) {
		super();
		this.cellType = cellType;
	}

	public int getCellType() {
		return cellType;
	}

	public void setCellType(int cellType) {
		this.cellType = cellType;
	}

	public CellStyle getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(CellStyle cellStyle) {
		this.cellStyle = cellStyle;
	}

	public Short getAlign() {
		return align;
	}

	public void setAlign(Short align) {
		this.align = align;
	}

	public Short getFontColor() {
		return fontColor;
	}

	public void setFontColor(Short fontColor) {
		this.fontColor = fontColor;
	}

	public Short getFontBold() {
		return fontBold;
	}

	public void setFontBold(Short fontBold) {
		this.fontBold = fontBold;
	}

	public double getTextWidthRate() {
		return textWidthRate;
	}

	public void setTextWidthRate(double textWidthRate) {
		this.textWidthRate = textWidthRate;
	}

	public boolean isFormula() {
		return formula;
	}

	public void setFormula(boolean formula) {
		this.formula = formula;
	}

}
