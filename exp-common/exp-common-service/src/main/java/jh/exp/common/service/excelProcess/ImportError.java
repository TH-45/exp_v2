package jh.exp.common.service.excelProcess;

/**
 * Excel 导入错误信息封装。
 * <p>
 * 记录导入过程中某一行数据的错误信息，包括行号、错误原因等。
 */
public class ImportError {

    /**
     * 错误行号（从 1 开始，包含表头）
     */
    private int rowNumber;

    /**
     * 错误原因描述
     */
    private String errorMessage;

    /**
     * 原始行数据（可选，用于调试）
     */
    private Object rowData;

    public ImportError() {
    }

    public ImportError(int rowNumber, String errorMessage) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
    }

    public ImportError(int rowNumber, String errorMessage, Object rowData) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.rowData = rowData;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getRowData() {
        return rowData;
    }

    public void setRowData(Object rowData) {
        this.rowData = rowData;
    }

    @Override
    public String toString() {
        return String.format("第 %d 行：%s", rowNumber, errorMessage);
    }
}




