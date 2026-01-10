package jh.exp.common.exception;

/**
 * 认证/鉴权相关异常。
 */
public class GatewayAuthException extends GatewayException {

    public GatewayAuthException(String code, String message) {
        super(code, message);
    }

    public GatewayAuthException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}

