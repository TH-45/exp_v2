package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.exception.BizException;
import jh.exp.common.core.exception.GatewayBizException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 业务异常处理器：用于处理BizException和GatewayBizException业务异常。
 */
@Component
@Order(-5)
public class BizExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof BizException || throwable instanceof GatewayBizException;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        if (throwable instanceof GatewayBizException) {
            GatewayBizException ex = (GatewayBizException) throwable;
            return ApiResponse.fail(ex.getCode(), ex.getMessage());
        } else if (throwable instanceof BizException) {
            BizException ex = (BizException) throwable;
            return ApiResponse.fail("400", ex.getMessage());
        }
        return ApiResponse.fail("500", "系统异常");
    }
}