package jh.exp.common.service.excelProcess;

/**
 * Excel 导入导出相关异常。
 * <p>
 * 用于统一处理 Excel 操作中的各种错误场景，如文件格式错误、数据校验失败、IO 异常等。
 */
public class ExcelException extends RuntimeException {

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}




