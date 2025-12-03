package jh.exp.common.exception;

/**
 * 业务异常：用于参数校验、业务规则触发等场景。
 */
public class GatewayBizException extends GatewayException {

    public GatewayBizException(String code, String message) {
        super(code, message);
    }

    public GatewayBizException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}

