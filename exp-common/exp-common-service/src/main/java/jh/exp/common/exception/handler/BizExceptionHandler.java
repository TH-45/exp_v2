package jh.exp.common.exception.handler;

import jh.exp.common.api.ApiResponse;
import jh.exp.common.exception.GatewayBizException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 业务异常处理器：用于返回业务错误码与提示。
 */
@Component
@Order(-5)
public class BizExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof GatewayBizException;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        GatewayBizException ex = (GatewayBizException) throwable;
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }
}

