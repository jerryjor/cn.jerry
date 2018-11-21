package cn.jerry.excel;

public class XlsxCellMergeDef {
    // 起始单元格的行，start with 0
    private int beginRow;
    // 起始单元格的列，start with 0
    private int beginCol;
    // 结束单元格的行
    private int endRow;
    // 结束单元格的列
    private int endCol;

    public XlsxCellMergeDef() {
        super();
    }

    public XlsxCellMergeDef(int beginRow, int endRow, int beginCol, int endCol) {
        this();
        this.beginRow = beginRow;
        this.endRow = endRow;
        this.beginCol = beginCol;
        this.endCol = endCol;
    }

    public int getBeginRow() {
        return beginRow;
    }

    public void setBeginRow(int beginRow) {
        this.beginRow = beginRow;
    }

    public int getBeginCol() {
        return beginCol;
    }

    public void setBeginCol(int beginCol) {
        this.beginCol = beginCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }

}
