package jh.exp.common.service.exception;

import jh.exp.common.core.exception.GatewayException;

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

