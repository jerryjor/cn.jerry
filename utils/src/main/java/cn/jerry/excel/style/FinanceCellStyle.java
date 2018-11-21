package cn.jerry.excel.style;

/**
 * 金额单元格格式定义
 */
public class FinanceCellStyle extends NumericCellStyle {
	public static String CURRENCY_CNY = "¥";
	public static String CURRENCY_USD = "$";
	public static String CURRENCY_EUR = "€";
	public static String CURRENCY_GBP = "£";

	private String currency;

	public FinanceCellStyle(String currency, String kc, int dgits) {
		super(kc, dgits);
		setPattern((currency == null ? "" : currency) + getPattern());
	}

	public FinanceCellStyle() {
		this(CURRENCY_CNY, SPLIT_COMMA, 2);
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
