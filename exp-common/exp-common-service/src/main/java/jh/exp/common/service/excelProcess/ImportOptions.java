package jh.exp.common.service.excelProcess;

/**
 * Excel 导入配置选项。
 * <p>
 * 用于控制导入行为，如跳过空行、最大行数限制、快速失败阈值等。
 */
public class ImportOptions {

    /**
     * 表头行号（从 1 开始），默认为 1
     */
    private int headRowNumber = 1;

    /**
     * 要读取的工作表索引（从 0 开始），默认为 0
     */
    private int sheetNo = 0;

    /**
     * 是否跳过空行，默认为 true
     */
    private boolean skipEmptyRows = true;

    /**
     * 最大允许导入行数（防止内存溢出），0 表示不限制，默认为 50000
     */
    private int maxRows = 50_000;

    /**
     * 快速失败阈值：当错误行数达到此值时，立即停止导入，默认为 0（不启用）
     */
    private int failFastThreshold = 0;

    /**
     * 是否启用数据校验（使用 JSR-303 注解），默认为 true
     */
    private boolean enableValidation = true;

    /**
     * 批次大小（用于大数据量场景的内存优化），默认为 1000
     */
    private int batchSize = 1000;

    public int getHeadRowNumber() {
        return headRowNumber;
    }

    public void setHeadRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
    }

    public int getSheetNo() {
        return sheetNo;
    }

    public void setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
    }

    public boolean isSkipEmptyRows() {
        return skipEmptyRows;
    }

    public void setSkipEmptyRows(boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getFailFastThreshold() {
        return failFastThreshold;
    }

    public void setFailFastThreshold(int failFastThreshold) {
        this.failFastThreshold = failFastThreshold;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}




