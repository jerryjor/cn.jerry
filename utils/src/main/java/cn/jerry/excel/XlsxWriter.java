package cn.jerry.excel;

import cn.jerry.excel.style.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel2007 导出工具类
 * 使用poi插件，提供基本的把查询结果列表导出为xlsx文件的功能
 * 如果数据对象中未定义列类型，将默认全部按文本处理
 *
 * @author Jerry.zhao
 */
public class XlsxWriter {
    private final SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*
     * 内部变量
     */
    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private Row row;
    private Cell cell;
    private int rowIndex;
    private int colIndex;
    private int totalCols;
    private DataFormat format;
    private Map<Integer, XlsxCellStyle> colsDefinition;

    /*
     * 构造方法和setter
     */
    private XlsxWriter(XlsxCellStyle[] colsStyle) {
        super();
        init();
        initColsStyle(colsStyle);
    }

    private void init() {
        this.workbook = new SXSSFWorkbook();
        this.format = this.workbook.createDataFormat();
        this.colsDefinition = new HashMap<>();
        this.createNewSheet();
    }

    /**
     * 创建新的Sheet，并设置新sheet为活动sheet
     * Sheet命名规则：“Sheet”+序号
     */
    private void createNewSheet() {
        int n = this.workbook.getNumberOfSheets();
        String sheetName = "Sheet" + (n + 1);
        this.sheet = this.workbook.createSheet(sheetName);
        this.rowIndex = -1;
        this.colIndex = -1;
    }

    /**
     * 新的一行
     */
    private void createNewRow() {
        this.rowIndex++;
        this.row = this.sheet.createRow(this.rowIndex);
        this.colIndex = -1;
    }

    /**
     * 新的单元格
     */
    private void createNewCell() {
        this.colIndex++;
        this.cell = this.row.createCell(this.colIndex);
        this.cell.setAsActiveCell();
    }

    /**
     * 根据用户定义的列格式，生成excel内部格式列表
     */
    private void initColsStyle(XlsxCellStyle[] colsStyle) {
        // 表头格式
        XlsxCellStyle headerStyle = fillCellStyle(new TextCellStyle(true));
        this.colsDefinition.put(-1, headerStyle);
        // 普通文本：右对齐
        XlsxCellStyle wrapTextStyle = fillCellStyle(new TextCellStyle());
        this.colsDefinition.put(-2, wrapTextStyle);
        // 长数字文本：右对齐
        XlsxCellStyle numTextStyle = fillCellStyle(new TextCellStyle());
        numTextStyle.setAlign(CellStyle.ALIGN_RIGHT);
        this.colsDefinition.put(-3, numTextStyle);
        // 公示错误
        XlsxCellStyle errorStyle = fillCellStyle(new ErrorCellStyle());
        this.colsDefinition.put(-4, errorStyle);
        // 生成自定义列格式
        if (colsStyle != null && colsStyle.length > 0) {
            for (int i = 0; i < colsStyle.length; i++) {
                XlsxCellStyle colStyle = colsStyle[i];
                fillCellStyle(colStyle);
                this.colsDefinition.put(i, colStyle);
            }
        }
    }

    /**
     * 根据用户定义的列格式，生成excel内部格式
     */
    private XlsxCellStyle fillCellStyle(XlsxCellStyle colStyle) {
        // 如果未指定列类型，默认文本
        if (colStyle == null) colStyle = new TextCellStyle();
        // 如果已生成过列格式，跳过
        if (colStyle.getCellStyle() != null) return colStyle;

        CellStyle style = this.workbook.createCellStyle();
        // 通用格式
        style.setAlignment(colStyle.getAlign());
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        Font textFont = this.workbook.createFont();
        textFont.setColor(colStyle.getFontColor());
        textFont.setBoldweight(colStyle.getFontBold());
        style.setFont(textFont);
        style.setWrapText(false);
        // 数值（数字、金额、百分比、日期）
        if (colStyle instanceof NumericCellStyle) {
            String pattern = ((NumericCellStyle) colStyle).getPattern();
            style.setDataFormat(this.format.getFormat(pattern));
        }

        colStyle.setCellStyle(style);
        return colStyle;
    }

    /**
     * 追加一行表头
     */
    public XlsxWriter appendHeader(List<String> row) {
        createNewRow();
        if (row == null || row.isEmpty()) return this;
        totalCols = Math.max(totalCols, row.size());

        for (String s : row) {
            createNewCell();
            XlsxCellStyle colDf = this.colsDefinition.get(-1);
            cell.setCellValue(s);
            cell.setCellType(colDf.getCellType());
            cell.setCellStyle(colDf.getCellStyle());
        }
        return this;
    }

    /**
     * 追加一行数据
     */
    public void appendData(Object[] row) {
        if (row == null || row.length == 0) return;
        totalCols = Math.max(totalCols, row.length);

        XlsxCellStyle colDf;
        for (Object cellValue : row) {
            createNewCell();
            colDf = this.colsDefinition.get(this.colIndex);
            if (colDf == null) colDf = this.colsDefinition.get(-2);
            this.cell.setCellType(colDf.getCellType());
            this.cell.setCellStyle(colDf.getCellStyle());
            if (cellValue == null) {
                writeEmptyCell();
            } else if (colDf.isFormula()) {
                writeFormulaCell(cellValue);
            } else if (Cell.CELL_TYPE_NUMERIC == colDf.getCellType()) {
                if (colDf instanceof DateCellStyle) {
                    writeDateCell(cellValue);
                } else {
                    writeNumericCell(cellValue);
                }
            } else {
                writeTextCell(cellValue, true);
            }
        }
    }

    /**
     * 空单元格
     */
    private void writeEmptyCell() {
        cell.setCellValue("");
        cell.setCellType(Cell.CELL_TYPE_BLANK);
    }

    /**
     * 公式单元格
     */
    private void writeFormulaCell(Object cellValue) {
        cell.setCellFormula(cellValue.toString());
        cell.setCellType(Cell.CELL_TYPE_FORMULA);
    }

    private void writeDateCell(Object cellValue) {
        // 把文本转换为日期值
        Date date = getDateValue(cellValue);
        // 转换失败，作为文本处理
        if (date == null) {
            this.cell.setCellType(Cell.CELL_TYPE_STRING);
            this.cell.setCellStyle(this.colsDefinition.get(-3).getCellStyle());
            writeTextCell(cellValue, false);
            return;
        }

        cell.setCellValue(date);
    }

    /**
     * 数值单元格
     */
    private void writeNumericCell(Object cellValue) {
        // 把文本值转换为数值
        BigDecimal value = getNumberValue(cellValue);
        // 转换失败，作为文本处理
        if (value == null) {
            this.cell.setCellType(Cell.CELL_TYPE_STRING);
            this.cell.setCellStyle(this.colsDefinition.get(-3).getCellStyle());
            writeTextCell(cellValue, false);
            return;
        }

        this.cell.setCellValue(value.doubleValue());
    }

    /**
     * 文本单元格
     */
    private void writeTextCell(Object cellValue, boolean wrap) {
        String text = getStringValue(cellValue);
        this.cell.setCellValue(text);
        wrap = wrap && calcLength(text) > XlsxCellStyle.TEXT_WRAP_LENGTH;
        this.cell.getCellStyle().setWrapText(wrap);
    }

    /**
     * 对象转日期
     */
    private Date getDateValue(Object obj) {
        Date value;
        if (obj == null) {
            value = null;
        } else if (obj instanceof Date) {
            value = (Date) obj;
        } else {
            BigDecimal time = getNumberValue(obj);
            if (time != null) {
                value = new Date(time.longValue());
            } else {
                value = null;
            }
        }
        return value;
    }

    /**
     * 对象转数值
     */
    private BigDecimal getNumberValue(Object obj) {
        BigDecimal value;
        if (obj == null) {
            value = null;
        } else if (obj instanceof BigDecimal) {
            value = (BigDecimal) obj;
        } else if (obj instanceof BigInteger) {
            value = new BigDecimal((BigInteger) obj);
        } else if (obj instanceof Double) {
            value = BigDecimal.valueOf((Double) obj);
        } else if (obj instanceof Float) {
            value = BigDecimal.valueOf((Float) obj);
        } else if (obj instanceof Long) {
            value = new BigDecimal((Long) obj);
        } else if (obj instanceof Integer) {
            value = new BigDecimal((Integer) obj);
        } else if (obj instanceof Short) {
            value = new BigDecimal((Short) obj);
        } else if (obj instanceof Character) {
            value = new BigDecimal((Character) obj);
        } else if (obj instanceof Date) {
            value = new BigDecimal(((Date) obj).getTime());
        } else {
            try {
                value = new BigDecimal(obj.toString());
            } catch (Exception e) {
                value = null;
            }
        }
        return value;
    }

    /**
     * 对象转字符串
     */
    private String getStringValue(Object obj) {
        String value;
        if (obj == null) {
            value = "";
        } else if (obj instanceof Date) {
            value = defaultFormat.format((Date) obj);
        } else if (obj instanceof BigDecimal) {
            value = ((BigDecimal) obj).toPlainString();
        } else {
            value = obj.toString();
        }
        return value;
    }

    /**
     * 计算字符串长度，中文字符=2
     */
    private int calcLength(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        } else {
            try {
                return text.getBytes("gb2312").length;
            } catch (Exception ex) {
                return text.length();
            }
        }
    }

    /**
     * 调整所有 sheet的列宽
     */
    private void sizeColumn() {
        int sheetCount = this.workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            Sheet currSheet = this.workbook.getSheetAt(i);
            for (int j = 0; j < this.totalCols; j++) {
                currSheet.autoSizeColumn(j);
                BigDecimal widthRate = BigDecimal.ONE;
                XlsxCellStyle colDf = this.colsDefinition.get(j);
                if (colDf != null) {
                    widthRate = BigDecimal.valueOf(this.colsDefinition.get(j).getTextWidthRate());
                }
                int colWidth = new BigDecimal(currSheet.getColumnWidth(j)).multiply(widthRate)
                        .setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + 512;
                currSheet.setColumnWidth(j, colWidth);
            }
        }
    }

    /**
     * 合并单元格
     */
    public void mergeCells(List<XlsxCellMergeDef> mergeDef) {
        if (mergeDef == null || mergeDef.isEmpty()) return;

        int sheetCount = this.workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            Sheet currSheet = this.workbook.getSheetAt(i);
            for (XlsxCellMergeDef def : mergeDef) {
                try {
                    currSheet.addMergedRegion(new CellRangeAddress(def.getBeginRow(),
                            def.getEndRow(), def.getBeginCol(), def.getEndCol()));
                } catch (Exception e) {
                    // do nothing...
                }
            }
        }
    }

    /**
     * 输出
     */
    public byte[] finish() throws IOException {
        // 调整所有 sheet的列宽
        sizeColumn();

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            this.workbook.write(os);
            byte[] bytes = os.toByteArray();
            os.flush();
            return bytes;
        }
    }

    /**
     * 输出
     */
    public void finish(OutputStream os) throws IOException {
        // 调整所有 sheet的列宽
        sizeColumn();
        this.workbook.write(os);
    }

    public static XlsxWriter newInstance(XlsxCellStyle[] colsStyle) {
        return new XlsxWriter(colsStyle);
    }

}
