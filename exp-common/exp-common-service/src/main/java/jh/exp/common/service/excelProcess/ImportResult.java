package jh.exp.common.service.excelProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果封装。
 * <p>
 * 包含成功导入的数据列表、失败行错误信息、统计信息等。
 *
 * @param <T> 导入数据的类型
 */
public class ImportResult<T> {

    /**
     * 成功导入的数据列表
     */
    private List<T> successList = new ArrayList<>();

    /**
     * 失败行的错误信息列表
     */
    private List<ImportError> errorList = new ArrayList<>();

    /**
     * 总行数（成功 + 失败）
     */
    private int totalRows;

    /**
     * 成功行数
     */
    private int successRows;

    /**
     * 失败行数
     */
    private int errorRows;

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return errorRows == 0 && totalRows > 0;
    }

    /**
     * 是否全部失败
     */
    public boolean isAllFailed() {
        return successRows == 0 && totalRows > 0;
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return errorRows > 0;
    }

    public List<T> getSuccessList() {
        return successList;
    }

    public void setSuccessList(List<T> successList) {
        this.successList = successList != null ? successList : new ArrayList<>();
    }

    public List<ImportError> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ImportError> errorList) {
        this.errorList = errorList != null ? errorList : new ArrayList<>();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(int successRows) {
        this.successRows = successRows;
    }

    public int getErrorRows() {
        return errorRows;
    }

    public void setErrorRows(int errorRows) {
        this.errorRows = errorRows;
    }
}




