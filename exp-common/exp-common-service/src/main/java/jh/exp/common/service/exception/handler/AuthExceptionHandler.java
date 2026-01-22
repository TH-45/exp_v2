package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.exception.GatewayAuthException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 鉴权异常处理器：如 Token 失效、权限不足等。
 */
@Component
@Order(-4)
public class AuthExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof GatewayAuthException;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        GatewayAuthException ex = (GatewayAuthException) throwable;
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }
}

