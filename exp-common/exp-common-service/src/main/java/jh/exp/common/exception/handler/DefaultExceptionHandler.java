package jh.exp.common.exception.handler;

import jh.exp.common.api.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 兜底异常处理器，捕获所有未明确处理的异常。
 */
@Component
@Order(0)
public class DefaultExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public boolean supports(Throwable throwable) {
        return true;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        String message = throwable.getMessage() != null ? throwable.getMessage() : "系统繁忙，请稍后再试";
        return ApiResponse.fail("500", message);
    }
}

