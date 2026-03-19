package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.stream.Collectors;

/**
 * 参数校验/绑定异常处理器。
 */
@Component
@Order(-80)
public class ValidationExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public int getOrder() {
        return -80;
    }

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof WebExchangeBindException
                || throwable instanceof BindException
                || throwable instanceof ServerWebInputException;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        String message = "请求参数不合法";
        if (throwable instanceof WebExchangeBindException bindException) {
            message = bindException.getFieldErrors().stream()
                    .map(error -> error.getField() + ":" + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (throwable instanceof BindException bindException) {
            message = bindException.getFieldErrors().stream()
                    .map(error -> error.getField() + ":" + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (throwable instanceof ServerWebInputException serverWebInputException) {
            message = serverWebInputException.getReason() != null
                    ? serverWebInputException.getReason()
                    : message;
        }
        return ApiResponse.fail("400", message);
    }
}

