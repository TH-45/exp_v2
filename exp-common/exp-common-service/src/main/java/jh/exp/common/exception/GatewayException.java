package jh.exp.common.exception;

/**
 * 网关层自定义基础异常，包含业务编码与提示信息。
 */
public class GatewayException extends RuntimeException {

    private final String code;

    public GatewayException(String code, String message) {
        super(message);
        this.code = code;
    }

    public GatewayException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

