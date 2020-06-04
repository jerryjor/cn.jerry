package cn.jerry.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel2007 导入工具类
 * 使用poi插件，提供把简单的xlsx文件读取为数据集合的功能
 * 读取结果为List<? extends XlsxRowData>格式
 * 暂未实现读取合并单元格功能
 * 
 * @author Jerry.zhao
 */
public class XlsxReader {
    private final XSSFWorkbook workbook;
    private final XSSFFormulaEvaluator fe;
    private XSSFSheet sheet;
    private int startRow = 0;
    private int startCol = 0;
    private int rowIndex;

    private boolean hasHeader = true;
    private HashMap<Integer, String> header;

    public XlsxReader(InputStream is) throws IOException {
        super();
        workbook = new XSSFWorkbook(is);
        sheet = workbook.getSheetAt(0);
        fe = new XSSFFormulaEvaluator(workbook);
    }

    public XlsxReader(InputStream is, int startRow, int startCol) throws IOException {
        this(is);
        this.startRow = startRow;
        this.startCol = startCol;
    }

    /**
     * main method
     * read excel
     */
    public <T extends XlsxDataBuilder> List<T> readXlsWithHeader(Class<T> cls) {
        List<T> records = new ArrayList<>();
        if (!hasHeader) return records;

        int sheetNum = workbook.getNumberOfSheets();
        for (int s = 0; s < sheetNum; s++) {
            sheet = workbook.getSheetAt(s);
            int rMax = sheet.getLastRowNum(); // 最后一行标记
            if (startRow > rMax) continue;
            // read header
            rowIndex = Math.max(startRow, sheet.getFirstRowNum());
            if (hasHeader) {
                readHeaderRow();
            } else {
                header = new HashMap<>();
            }
            // read data
            while (rowIndex <= rMax) {
            	Map<String, Object> record = readNextRow();
				try {
					T t = cls.newInstance();
	            	t.buildByRow(record);
	                records.add(t);
				} catch (InstantiationException | IllegalAccessException e) {
					// log e
				}
            }
        }
        return records;
    }

    /**
     * read header row
     */
    private void readHeaderRow() {
        Map<Integer, Object> rowData = readRow();
        if (rowData != null) {
            header = new HashMap<>();
            Set<Integer> keys = rowData.keySet();
            for (Integer key : keys) {
                String content = (String) rowData.get(key);
                if (content != null && content.length() > 0) {
                    header.put(key, content);
                }
            }
        }
    }

    /**
     * read data row
     */
    private Map<String, Object> readNextRow() {
        Map<String, Object> record = new HashMap<>();
        Map<Integer, Object> rowData = readRow();
        if (rowData != null) {
            // build rocord
            for (Entry<Integer, Object> cell : rowData.entrySet()) {
                String key = header.get(cell.getKey());
                if (key == null) key = "COL" + cell.getKey();
                record.put(key, cell.getValue());
            }
        }
        return record;
    }

    /**
     * read a row
     */
    private Map<Integer, Object> readRow() {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null) return null;
        int start = row.getFirstCellNum();
        int end = row.getLastCellNum();
        if (startCol > end) return null;

        Map<Integer, Object> rowData = new HashMap<>();
        int colIndex = Math.max(startCol, start);
        while (colIndex <= end) {
            Cell cell = row.getCell(colIndex);
            rowData.put(colIndex, readCell(cell));
            colIndex++;
        }
        rowIndex++;
        return rowData;
    }

    /**
     * read a cell
     */
    private Object readCell(Cell cell) {
        if (cell == null) return null;

        Object cellContent;
        int cellType = cell.getCellType();
        switch (cellType) {
        case Cell.CELL_TYPE_STRING:
            cellContent = cell.getStringCellValue();
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            cellContent = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                cellContent = cell.getDateCellValue();
            } else {
                cellContent = cell.getNumericCellValue();
            }
            break;
        case Cell.CELL_TYPE_FORMULA:
            cellContent = readFormula(cell);
            break;
        default:
            cellContent = null;
            break;
        }
        return cellContent;
    }

    /**
     * read formula calculate result
     */
    private Object readFormula(Cell cell) {
        if (cell == null) return null;

        Object cellContent;
        CellValue cellValue = fe.evaluate(cell);
        int valueType = cellValue.getCellType();
        switch (valueType) {
        case Cell.CELL_TYPE_STRING:
            cellContent = cellValue.getStringValue();
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            cellContent = cellValue.getBooleanValue();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            cellContent = cellValue.getNumberValue();
            break;
        case Cell.CELL_TYPE_BLANK:
        case Cell.CELL_TYPE_ERROR:
            cellContent = null;
            break;
        default:
            cellContent = cellValue.formatAsString();
            break;
        }
        return cellContent;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

}
