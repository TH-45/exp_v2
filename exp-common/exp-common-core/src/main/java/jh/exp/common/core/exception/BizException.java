package jh.exp.common.core.exception;

/**
 * 业务异常：用于业务逻辑校验失败等场景。
 * 通常用于抛出业务级别的错误信息，不包含错误码。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}