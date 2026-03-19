package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.exception.AuthException;
import jh.exp.common.core.exception.GatewayAuthException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 鉴权异常处理器：如 Token 失效、权限不足等。
 * 支持 AuthException（含 code）和 GatewayAuthException，产出 AUTH_FORBIDDEN / AUTH_PERMISSION_CHANGED 等错误码供前端识别。
 */
@Component
@Order(-100)
public class AuthExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof GatewayAuthException || throwable instanceof AuthException;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        if (throwable instanceof GatewayAuthException) {
            GatewayAuthException ex = (GatewayAuthException) throwable;
            return ApiResponse.fail(ex.getCode(), ex.getMessage());
        }
        AuthException ex = (AuthException) throwable;
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }
}

